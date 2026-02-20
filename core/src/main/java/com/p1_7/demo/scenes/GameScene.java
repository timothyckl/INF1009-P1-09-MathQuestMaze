package com.p1_7.demo.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.p1_7.abstractengine.collision.CollisionManager;
import com.p1_7.abstractengine.entity.IEntityMutator;
import com.p1_7.abstractengine.movement.MovementManager;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.demo.Settings;
import com.p1_7.demo.display.LivesDisplay;
import com.p1_7.demo.display.ScoreDisplay;
import com.p1_7.demo.entities.Background;
import com.p1_7.demo.entities.Bucket;
import com.p1_7.demo.entities.Cloud;
import com.p1_7.demo.entities.Droplet;

/**
 * main game scene for the "catch the droplet" demo.
 *
 * manages bucket, falling droplets, background, lives display, and
 * audio. spawns multiple droplets over time, detects caught/missed
 * droplets, and handles game over at 0 lives.
 */
public class GameScene extends Scene {

    /** initial number of lives */
    private static final int INITIAL_LIVES = 10;

    /** y coordinate where droplets spawn (top of screen) */
    private static final float SPAWN_Y = Settings.WINDOW_HEIGHT;

    /** maximum concurrent droplets */
    private static final int MAX_DROPLETS = 5;

    /** seconds between droplet spawns */
    private static final float SPAWN_INTERVAL = 1.0f;
    private static final float HORIZONTAL_DRAG = 0.9f;
    private static final float MIN_HORIZONTAL_SPEED = 5f;

    // ==================== game entities ====================

    private Bucket bucket;
    private Array<Droplet> droplets = new Array<>();
    private Array<Cloud> clouds = new Array<>();
    private Background background;
    private LivesDisplay livesDisplay;
    private ScoreDisplay scoreDisplay;

    // ==================== audio ====================

    private Sound dropSound;
    private Music music;

    // ==================== game state ====================

    private int score = 0;
    private boolean gameOver = false;
    private float spawnTimer = 0f;

    // ==================== manager references ====================

    private final MovementManager movementManager;
    private final CollisionManager collisionManager;

    /**
     * constructs a game scene with references to required managers.
     *
     * @param movementManager  the movement manager for bucket registration
     * @param collisionManager the collision manager for entity registration
     */
    public GameScene(MovementManager movementManager,
                     CollisionManager collisionManager) {
        this.name = "game";
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
    }

    // ==================== lifecycle hooks ====================

    @Override
    public void onEnter(SceneContext context) {
        // 0. reset game state (for replays)
        score = 0;
        gameOver = false;
        spawnTimer = 0f;
        droplets.clear();

        // 1. load audio
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.setVolume(Settings.MUSIC_VOLUME);
        music.play();

        // 2. create background (not an entity)
        background = new Background(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);

        // 3. create lives display via entity manager
        livesDisplay = (LivesDisplay) context.entities().createEntity(() -> new LivesDisplay(INITIAL_LIVES));

        // 4. create score display via entity manager
        scoreDisplay = (ScoreDisplay) context.entities().createEntity(
            () -> new ScoreDisplay(520f, Settings.WINDOW_HEIGHT - 10f, 0)
        );

        // 5. create bucket via entity manager
        float bucketX = (Settings.WINDOW_WIDTH / 2f) - (Bucket.BUCKET_WIDTH / 2f);
        float bucketY = 20f;
        bucket = (Bucket) context.entities().createEntity(() -> new Bucket(bucketX, bucketY));

        // 6. register bucket with managers
        movementManager.registerMovable(bucket);
        collisionManager.registerCollidable(bucket);

        // wire catch handler
        bucket.setCatchHandler(this::handleDropletCatch);

        // 7. create and register cloud deflectors
        createClouds(context.entities());

        // 8. spawn initial droplet
        spawnDroplet(context.entities());
    }

    @Override
    public void onExit(SceneContext context) {
        // stop and dispose audio
        if (music != null) {
            music.stop();
            music.dispose();
        }
        if (dropSound != null) {
            dropSound.dispose();
        }

        // dispose font
        if (livesDisplay != null) {
            livesDisplay.dispose();
        }

        // clean up bucket
        if (bucket != null) {
            movementManager.unregisterMovable(bucket);
            collisionManager.unregisterCollidable(bucket);
            context.entities().removeEntity(bucket.getId());
        }

        // clean up remaining droplets
        for (int i = 0; i < droplets.size; i++) {
            Droplet droplet = droplets.get(i);
            collisionManager.unregisterCollidable(droplet);
            context.entities().removeEntity(droplet.getId());
        }
        droplets.clear();

        // clean up clouds
        for (int i = 0; i < clouds.size; i++) {
            Cloud cloud = clouds.get(i);
            collisionManager.unregisterCollidable(cloud);
            context.entities().removeEntity(cloud.getId());
        }
        clouds.clear();

        // remove lives display entity
        if (livesDisplay != null) {
            context.entities().removeEntity(livesDisplay.getId());
        }

        // remove score display entity
        if (scoreDisplay != null) {
            scoreDisplay.dispose();
            context.entities().removeEntity(scoreDisplay.getId());
        }
    }

    @Override
    public void onSuspend(SceneContext context) {
        // minimal cleanup for pause - keep all entities and state intact

        // pause music during pause menu
        if (music != null) {
            music.pause();
        }
    }

    @Override
    public void onResume(SceneContext context) {
        // reconnect resources after resuming from pause

        // reapply volume setting (may have changed in pause menu)
        if (music != null) {
            music.setVolume(Settings.MUSIC_VOLUME);
            music.play();
        }
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        // check for pause key press
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.P)) {

            // get pause scene and pass current state
            PauseScene pauseScene = (PauseScene) context.getScene("pause");
            if (pauseScene != null) {
                pauseScene.setGameState(livesDisplay.getLives(), score);
                pauseScene.setMusicReference(music);
            }

            // use suspendScene to preserve game state
            context.suspendScene("pause");
            return;
        }

        // early exit if game over
        if (gameOver) {
            return;
        }

        // 1. update bucket movement
        bucket.updateMovement(context.input());

        // 2. update spawn timer and spawn new droplets
        spawnTimer += deltaTime;
        if (spawnTimer >= SPAWN_INTERVAL && droplets.size < MAX_DROPLETS) {
            spawnDroplet(context.entities());
            spawnTimer = 0f;
        }

        // 3. update droplets (reverse iteration for safe removal)
        for (int i = droplets.size - 1; i >= 0; i--) {
            Droplet droplet = droplets.get(i);

            // manually move droplet (not registered with MovementManager)
            droplet.move(deltaTime);

            // reset to straight fall (no horizontal movement)
            float[] velocity = droplet.getVelocity();
            velocity[0] *= HORIZONTAL_DRAG;
            if (Math.abs(velocity[0]) < MIN_HORIZONTAL_SPEED) {
                velocity[0] = 0f;
            }
            velocity[1] = -Droplet.FALL_SPEED;
            droplet.setVelocity(velocity);

            // clamp droplet to horizontal bounds (prevent sliding off-screen)
            float[] position = droplet.getTransform().getPosition();
            if (position[0] < 0) {
                position[0] = 0;
            } else if (position[0] + Droplet.DROPLET_WIDTH > Settings.WINDOW_WIDTH) {
                position[0] = Settings.WINDOW_WIDTH - Droplet.DROPLET_WIDTH;
            }
            droplet.getTransform().setPosition(position);

            // check if caught
            if (droplet.isCaught()) {
                // remove entity
                context.entities().removeEntity(droplet.getId());

                // unregister from collision manager
                collisionManager.unregisterCollidable(droplet);

                // remove from array
                droplets.removeIndex(i);
                continue;
            }

            // check if missed (fell below screen)
            if (droplet.getTransform().getPosition()[1] < 0) {
                // decrement lives
                int currentLives = livesDisplay.getLives();
                livesDisplay.setLives(currentLives - 1);

                // remove entity
                context.entities().removeEntity(droplet.getId());

                // unregister from collision manager
                collisionManager.unregisterCollidable(droplet);

                // remove from array
                droplets.removeIndex(i);

                // check game over
                if (livesDisplay.getLives() == 0) {
                    gameOver = true;

                    // pass score to game over scene and transition
                    GameOverScene gameOverScene = (GameOverScene) context.getScene("gameover");
                    if (gameOverScene != null) {
                        gameOverScene.setScore(score);
                    }
                    context.changeScene("gameover");
                }
            }
        }
    }

    @Override
    public void submitRenderable(SceneContext context) {
        // background first (draws behind)
        context.renderQueue().queue(background);

        // bucket
        context.renderQueue().queue(bucket);

        // all active droplets
        for (int i = 0; i < droplets.size; i++) {
            context.renderQueue().queue(droplets.get(i));
        }

        // clouds (draw above droplets but below ui)
        for (int i = 0; i < clouds.size; i++) {
            context.renderQueue().queue(clouds.get(i));
        }

        // ui displays last (draw on top)
        context.renderQueue().queue(livesDisplay);
        context.renderQueue().queue(scoreDisplay);
    }

    // ==================== helper methods ====================

    /**
     * spawns a new droplet at a random x position at the top of the screen.
     *
     * @param mutator the entity mutator for creating entities
     */
    private void spawnDroplet(IEntityMutator mutator) {
        float x = randomX();
        Droplet droplet = (Droplet) mutator.createEntity(() -> new Droplet(x, SPAWN_Y));

        // add to array
        droplets.add(droplet);

        // register with collision manager
        collisionManager.registerCollidable(droplet);
    }

    /**
     * returns a random x position that keeps the droplet fully on-screen.
     *
     * @return random x coordinate in valid range
     */
    private float randomX() {
        return MathUtils.random(Settings.WINDOW_WIDTH - Droplet.DROPLET_WIDTH);
    }

    /**
     * handles a droplet being caught by the bucket.
     *
     * increments score and plays catch sound. called by the bucket's
     * collision handler.
     *
     * @param droplet the droplet that was caught
     */
    private void handleDropletCatch(Droplet droplet) {
        // increment score
        score++;

        // update score display
        scoreDisplay.setScore(score);

        // play catch sound
        dropSound.play();
    }

    /**
     * creates three cloud deflectors positioned to create obstacles
     * for falling droplets.
     *
     * @param mutator the entity mutator for creating entities
     */
    private void createClouds(IEntityMutator mutator) {
        // left cloud
        Cloud leftCloud = (Cloud) mutator.createEntity(
            () -> new Cloud(60f, 400f)
        );
        clouds.add(leftCloud);
        collisionManager.registerCollidable(leftCloud);

        // right cloud
        Cloud rightCloud = (Cloud) mutator.createEntity(
            () -> new Cloud(414f, 400f)
        );
        clouds.add(rightCloud);
        collisionManager.registerCollidable(rightCloud);

        // middle cloud (below the others)
        Cloud middleCloud = (Cloud) mutator.createEntity(
            () -> new Cloud(242f, 300f)
        );
        clouds.add(middleCloud);
        collisionManager.registerCollidable(middleCloud);
    }
}

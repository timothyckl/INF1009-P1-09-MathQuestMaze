package com.p1_7.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.p1_7.abstractengine.collision.CollisionManager;
import com.p1_7.abstractengine.entity.IEntityMutator;
import com.p1_7.abstractengine.movement.MovementManager;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;

/**
 * main game scene for the "catch the droplet" demo.
 *
 * manages bucket, falling droplets, background, lives display, and
 * audio. spawns multiple droplets over time, detects caught/missed
 * droplets, and handles game over at 0 lives.
 */
public class GameScene extends Scene {

    /** initial number of lives */
    private static final int INITIAL_LIVES = 3;

    /** y coordinate where droplets spawn (top of screen) */
    private static final float SPAWN_Y = Settings.WINDOW_HEIGHT;

    /** maximum concurrent droplets */
    private static final int MAX_DROPLETS = 5;

    /** seconds between droplet spawns */
    private static final float SPAWN_INTERVAL = 1.0f;

    // ==================== game entities ====================

    private Bucket bucket;
    private Array<Droplet> droplets = new Array<>();
    private Background background;
    private LivesDisplay livesDisplay;

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
    private final IEntityMutator entityMutator;

    /**
     * constructs a game scene with references to required managers.
     *
     * @param movementManager  the movement manager for bucket registration
     * @param collisionManager the collision manager for entity registration
     * @param entityMutator    the entity mutator for creation and removal
     */
    public GameScene(MovementManager movementManager,
                     CollisionManager collisionManager,
                     IEntityMutator entityMutator) {
        this.name = "game";
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
        this.entityMutator = entityMutator;
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
        music.play();

        // 2. create background (not an entity)
        background = new Background(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);

        // 3. create lives display via entity manager
        livesDisplay = (LivesDisplay) entityMutator.createEntity(() -> new LivesDisplay(INITIAL_LIVES));

        // 4. create bucket via entity manager
        float bucketX = (Settings.WINDOW_WIDTH / 2f) - (Bucket.BUCKET_WIDTH / 2f);
        float bucketY = 20f;
        bucket = (Bucket) entityMutator.createEntity(() -> new Bucket(bucketX, bucketY));

        // 5. register bucket with managers
        movementManager.registerMovable(bucket);
        collisionManager.registerCollidable(bucket);

        // wire catch handler
        bucket.setCatchHandler(this::handleDropletCatch);

        // 6. spawn initial droplet
        spawnDroplet();
    }

    @Override
    public void onExit(SceneContext context) {
        // stop and dispose audio
        music.stop();
        music.dispose();
        dropSound.dispose();

        // dispose font
        livesDisplay.dispose();

        // clean up bucket
        if (bucket != null) {
            movementManager.unregisterMovable(bucket);
            collisionManager.unregisterCollidable(bucket);
            entityMutator.removeEntity(bucket.getId());
        }

        // clean up remaining droplets
        for (int i = 0; i < droplets.size; i++) {
            Droplet droplet = droplets.get(i);
            collisionManager.unregisterCollidable(droplet);
            entityMutator.removeEntity(droplet.getId());
        }
        droplets.clear();

        // remove lives display entity
        if (livesDisplay != null) {
            entityMutator.removeEntity(livesDisplay.getId());
        }
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        // early exit if game over
        if (gameOver) {
            return;
        }

        // 1. update bucket movement
        bucket.updateMovement(context.input());

        // 2. update spawn timer and spawn new droplets
        spawnTimer += deltaTime;
        if (spawnTimer >= SPAWN_INTERVAL && droplets.size < MAX_DROPLETS) {
            spawnDroplet();
            spawnTimer = 0f;
        }

        // 3. update droplets (reverse iteration for safe removal)
        for (int i = droplets.size - 1; i >= 0; i--) {
            Droplet droplet = droplets.get(i);

            // manually move droplet (not registered with MovementManager)
            droplet.move(deltaTime);

            // check if caught
            if (droplet.isCaught()) {
                // remove entity
                entityMutator.removeEntity(droplet.getId());

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
                entityMutator.removeEntity(droplet.getId());

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

        // lives display last (draws on top)
        context.renderQueue().queue(livesDisplay);
    }

    // ==================== helper methods ====================

    /**
     * spawns a new droplet at a random x position at the top of the screen.
     */
    private void spawnDroplet() {
        float x = randomX();
        Droplet droplet = (Droplet) entityMutator.createEntity(() -> new Droplet(x, SPAWN_Y));

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

        // play catch sound
        dropSound.play();
    }
}

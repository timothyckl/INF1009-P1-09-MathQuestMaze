package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.game.input.GameActions;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.GameConfig;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.ui.GameHudRenderer;
import com.p1_7.game.ui.HudStrip;
import com.p1_7.game.entities.HostileCharacter;
import com.p1_7.game.entities.Player;
import com.p1_7.game.gameplay.Difficulty;
import com.p1_7.game.gameplay.EnemyController;
import com.p1_7.game.gameplay.GamePhaseController;
import com.p1_7.game.gameplay.GamePhaseListener;
import com.p1_7.game.gameplay.ItemSpawner;
import com.p1_7.game.gameplay.MovementPipeline;
import com.p1_7.game.gameplay.RoundPhase;
import com.p1_7.game.level.ILevelOrchestrator;
import com.p1_7.game.managers.MazeCollisionManager;
import com.p1_7.game.maze.MazeLayout;
import com.p1_7.game.maze.WallCollidable;
import com.p1_7.game.managers.GameMovementManager;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.items.Item;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * core gameplay scene — wires the level orchestrator, player movement, wall collision,
 * and answer-room entry detection.
 *
 * delegates gameplay responsibilities to focused collaborators:
 *   - GamePhaseController  — phase state machine and room-entry detection
 *   - EnemyController      — enemy spawning and AI updates
 *   - GameHudRenderer      — score, health, level, feedback, and answer labels
 *   - ItemSpawner          — heart pickup placement
 *   - MovementPipeline     — documents the three-step movement ordering
 */
public class GameScene extends Scene implements GamePhaseListener {

    /** lighter blue-slate background for the walkable playfield */
    private static final Color SCENE_BG_COLOUR = new Color(0.15f, 0.19f, 0.27f, 1f);

    /** solid wall fill colour for the generated maze */
    private static final Color WALL_FILL_COLOUR = new Color(0.07f, 0.10f, 0.16f, 1f);

    // collaborators ───────────────────────────────────────────────────

    private final GamePhaseController phaseController  = new GamePhaseController();
    private final EnemyController     enemyController  = new EnemyController();
    private final ItemSpawner         itemSpawner      = new ItemSpawner();
    private final MovementPipeline    movementPipeline = new MovementPipeline();
    private final GameHudRenderer     hudRenderer      = new GameHudRenderer();

    // scene-lifecycle fields ──────────────────────────────────────────

    /** the fixed spatial layout providing spawn point, room bounds, and wall bounds */
    private MazeLayout layout;

    /** the player entity — created at scene entry, released on exit */
    private Player player;

    /** all hostile characters (goblins + skeletons) */
    private List<HostileCharacter> enemies;

    /** collectable pickups currently present in the maze */
    private List<Item> items;

    /** solid background quad for the gameplay area */
    private IRenderable backgroundRenderable;

    /** wall collidables registered with the collision manager */
    private List<WallCollidable> wallCollidables;

    /** one renderable per wall rectangle so the maze geometry is visible */
    private List<IRenderable> wallRenderables;

    /**
     * cached copies of the four room bounds arrays; MazeLayout is immutable so these
     * never change — caching avoids defensive-clone allocations inside the per-frame loop
     */
    private float[][] cachedRoomBounds;

    /**
     * constructs the game scene with the scene key "game".
     */
    public GameScene() {
        this.name = "game";
    }

    /**
     * initialises the layout, player, input query, orchestrator, and room state.
     *
     * @param context the engine service context
     */
    @Override
    public void onEnter(SceneContext context) {
        // ensure the scene always starts unpaused
        setPaused(false);
        context.get(IAudioManager.class).playMusic("game", true);
        this.layout   = MazeLayout.createDefault();
        float[] spawn = layout.getSpawnPoint();
        this.player   = new Player(spawn[0], spawn[1]);
        this.backgroundRenderable = createBackgroundRenderable();

        // wire movement manager and register the player for position integration
        GameMovementManager movementManager = context.get(GameMovementManager.class);
        movementManager.registerMovable(player);

        // wire collision manager and register the player and all walls
        MazeCollisionManager collisionManager = context.get(MazeCollisionManager.class);
        this.wallCollidables = new ArrayList<>();
        this.wallRenderables = new ArrayList<>();
        collisionManager.registerPlayer(player);
        for (float[] rect : layout.getWallBounds()) {
            WallCollidable wall = new WallCollidable(rect);
            wallCollidables.add(wall);
            collisionManager.registerWall(wall);
            wallRenderables.add(createWallRenderable(rect));
        }

        // wire level orchestrator and start the session at the selected difficulty
        ILevelOrchestrator orchestrator = context.get(ILevelOrchestrator.class);
        Difficulty difficulty = orchestrator.getCurrentDifficulty();
        orchestrator.startLevel(difficulty);
        player.bindGameplay(orchestrator);

        // cache room bounds once
        this.cachedRoomBounds = new float[4][];
        for (int i = 0; i < cachedRoomBounds.length; i++) {
            cachedRoomBounds[i] = layout.getRoomBounds(i);
        }

        // spawn enemies via the controller
        this.enemies = enemyController.spawnEnemies(layout, difficulty);
        for (HostileCharacter enemy : enemies) {
            movementManager.registerMovable(enemy);
            collisionManager.registerMover(enemy);
        }

        // spawn items via the spawner
        this.items = itemSpawner.spawnItems(layout, orchestrator);
        for (Item item : items) {
            collisionManager.registerItem(item);
        }

        // initialise HUD — fonts, answer labels, panels, overlays
        hudRenderer.init(context, layout, orchestrator, difficulty);

        // initialise phase controller — prevent a spurious onPhaseChanged on the first tick
        phaseController.setLastKnownPhase(orchestrator.getPhase());
        phaseController.setHoldTimer(GameConfig.QUESTION_INTRO_HOLD_SECONDS);

    }

    /**
     * releases all scene-owned references so they can be garbage collected.
     *
     * @param context the engine service context
     */
    @Override
    public void onExit(SceneContext context) {
        GameMovementManager movementManager = context.get(GameMovementManager.class);
        MazeCollisionManager collisionManager = context.get(MazeCollisionManager.class);

        movementManager.unregisterMovable(player);
        for (HostileCharacter enemy : enemies) {
            movementManager.unregisterMovable(enemy);
            collisionManager.unregisterMover(enemy);
        }
        for (Item item : items) {
            collisionManager.unregisterItem(item);
        }
        enemies = null;
        items   = null;

        collisionManager.unregisterPlayer();
        for (WallCollidable wall : wallCollidables) {
            collisionManager.unregisterWall(wall);
        }

        hudRenderer.dispose();

        cachedRoomBounds     = null;
        wallRenderables      = null;
        layout               = null;
        player               = null;
        backgroundRenderable = null;
        wallCollidables      = null;
    }

    /**
     * called when the pause overlay opens — freezes the game world in place.
     *
     * @param context the engine service context
     */
    @Override
    public void onSuspend(SceneContext context) {
        setPaused(true);
        player.setVelocity(new float[]{ 0f, 0f });
        enemyController.freezeEnemies(enemies);
        context.get(IAudioManager.class).pauseMusic();
    }

    /**
     * called when the pause overlay closes — resumes normal gameplay.
     *
     * @param context the engine service context
     */
    @Override
    public void onResume(SceneContext context) {
        setPaused(false);
        context.get(IAudioManager.class).resumeMusic();
    }

    /**
     * resolves player input, detects phase changes, and checks answer-room entry.
     *
     * @param deltaTime elapsed seconds since the last frame
     * @param context   the engine service context
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        ILevelOrchestrator orchestrator = context.get(ILevelOrchestrator.class);
        IInputQuery inputQuery = context.get(IInputQuery.class);
        RoundPhase phase = orchestrator.getPhase();

        // detect phase change and react
        phaseController.detectPhaseChange(phase, orchestrator, this);

        // non-interactive phases: freeze input and auto-advance after hold timer
        if (phase != RoundPhase.CHOOSING) {
            if (phase == RoundPhase.GAME_OVER && orchestrator.wasLastDamageFromEnemy()) {
                context.changeScene("game-over");
                return;
            }
            // step 1 of the movement pipeline — velocity zeroed via phase lock
            movementPipeline.step(deltaTime, player, inputQuery, phase);
            enemyController.freezeEnemies(enemies);
            if (phase == RoundPhase.QUESTION_INTRO) {
                hudRenderer.updateQuestionPanel(deltaTime);
            }
            if (phaseController.tickHoldTimer(deltaTime)) {
                if (GamePhaseController.isTerminalPhase(phase)) {
                    context.changeScene(
                        phase == RoundPhase.LEVEL_COMPLETE ? "level-complete" : "game-over");
                } else {
                    orchestrator.advance();
                    // re-read and process the resulting phase in the same frame
                    RoundPhase newPhase = orchestrator.getPhase();
                    phaseController.detectPhaseChange(newPhase, orchestrator, this);
                }
            }
            return;
        }

        // choosing phase: check for pause request before processing player input
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            context.suspendScene("pause");
            return;
        }

        // step 1 of the movement pipeline — resolve input into velocity
        movementPipeline.step(deltaTime, player, inputQuery, phase);
        enemyController.updateEnemies(deltaTime, enemies, player, wallCollidables);
        phaseController.checkRoomEntry(deltaTime, player, cachedRoomBounds, orchestrator);
    }

    /**
     * submits all renderables to the queue in painter's order.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        if (wallRenderables == null) return; // scene has already exited
        renderQueue.queue(backgroundRenderable);
        for (IRenderable wall : wallRenderables) {
            renderQueue.queue(wall);
        }
        // room answer labels sit behind entity sprites
        hudRenderer.submitRoomLabels(renderQueue);
        for (HostileCharacter enemy : enemies) {
            renderQueue.queue(enemy);
        }
        for (Item item : items) {
            if (item.isActive()) {
                renderQueue.queue(item);
            }
        }
        renderQueue.queue(player);
        // HUD overlays sit on top of everything
        hudRenderer.submitHudOverlays(renderQueue, paused);
    }

    // GamePhaseListener ───────────────────────────────────────────────

    /**
     * called whenever the round phase transitions. resets the player on ROUND_RESET
     * and refreshes the answer cache / panel animation on QUESTION_INTRO.
     */
    @Override
    public void onPhaseChanged(RoundPhase from, RoundPhase to,
                               ILevelOrchestrator orchestrator) {
        if (to == RoundPhase.ROUND_RESET) {
            player.resetToSpawn(layout.getSpawnPoint());
            for (HostileCharacter enemy : enemies) {
                enemy.resetToSpawn();
            }
            phaseController.resetRoomState();
        }
        if (to == RoundPhase.QUESTION_INTRO) {
            hudRenderer.refreshAnswerCache(orchestrator);
            hudRenderer.beginQuestionIntro(orchestrator.getCurrentQuestion().getPrompt());
        }
    }

    // private helpers ─────────────────────────────────────────────────

    private IRenderable createWallRenderable(float[] rect) {
        final float[] stableRect = rect.clone();
        final Transform2D transform = new Transform2D(
            stableRect[0], stableRect[1], stableRect[2], stableRect[3]);
        return new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).rect(
                    WALL_FILL_COLOUR,
                    stableRect[0], stableRect[1], stableRect[2], stableRect[3], true);
            }
        };
    }

    private IRenderable createBackgroundRenderable() {
        final Transform2D transform = new Transform2D(
            0f, 0f, Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);
        return new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).drawTintedQuad(
                    SCENE_BG_COLOUR, 0f, 0f,
                    Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);
            }
        };
    }

}

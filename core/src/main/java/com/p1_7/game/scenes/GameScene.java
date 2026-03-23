package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.game.input.GameActions;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.HudStrip;
import com.p1_7.game.ui.QuestionPanel;
import com.p1_7.game.entities.Enemy;
import com.p1_7.game.entities.HostileCharacter;
import com.p1_7.game.entities.Player;
import com.p1_7.game.entities.Skeleton;
import com.p1_7.game.gameplay.Difficulty;
import com.p1_7.game.gameplay.RoundPhase;
import com.p1_7.game.level.ILevelOrchestrator;
import com.p1_7.game.managers.MazeCollisionManager;
import com.p1_7.game.maze.MazeLayout;
import com.p1_7.game.maze.WallCollidable;
import com.p1_7.game.managers.GameMovementManager;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.managers.IFontManager;
import com.p1_7.game.items.Heart;
import com.p1_7.game.items.Item;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * core gameplay scene — wires the level orchestrator, player movement, wall collision,
 * and answer-room entry detection.
 *
 * update ordering each frame:
 *   1. GameScene.update()  — resolve input / check room entry
 *   2. GameMovementManager — integrate player position
 *   3. MazeCollisionManager — push player out of any penetrating wall
 *
 * room entry is checked every frame during the CHOOSING phase. a 1-second re-entry
 * cooldown prevents a wrong room from penalising the player more than once per visit.
 * grey outlines are rendered for each answer room during testing.
 */
public class GameScene extends Scene {

    /** re-entry cooldown in seconds before a wrong room can penalise the player again */
    private static final float ROOM_COOLDOWN_SECONDS = 1.0f;

    /**
     * hold time for QUESTION_INTRO — matches QuestionPanel.ANIM_START_DELAY (1.5 s)
     * + QuestionPanel.ANIM_DURATION (1.0 s) = 2.5 s.
     */
    private static final float QUESTION_INTRO_HOLD_SECONDS = 2.5f;

    /** hold time in seconds for ROUND_RESET */
    private static final float ROUND_RESET_HOLD_SECONDS = 1.0f;

    /** hold time in seconds for the FEEDBACK phase — longer to allow the player to read the result */
    private static final float FEEDBACK_HOLD_SECONDS = 2.0f;

    /** width of the centre trigger zone within each answer room in pixels */
    private static final float TRIGGER_WIDTH  = 100f;

    /** height of the centre trigger zone within each answer room in pixels */
    private static final float TRIGGER_HEIGHT = 80f;

    /** pre-allocated overlay colours — reused every frame to avoid per-frame allocation */
    private static final Color OVERLAY_CORRECT    = new Color(0.08f, 0.62f, 0.22f, 0.42f);
    private static final Color OVERLAY_WRONG      = new Color(0.75f, 0.08f, 0.08f, 0.42f);

    /** lighter blue-slate background for the walkable playfield */
    private static final Color SCENE_BG_COLOUR = new Color(0.15f, 0.19f, 0.27f, 1f);

    /** solid wall fill colour for the generated maze */
    private static final Color WALL_FILL_COLOUR = new Color(0.07f, 0.10f, 0.16f, 1f);

/** the fixed spatial layout providing spawn point, room bounds, and wall bounds */
    private MazeLayout layout;

    /** the player entity — created at scene entry, released on exit */
    private Player player;

    /** corner-room goblins plus one roaming skeleton — created at scene entry, released on exit */
    private List<HostileCharacter> enemies;

    /** collectable pickups currently present in the maze */
    private List<Item> items;

    /** solid background quad for the gameplay area */
    private IRenderable backgroundRenderable;

    /** wall collidables registered with the collision manager for this scene */
    private List<WallCollidable> wallCollidables;

    /** one renderable per wall rectangle so the maze geometry is visible */
    private List<IRenderable> wallRenderables;

    /** tracks whether the player is currently overlapping each answer room */
    private boolean[] playerInsideRoom;

    /** per-room countdown timer; fires when the player exits a room, blocks re-entry while > 0 */
    private float[] roomCooldownTimers;

    /**
     * cached copies of the four room bounds arrays; MazeLayout is immutable so these
     * never change — caching avoids defensive-clone allocations inside the per-frame loop
     */
    private float[][] cachedRoomBounds;

    /** phase recorded at the end of the previous update; used to detect transitions */
    private RoundPhase lastKnownPhase;

    /** countdown timer used to auto-advance through non-interactive phases */
    private float phaseHoldTimer;

    /** one IRenderable per answer room — renders only the answer label each frame */
    private List<IRenderable> roomRenderables;

    /**
     * per-room answer strings and their pre-computed glyph layouts.
     * refreshed once per question (when entering QUESTION_INTRO) so room renderables
     * never allocate during render.
     */
    private String[]      roomAnswerTexts;
    private GlyphLayout[] roomAnswerLayouts;

    /** animated panel that slides to the bottom of the screen during QUESTION_INTRO */
    private QuestionPanel questionPanel;

    /** font shared between room answer labels */
    private BitmapFont promptFont;

    /** larger font used exclusively for the question panel */
    private BitmapFont questionFont;

    /** font used for score text and feedback messages */
    private BitmapFont hudFont;

    /** full-screen tinted overlay with result text; visible only during FEEDBACK */
    private IRenderable feedbackOverlay;

    /** full-screen brightness overlay; must be queued last so it composites over all other elements */
    private BrightnessOverlay brightnessOverlay;

    /** score counter rendered in the top-right corner */
    private IRenderable scoreDisplay;

    /** three health squares rendered in the top-left corner */
    private IRenderable healthDisplay;

    /** current level/difficulty label rendered at the centre of the HUD strip */
    private IRenderable levelDisplay;

    /** solid top bar separating HUD elements from the playfield */
    private HudStrip hudStrip;

    /** vertical offset from the strip baseline to the score text baseline, in pixels */
    private static final float HUD_SCORE_BASELINE_OFFSET = 31f;

    /** vertical offset from the strip baseline to the health pip top edge, in pixels */
    private static final float HUD_HEALTH_BASELINE_OFFSET = 14f;

    /** set to true to draw zone-boundary and corridor-centreline debug lines over the scene */
    private static final boolean SHOW_DEBUG_GRID = false;

    /** magenta for zone boundaries, cyan for screen midlines, orange for spawn */
    private static final Color DEBUG_ZONE_COLOUR  = new Color(1f, 0f, 1f, 0.9f);
    private static final Color DEBUG_CORR_COLOUR  = new Color(0f, 0.8f, 1f, 0.9f);
    private static final Color DEBUG_SPAWN_COLOUR = new Color(1f, 0.5f, 0f, 1f);

    /** debug overlay — null when SHOW_DEBUG_GRID is false */
    private IRenderable debugGridRenderable;

    /**
     * constructs the game scene with the scene key "game".
     */
    public GameScene() {
        this.name = "game";
    }

    /**
     * initialises the layout, player, input query, orchestrator, and room state for this scene.
     *
     * @param context the engine service context
     */
    @Override
    public void onEnter(SceneContext context) {
        // ensure the scene always starts unpaused — guards against a stale paused flag
        // left over if the player suspended and then returned to menu before playing again
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

        // initialise per-room entry state and cooldown timers
        this.playerInsideRoom   = new boolean[4];
        this.roomCooldownTimers = new float[4];
        this.lastKnownPhase     = null;
        this.phaseHoldTimer     = 0f;

        // cache room bounds once; MazeLayout is immutable so these are stable for the scene lifetime
        this.cachedRoomBounds = new float[4][];
        for (int i = 0; i < cachedRoomBounds.length; i++) {
            cachedRoomBounds[i] = layout.getRoomBounds(i);
        }

        int skeletonCount = getSkeletonCount(difficulty);

        // spawn one goblin at the centre of each corner room
        this.enemies = new ArrayList<>(4 + skeletonCount);
        for (float[] room : cachedRoomBounds) {
            float cx = room[0] + room[2] / 2f;
            float cy = room[1] + room[3] / 2f;
            HostileCharacter enemy = new Enemy(cx, cy);
            enemies.add(enemy);
            movementManager.registerMovable(enemy);
            collisionManager.registerMover(enemy);
        }

        // skeletons spawn at distinct corridor-only intersections and share the same
        // maze-wide patrol loop from their respective start points.
        int[] skeletonStartIndices = chooseSkeletonStartIndices(skeletonCount);
        for (int startIndex : skeletonStartIndices) {
            float[][] skeletonRoute = createSkeletonPatrolRoute(startIndex);
            HostileCharacter skeleton = new Skeleton(
                skeletonRoute[0][0],
                skeletonRoute[0][1],
                skeletonRoute
            );
            enemies.add(skeleton);
            movementManager.registerMovable(skeleton);
            collisionManager.registerMover(skeleton);
        }

        this.items = spawnItems(orchestrator);
        for (Item item : items) {
            collisionManager.registerItem(item);
        }

        // source fonts from the font manager
        IFontManager fontManager = context.get(IFontManager.class);
        this.promptFont   = fontManager.getLightTextFont(28);
        this.questionFont = fontManager.getLightTextFont(36);
        this.hudFont      = fontManager.getLightTextFont(22);

        // allocate per-room answer caches before the loop so closures can capture the array references
        this.roomAnswerTexts   = new String[4];
        this.roomAnswerLayouts = new GlyphLayout[4];
        for (int i = 0; i < 4; i++) {
            roomAnswerLayouts[i] = new GlyphLayout();
        }

        // create the brightness overlay early so submitRenderable never sees a null reference
        this.brightnessOverlay = new BrightnessOverlay();
        this.hudStrip = new HudStrip();

        // build one renderable per answer room — grey outline + centred answer label
        this.roomRenderables = new ArrayList<>(4);
        List<float[]> allRooms = layout.getAllRoomBounds();
        // capture orchestrator and font as effectively-final locals for lambda use
        final ILevelOrchestrator orch               = orchestrator;
        final BitmapFont         roomFont           = promptFont;
        // capture array references; refreshRoomAnswerCache() populates the elements later
        final String[]           capturedAnswerTexts   = roomAnswerTexts;
        final GlyphLayout[]      capturedAnswerLayouts = roomAnswerLayouts;

        for (int i = 0; i < allRooms.size(); i++) {
            final int    roomIndex = i;
            final float[] rect     = allRooms.get(i);
            // transform satisfies ITransformable but is not used for rendering;
            // the rect array drives the draw call directly
            final Transform2D roomTransform = new Transform2D(rect[0], rect[1], rect[2], rect[3]);
            roomRenderables.add(new IRenderable() {
                @Override public String     getAssetPath() { return null; }
                @Override public ITransform getTransform() { return roomTransform; }

                @Override
                public void render(IDrawContext ctx) {
                    GdxDrawContext gdx = (GdxDrawContext) ctx;
                    // layout and text were pre-computed in refreshRoomAnswerCache — no allocation
                    GlyphLayout gl = capturedAnswerLayouts[roomIndex];
                    gdx.drawFont(roomFont, capturedAnswerTexts[roomIndex],
                        rect[0] + (rect[2] - gl.width)  / 2f,
                        rect[1] + rect[3] / 2f + gl.height / 2f);
                }
            });
        }

        // populate the room answer cache for the initial question
        refreshRoomAnswerCache(orchestrator);

        // question panel — begins its slide animation immediately (scene starts at QUESTION_INTRO)
        this.questionPanel = new QuestionPanel(questionFont);
        questionPanel.beginIntro(orchestrator.getCurrentQuestion().getPrompt());
        // prevent onPhaseChanged from firing beginIntro a second time on the first update tick
        this.lastKnownPhase = orchestrator.getPhase();
        this.phaseHoldTimer = QUESTION_INTRO_HOLD_SECONDS; // initialise hold for the initial QUESTION_INTRO

        // capture hudFont as a final local so closures below are independent of the field lifecycle
        final BitmapFont capturedHudFont = hudFont;
        final Difficulty capturedDifficulty = difficulty;

        // pre-compute fixed feedback layouts once — feedback strings never change
        final GlyphLayout correctLayout = new GlyphLayout(capturedHudFont, "CORRECT!");
        final GlyphLayout wrongLayout   = new GlyphLayout(capturedHudFont, "WRONG!");
        // feedback overlay: playfield-height green/red tint + result text, shown only during FEEDBACK
        final Color overlayColour = new Color();
        this.feedbackOverlay = new IRenderable() {
            private final Transform2D t = new Transform2D(
                0f,
                0f,
                Settings.getWindowWidth(),
                HudStrip.PLAYFIELD_HEIGHT
            );
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                // show for FEEDBACK, LEVEL_COMPLETE and GAME_OVER — terminal phases
                // inherit the same overlay so the result is visible before the scene transition
                RoundPhase p = orch.getPhase();
                boolean enemyDeath = p == RoundPhase.GAME_OVER && orch.wasLastDamageFromEnemy();
                if (p != RoundPhase.FEEDBACK
                        && p != RoundPhase.LEVEL_COMPLETE
                        && p != RoundPhase.GAME_OVER) return;
                if (enemyDeath) return;
                boolean correct = orch.isLastAnswerCorrect();
                // reuse pre-allocated colour and layout objects — no allocation in this hot path
                overlayColour.set(correct ? OVERLAY_CORRECT : OVERLAY_WRONG);
                GdxDrawContext gdx    = (GdxDrawContext) ctx;
                gdx.drawTintedQuad(
                    overlayColour,
                    0f,
                    0f,
                    Settings.getWindowWidth(),
                    HudStrip.PLAYFIELD_HEIGHT
                );
                String      msg    = correct ? "CORRECT!" : "WRONG!";
                GlyphLayout layout = correct ? correctLayout : wrongLayout;
                gdx.drawFont(
                    capturedHudFont,
                    msg,
                    Settings.getWindowWidth() / 2f - layout.width / 2f,
                    HudStrip.PLAYFIELD_HEIGHT / 2f + 60f
                );
            }
        };

        // score display: top-right corner
        this.scoreDisplay = new IRenderable() {
            private static final float RIGHT_PADDING = 18f;
            private final float BASELINE_Y = HudStrip.STRIP_Y + HUD_SCORE_BASELINE_OFFSET;
            private final Transform2D t = new Transform2D(
                Settings.getWindowWidth() - RIGHT_PADDING,
                BASELINE_Y,
                0f,
                0f
            );
            private final GlyphLayout layout = new GlyphLayout();
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                String text = "Score: " + orch.getScore();
                layout.setText(capturedHudFont, text);
                ((GdxDrawContext) ctx).drawFont(
                    capturedHudFont,
                    text,
                    Settings.getWindowWidth() - RIGHT_PADDING - layout.width,
                    BASELINE_Y
                );
            }
        };

        // health display: top-left, "Health:" label followed by 3 heart icons from the sprite strip
        this.healthDisplay = new IRenderable() {
            private static final String HEART_ASSET  = "Heart.png";
            private static final String LABEL        = "Health:";
            private static final int    FRAME_SIZE   = 16;   // px — each frame in the strip
            private static final int    FRAME_FULL   = 4;    // frame index for active heart
            private static final int    FRAME_EMPTY  = 0;    // frame index for lost heart
            private static final float  SQ           = 20f;
            private static final float  GAP          = 6f;
            private static final float  LABEL_MARGIN = 8f;   // gap between label and first heart
            private static final float  BASE_X       = 18f;
            private final float       BASE_Y      = HudStrip.STRIP_Y + HUD_HEALTH_BASELINE_OFFSET;
            private final float       LABEL_Y     = HudStrip.STRIP_Y + HUD_SCORE_BASELINE_OFFSET;
            // pre-measure label width so hearts are always flush against it
            private final GlyphLayout labelLayout = new GlyphLayout(capturedHudFont, LABEL);
            private final Transform2D t           = new Transform2D(BASE_X, BASE_Y, 0f, 0f);
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                GdxDrawContext gdx      = (GdxDrawContext) ctx;
                int            health   = orch.getHealth();
                float          heartsX  = BASE_X + labelLayout.width + LABEL_MARGIN;
                gdx.drawFont(capturedHudFont, LABEL, BASE_X, LABEL_Y);
                for (int i = 0; i < 3; i++) {
                    float x     = heartsX + i * (SQ + GAP);
                    int   frame = (i < health) ? FRAME_FULL : FRAME_EMPTY;
                    int   srcX  = frame * FRAME_SIZE;
                    // srcY=0 — strip height equals one frame; libgdx y-down so top is 0
                    gdx.drawTextureRegion(HEART_ASSET, srcX, 0, FRAME_SIZE, FRAME_SIZE,
                                         x, BASE_Y, SQ, SQ, false);
                }
            }
        };

        // level display: centred in the HUD strip
        this.levelDisplay = new IRenderable() {
            private final float BASELINE_Y = HudStrip.STRIP_Y + HUD_SCORE_BASELINE_OFFSET;
            private final float CENTRE_X   = Settings.getWindowWidth() / 2f;
            private final Transform2D t = new Transform2D(CENTRE_X, BASELINE_Y, 0f, 0f);
            private final GlyphLayout layout = new GlyphLayout();
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                String text = "Level " + getLevelNumber(capturedDifficulty)
                    + " - " + formatDifficultyLabel(capturedDifficulty);
                layout.setText(capturedHudFont, text);
                ((GdxDrawContext) ctx).drawFont(
                    capturedHudFont,
                    text,
                    CENTRE_X - layout.width / 2f,
                    BASELINE_Y
                );
            }
        };

        // debug grid — zone boundary lines and corridor centrelines drawn over everything
        if (SHOW_DEBUG_GRID) {
            // zone boundaries derived from room bounds — left/right edges and top/bottom edges
            final float zoneLeft  = cachedRoomBounds[0][0] + cachedRoomBounds[0][2]; // right edge of TL room
            final float zoneRight = cachedRoomBounds[1][0];                           // left edge of TR room
            final float zoneTop   = cachedRoomBounds[0][1];                           // bottom edge of top rooms
            final float zoneBot   = cachedRoomBounds[2][1] + cachedRoomBounds[2][3];  // top edge of bottom rooms
            final float[] sp      = layout.getSpawnPoint();
            this.debugGridRenderable = new IRenderable() {
                private final Transform2D t = new Transform2D(0f, 0f, 0f, 0f);
                @Override public String     getAssetPath() { return null; }
                @Override public ITransform getTransform() { return t; }

                @Override
                public void render(IDrawContext ctx) {
                    GdxDrawContext gdx = (GdxDrawContext) ctx;
                    // magenta zone boundary lines
                    gdx.rect(
                        DEBUG_ZONE_COLOUR,
                        zoneLeft - 1f,
                        0f,
                        2f,
                        HudStrip.PLAYFIELD_HEIGHT,
                        true
                    );
                    gdx.rect(
                        DEBUG_ZONE_COLOUR,
                        zoneRight - 1f,
                        0f,
                        2f,
                        HudStrip.PLAYFIELD_HEIGHT,
                        true
                    );
                    gdx.rect(DEBUG_ZONE_COLOUR, 0f, zoneTop - 1f, Settings.getWindowWidth(), 2f, true);
                    gdx.rect(DEBUG_ZONE_COLOUR, 0f, zoneBot - 1f, Settings.getWindowWidth(), 2f, true);
                    // cyan playfield midlines
                    gdx.rect(
                        DEBUG_CORR_COLOUR,
                        Settings.getWindowWidth() / 2f - 1f,
                        0f,
                        2f,
                        HudStrip.PLAYFIELD_HEIGHT,
                        true
                    );
                    gdx.rect(
                        DEBUG_CORR_COLOUR,
                        0f,
                        HudStrip.PLAYFIELD_HEIGHT / 2f - 1f,
                        Settings.getWindowWidth(),
                        2f,
                        true
                    );
                    // orange spawn crosshair
                    gdx.rect(DEBUG_SPAWN_COLOUR, sp[0] - 4f, sp[1] - 4f, 8f, 8f, true);
                }
            };
        }
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

        // unregister the player from movement before clearing references
        movementManager.unregisterMovable(player);

        // unregister enemies from movement and collision before clearing references
        for (HostileCharacter enemy : enemies) {
            movementManager.unregisterMovable(enemy);
            collisionManager.unregisterMover(enemy);
        }
        for (Item item : items) {
            collisionManager.unregisterItem(item);
        }
        enemies = null;
        items = null;

        // unregister all collision participants before clearing references
        collisionManager.unregisterPlayer();
        for (WallCollidable wall : wallCollidables) {
            collisionManager.unregisterWall(wall);
        }

        playerInsideRoom   = null;
        roomCooldownTimers = null;
        cachedRoomBounds   = null;
        wallRenderables    = null;
        roomRenderables    = null;
        roomAnswerTexts    = null;
        roomAnswerLayouts  = null;
        lastKnownPhase     = null;
        questionPanel      = null;
        promptFont         = null;
        questionFont       = null;
        hudFont            = null;
        feedbackOverlay    = null;
        scoreDisplay       = null;
        healthDisplay      = null;
        levelDisplay       = null;
        hudStrip           = null;
        brightnessOverlay   = null;
        debugGridRenderable = null;

        layout          = null;
        player          = null;
        backgroundRenderable = null;
        wallCollidables = null;
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
        for (HostileCharacter enemy : enemies) {
            enemy.setVelocity(new float[]{ 0f, 0f });
        }
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
     * non-interactive phases freeze input and auto-advance after the hold timer expires.
     * when advance() is called the resulting phase transition is processed in the same
     * frame to avoid a one-frame lag on state-sensitive reactions (e.g. player spawn reset).
     * room entry is checked per-frame during the CHOOSING phase only.
     *
     * @param deltaTime elapsed seconds since the last frame
     * @param context   the engine service context
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        ILevelOrchestrator orchestrator = context.get(ILevelOrchestrator.class);
        IInputQuery inputQuery = context.get(IInputQuery.class);
        RoundPhase phase = orchestrator.getPhase();

        // detect phase change and react (reset player on ROUND_RESET, start hold timer)
        if (phase != lastKnownPhase) {
            onPhaseChanged(lastKnownPhase, phase, orchestrator);
            lastKnownPhase = phase;
        }

        // non-interactive phases: freeze input and auto-advance after hold timer
        if (phase != RoundPhase.CHOOSING) {
            if (phase == RoundPhase.GAME_OVER && orchestrator.wasLastDamageFromEnemy()) {
                context.changeScene("game-over");
                return;
            }
            // zeros velocity via phase lock inside player.update()
            player.update(deltaTime, inputQuery, phase);
            for (HostileCharacter enemy : enemies) {
                enemy.setVelocity(new float[]{ 0f, 0f });
            }
            // advance the panel slide during the QUESTION_INTRO hold
            if (phase == RoundPhase.QUESTION_INTRO) {
                questionPanel.update(deltaTime);
            }
            phaseHoldTimer -= deltaTime;
            if (phaseHoldTimer <= 0f) {
                if (isTerminalPhase(phase)) {
                    // level_complete or game_over — exit to the appropriate scene
                    context.changeScene(phase == RoundPhase.LEVEL_COMPLETE ? "level-complete" : "game-over");
                } else {
                    orchestrator.advance();
                    // re-read and process the resulting phase in the same frame so
                    // reactions like player.resetToSpawn() are not delayed by one tick
                    RoundPhase newPhase = orchestrator.getPhase();
                    if (newPhase != lastKnownPhase) {
                        onPhaseChanged(lastKnownPhase, newPhase, orchestrator);
                        lastKnownPhase = newPhase;
                    }
                }
            }
            return;
        }

        // choosing phase: check for pause request before processing player input
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            context.suspendScene("pause");
            return;
        }

        // resolve player input then check room entry
        player.update(deltaTime, inputQuery, phase);
        for (HostileCharacter enemy : enemies) {
            enemy.update(deltaTime, player.getTransform(), hasLineOfSightToPlayer(enemy));
        }
        checkRoomEntry(deltaTime, orchestrator);
    }

    /**
     * submits all renderables to the queue in painter's order:
     * room outlines and answer labels → player → question panel → hud strip → score → health →
     * level → feedback overlay
     * feedback overlay → debug grid (when enabled) → brightness overlay.
     *
     * the brightness overlay is queued last so it dims the entire composited frame
     * uniformly per the user's brightness setting.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        if (roomRenderables == null) return; // scene has already exited
        renderQueue.queue(backgroundRenderable);
        for (IRenderable wall : wallRenderables) {
            renderQueue.queue(wall);
        }
        for (IRenderable room : roomRenderables) {
            renderQueue.queue(room);
        }
        for (HostileCharacter enemy : enemies) {
            renderQueue.queue(enemy);
        }
        for (Item item : items) {
            if (item.isActive()) {
                renderQueue.queue(item);
            }
        }
        renderQueue.queue(player);
        renderQueue.queue(questionPanel);   // slides above the world during QUESTION_INTRO
        renderQueue.queue(hudStrip);
        renderQueue.queue(scoreDisplay);
        renderQueue.queue(healthDisplay);
        renderQueue.queue(levelDisplay);
        renderQueue.queue(feedbackOverlay);
        if (SHOW_DEBUG_GRID && debugGridRenderable != null) {
            renderQueue.queue(debugGridRenderable);
        }
        // skip the brightness overlay when suspended — the active overlay scene applies its own
        // dim, and stacking both would produce a double-darkening at non-maximum brightness
        if (!paused) {
            renderQueue.queue(brightnessOverlay);
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * called whenever the round phase changes. resets the player to spawn on ROUND_RESET
     * and starts the hold timer for non-interactive phases.
     *
     * @param from         the previous phase (null on first frame)
     * @param to           the new phase
     * @param orchestrator the level orchestrator for reading questions and room assignments
     */
    private void onPhaseChanged(RoundPhase from, RoundPhase to, ILevelOrchestrator orchestrator) {
        if (to == RoundPhase.ROUND_RESET) {
            // new question loading — return player to spawn and clear room state
            player.resetToSpawn(layout.getSpawnPoint());
            for (HostileCharacter enemy : enemies) {
                enemy.resetToSpawn();
            }
            Arrays.fill(playerInsideRoom, false);
            Arrays.fill(roomCooldownTimers, 0f);
        }
        if (to == RoundPhase.QUESTION_INTRO) {
            // update the room answer cache for the new question, then slide the panel
            refreshRoomAnswerCache(orchestrator);
            questionPanel.beginIntro(orchestrator.getCurrentQuestion().getPrompt());
        }
        if (to != RoundPhase.CHOOSING) {
            // terminal phases share the feedback hold so the overlay is visible before transitioning
            if (to == RoundPhase.FEEDBACK || isTerminalPhase(to)) {
                phaseHoldTimer = FEEDBACK_HOLD_SECONDS;
            } else if (to == RoundPhase.QUESTION_INTRO) {
                phaseHoldTimer = QUESTION_INTRO_HOLD_SECONDS;
            } else {
                phaseHoldTimer = ROUND_RESET_HOLD_SECONDS;
            }
        }
    }

    /**
     * checks each answer room for player overlap. on entry fires submitRoomChoice(), on
     * exit starts the re-entry cooldown. only fires during the CHOOSING phase.
     *
     * when the wrong room is entered, playerInsideRoom[i] is intentionally left as true
     * until the player physically exits; this means re-entry cannot fire again without
     * the player leaving first, which is the correct guard for the cooldown path.
     *
     * @param deltaTime    seconds elapsed since the previous frame, used to tick cooldowns
     * @param orchestrator the level orchestrator used to submit the player's room choice
     */
    private void checkRoomEntry(float deltaTime, ILevelOrchestrator orchestrator) {
        IBounds playerBounds = player.getBounds();

        for (int i = 0; i < playerInsideRoom.length; i++) {
            // tick the cooldown down; clamp to zero
            if (roomCooldownTimers[i] > 0f) {
                roomCooldownTimers[i] = Math.max(0f, roomCooldownTimers[i] - deltaTime);
            }

            boolean overlapping = overlapsRoom(playerBounds, cachedRoomBounds[i]);

            if (overlapping && !playerInsideRoom[i] && roomCooldownTimers[i] <= 0f) {
                // player just entered a room with no active cooldown
                playerInsideRoom[i] = true;
                int healthBefore = orchestrator.getHealth();
                orchestrator.submitRoomChoice(i);
                if (orchestrator.getHealth() < healthBefore) {
                    player.triggerDamageAnimation();
                }
                // phase has changed after submitRoomChoice — stop checking further rooms
                break;
            } else if (!overlapping && playerInsideRoom[i]) {
                // player just exited — start cooldown so re-entry doesn't instant-trigger
                playerInsideRoom[i] = false;
                roomCooldownTimers[i] = ROOM_COOLDOWN_SECONDS;
            }
        }
    }

    /**
     * returns true when the straight line from enemy centre to player centre is not blocked
     * by any wall rectangle in the current maze.
     *
     * @param enemy the enemy whose vision ray is being tested
     * @return true if no wall intersects the enemy-to-player segment
     */
    private boolean hasLineOfSightToPlayer(HostileCharacter enemy) {
        ITransform enemyTransform = enemy.getTransform();
        ITransform playerTransform = player.getTransform();

        float ex = enemyTransform.getPosition(0) + enemyTransform.getSize(0) / 2f;
        float ey = enemyTransform.getPosition(1) + enemyTransform.getSize(1) / 2f;
        float px = playerTransform.getPosition(0) + playerTransform.getSize(0) / 2f;
        float py = playerTransform.getPosition(1) + playerTransform.getSize(1) / 2f;

        for (WallCollidable wall : wallCollidables) {
            if (segmentIntersectsRect(ex, ey, px, py, wall.getBounds())) {
                return false;
            }
        }
        return true;
    }

    /**
     * builds a map-wide patrol loop for the roaming skeleton and rotates it so the
     * skeleton starts at a random corridor intersection.
     *
     * the route uses corridor-centre intersections only, so the chosen spawn point
     * never overlaps the goblin room-centre spawn positions.
     *
     * @return ordered loop of [x, y] waypoints beginning at a random intersection
     */
    private float[][] createSkeletonPatrolRoute(int startIndex) {
        List<float[]> pathways = layout.getPathwayBounds();
        if (pathways.size() < 8) {
            throw new IllegalStateException(
                "MazeLayout must expose at least eight pathway rectangles for skeleton patrol routing");
        }

        float topY = centreY(pathways.get(0));
        float bottomY = centreY(pathways.get(1));
        float leftX = centreX(pathways.get(2));
        float rightX = centreX(pathways.get(3));
        float middleY = centreY(pathways.get(7));
        float centreX = centreX(pathways.get(4));

        float[][] route = new float[][]{
            new float[]{ leftX, topY },
            new float[]{ centreX, topY },
            new float[]{ rightX, topY },
            new float[]{ rightX, middleY },
            new float[]{ rightX, bottomY },
            new float[]{ centreX, bottomY },
            new float[]{ leftX, bottomY },
            new float[]{ leftX, middleY }
        };

        return rotateRoute(route, startIndex);
    }

    private int[] chooseSkeletonStartIndices(int skeletonCount) {
        int[] spawnableStartIndices = new int[]{ 1, 3, 5, 7 };
        if (skeletonCount < 0 || skeletonCount > spawnableStartIndices.length) {
            throw new IllegalArgumentException(
                "skeletonCount must be in [0, " + spawnableStartIndices.length + "], got: " + skeletonCount);
        }

        int[] chosen = spawnableStartIndices.clone();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = chosen.length - 1; i > 0; i--) {
            int swapIndex = rng.nextInt(i + 1);
            int tmp = chosen[i];
            chosen[i] = chosen[swapIndex];
            chosen[swapIndex] = tmp;
        }

        return Arrays.copyOf(chosen, skeletonCount);
    }

    private static float centreX(float[] rect) {
        return rect[0] + rect[2] / 2f;
    }

    private static float centreY(float[] rect) {
        return rect[1] + rect[3] / 2f;
    }

    private static float[][] rotateRoute(float[][] route, int startIndex) {
        float[][] rotated = new float[route.length][2];
        for (int i = 0; i < route.length; i++) {
            float[] point = route[(startIndex + i) % route.length];
            rotated[i][0] = point[0];
            rotated[i][1] = point[1];
        }
        return rotated;
    }

    private static int getSkeletonCount(Difficulty difficulty) {
        if (difficulty == Difficulty.HARD) {
            return 3;
        }
        if (difficulty == Difficulty.MEDIUM) {
            return 2;
        }
        return 1;
    }

    private static int getLevelNumber(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) {
            return 2;
        }
        if (difficulty == Difficulty.HARD) {
            return 3;
        }
        return 1;
    }

    private static String formatDifficultyLabel(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) {
            return "Medium";
        }
        if (difficulty == Difficulty.HARD) {
            return "Hard";
        }
        return "Easy";
    }

    /**
     * returns true when the closed segment p0->p1 intersects the given axis-aligned rectangle.
     *
     * uses the Liang-Barsky slab test, which is stable for vertical and horizontal segments.
     *
     * @param x0 start x
     * @param y0 start y
     * @param x1 end x
     * @param y1 end y
     * @param bounds rectangle bounds
     * @return true if the segment enters or touches the rectangle
     */
    private boolean segmentIntersectsRect(float x0, float y0, float x1, float y1, IBounds bounds) {
        float[] min = bounds.getMinPosition();
        float[] ext = bounds.getExtent();

        float minX = min[0];
        float minY = min[1];
        float maxX = min[0] + ext[0];
        float maxY = min[1] + ext[1];

        float dx = x1 - x0;
        float dy = y1 - y0;
        clipWindow[0] = 0f;
        clipWindow[1] = 1f;

        if (!clipTest(-dx, x0 - minX)) {
            return false;
        }
        if (!clipTest(dx, maxX - x0)) {
            return false;
        }
        if (!clipTest(-dy, y0 - minY)) {
            return false;
        }
        return clipTest(dy, maxY - y0);
    }

    /** reusable scratch window for Liang-Barsky clipping */
    private final float[] clipWindow = new float[2];

    /**
     * one Liang-Barsky clipping step that narrows the parametric segment window.
     *
     * @param p line direction term for one slab
     * @param q offset term for one slab
     * @return true if the segment still intersects the clip volume after this slab
     */
    private boolean clipTest(float p, float q) {
        float tMin = clipWindow[0];
        float tMax = clipWindow[1];

        if (p == 0f) {
            if (q < 0f) {
                return false;
            }
            clipWindow[0] = tMin;
            clipWindow[1] = tMax;
            return true;
        }

        float r = q / p;
        if (p < 0f) {
            if (r > tMax) {
                return false;
            }
            if (r > tMin) {
                tMin = r;
            }
        } else {
            if (r < tMin) {
                return false;
            }
            if (r < tMax) {
                tMax = r;
            }
        }

        clipWindow[0] = tMin;
        clipWindow[1] = tMax;
        return true;
    }

    /**
     * spawns a random set of heart pickups within corridor/pathway rectangles.
     *
     * @return newly created active item list
     */
    private List<Item> spawnItems(ILevelOrchestrator orchestrator) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<float[]> pathways = layout.getPathwayBounds();
        List<Item> spawnedItems = new ArrayList<>();
        int heartCount = rng.nextInt(1, 4);

        for (int i = 0; i < heartCount; i++) {
            Heart heart = trySpawnHeart(pathways, spawnedItems, rng, orchestrator);
            if (heart != null) {
                spawnedItems.add(heart);
            }
        }

        return spawnedItems;
    }

    /**
     * attempts to place one heart in a random pathway while keeping it away from existing hearts.
     *
     * @param pathways current walkway rectangles
     * @param existing already spawned items
     * @param rng random source
     * @return a new heart, or null if placement failed after several attempts
     */
    private Heart trySpawnHeart(List<float[]> pathways, List<Item> existing, ThreadLocalRandom rng,
                                ILevelOrchestrator orchestrator) {
        final int maxAttempts = 24;
        final float padding = Heart.SIZE / 2f + 6f;
        final float minSpacing = Heart.SIZE * 1.6f;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            float[] rect = chooseRandomPathway(pathways, rng);
            if (rect == null || rect[2] <= padding * 2f || rect[3] <= padding * 2f) {
                continue;
            }

            float cx = rng.nextFloat(rect[0] + padding, rect[0] + rect[2] - padding);
            float cy = rng.nextFloat(rect[1] + padding, rect[1] + rect[3] - padding);

            boolean overlapsExisting = false;
            for (Item item : existing) {
                float ix = item.getTransform().getPosition(0) + item.getTransform().getSize(0) / 2f;
                float iy = item.getTransform().getPosition(1) + item.getTransform().getSize(1) / 2f;
                float dx = cx - ix;
                float dy = cy - iy;
                if (dx * dx + dy * dy < minSpacing * minSpacing) {
                    overlapsExisting = true;
                    break;
                }
            }

            if (!overlapsExisting) {
                return new Heart(cx, cy, orchestrator);
            }
        }

        return null;
    }

    /**
     * chooses one pathway rectangle with probability proportional to its area.
     *
     * @param pathways candidate pathway rectangles
     * @param rng random source
     * @return chosen rectangle, or null if none are available
     */
    private float[] chooseRandomPathway(List<float[]> pathways, ThreadLocalRandom rng) {
        if (pathways.isEmpty()) {
            return null;
        }

        float totalArea = 0f;
        for (float[] rect : pathways) {
            totalArea += rect[2] * rect[3];
        }

        float pick = rng.nextFloat() * totalArea;
        for (float[] rect : pathways) {
            pick -= rect[2] * rect[3];
            if (pick <= 0f) {
                return rect;
            }
        }

        return pathways.get(pathways.size() - 1);
    }

    /**
     * returns true if the player's AABB overlaps the given room rectangle.
     *
     * @param playerBounds the player's current AABB
     * @param room         a four-element [x, y, w, h] room rectangle
     * @return true if the two AABBs overlap on both axes
     */
    private boolean overlapsRoom(IBounds playerBounds, float[] room) {
        // compute the centre trigger zone — smaller than the full room to require deliberate entry
        float triggerX = room[0] + (room[2] - TRIGGER_WIDTH)  / 2f;
        float triggerY = room[1] + (room[3] - TRIGGER_HEIGHT) / 2f;

        float[] pMin = playerBounds.getMinPosition();
        float[] pExt = playerBounds.getExtent();
        float pMaxX = pMin[0] + pExt[0];
        float pMaxY = pMin[1] + pExt[1];
        return pMaxX > triggerX                 && pMin[0] < triggerX + TRIGGER_WIDTH
            && pMaxY > triggerY                 && pMin[1] < triggerY + TRIGGER_HEIGHT;
    }

    /**
     * returns true for phases that have no valid advance() transition.
     *
     * @param phase the phase to test
     * @return true if the phase is LEVEL_COMPLETE or GAME_OVER
     */
    private boolean isTerminalPhase(RoundPhase phase) {
        return phase == RoundPhase.LEVEL_COMPLETE || phase == RoundPhase.GAME_OVER;
    }

    /**
     * creates a filled wall renderable from a rectangle in layout space.
     *
     * @param rect the wall rectangle as [x, y, w, h]
     * @return a renderable that draws the wall as a continuous solid mass
     */
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
                    WALL_FILL_COLOUR, stableRect[0], stableRect[1], stableRect[2], stableRect[3], true);
            }
        };
    }

    /**
     * creates the lighter background quad that sits under the maze geometry.
     *
     * @return a renderable that fills the whole gameplay viewport
     */
    private IRenderable createBackgroundRenderable() {
        final Transform2D transform = new Transform2D(
            0f,
            0f,
            Settings.getWindowWidth(),
            HudStrip.PLAYFIELD_HEIGHT
        );
        return new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).drawTintedQuad(
                    SCENE_BG_COLOUR,
                    0f,
                    0f,
                    Settings.getWindowWidth(),
                    HudStrip.PLAYFIELD_HEIGHT
                );
            }
        };
    }

    /**
     * reads the current room assignment from the orchestrator and pre-computes the
     * answer text and glyph layout for each room.
     *
     * called once in onEnter and once each time QUESTION_INTRO begins so that
     * room renderables never allocate during their hot render path.
     *
     * @param orchestrator the level orchestrator used to read the current room assignment
     */
    private void refreshRoomAnswerCache(ILevelOrchestrator orchestrator) {
        for (int i = 0; i < 4; i++) {
            roomAnswerTexts[i] = String.valueOf(orchestrator.getRoomAssignment().getAnswerForRoom(i));
            // setText() updates the layout in-place — no new GlyphLayout allocation
            roomAnswerLayouts[i].setText(promptFont, roomAnswerTexts[i]);
        }
    }
}

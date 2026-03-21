package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.entities.QuestionPanel;
import com.p1_7.game.gameplay.Difficulty;
import com.p1_7.game.gameplay.ILevelOrchestrator;
import com.p1_7.game.gameplay.MazeCollisionManager;
import com.p1_7.game.gameplay.MazeLayout;
import com.p1_7.game.gameplay.Player;
import com.p1_7.game.gameplay.RoundPhase;
import com.p1_7.game.gameplay.WallCollidable;
import com.p1_7.game.managers.GameMovementManager;
import com.p1_7.game.managers.IFontManager;
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

    /** hold time in seconds for non-interactive phases (QUESTION_INTRO and ROUND_RESET) */
    private static final float PHASE_HOLD_SECONDS = 1.0f;

    /** hold time in seconds for the FEEDBACK phase — longer to allow the player to read the result */
    private static final float FEEDBACK_HOLD_SECONDS = 2.0f;

    /** pre-allocated overlay colours — reused every frame to avoid per-frame allocation */
    private static final Color OVERLAY_CORRECT    = new Color(0f, 0.7f, 0f, 0.35f);
    private static final Color OVERLAY_WRONG      = new Color(0.8f, 0f, 0f, 0.35f);
    private static final Color HEALTH_LOST_COLOUR = new Color(0.3f, 0.3f, 0.3f, 1f);

    /** the fixed spatial layout providing spawn point, room bounds, and wall bounds */
    private MazeLayout layout;

    /** the player entity — created at scene entry, released on exit */
    private Player player;

    /** input query for reading action states each frame */
    private IInputQuery inputQuery;

    /** collision manager sourced from the engine service context */
    private MazeCollisionManager collisionManager;

    /** movement manager sourced from the engine service context */
    private GameMovementManager movementManager;

    /** wall collidables registered with the collision manager for this scene */
    private List<WallCollidable> wallCollidables;

    /** level orchestrator sourced from the engine service context */
    private ILevelOrchestrator orchestrator;

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

    /** one IRenderable per answer room — renders a grey outline and answer label each frame */
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

    /** font shared between the question panel and room answer labels */
    private BitmapFont promptFont;

    /** font used for score text and feedback messages */
    private BitmapFont hudFont;

    /** full-screen tinted overlay with result text; visible only during FEEDBACK */
    private IRenderable feedbackOverlay;

    /** score counter rendered in the top-right corner */
    private IRenderable scoreDisplay;

    /** three health squares rendered in the top-left corner */
    private IRenderable healthDisplay;

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
        this.layout   = MazeLayout.createDefault();
        float[] spawn = layout.getSpawnPoint();
        this.player   = new Player(spawn[0], spawn[1]);
        this.inputQuery = context.get(IInputQuery.class);

        // wire movement manager and register the player for position integration
        this.movementManager = context.get(GameMovementManager.class);
        movementManager.registerMovable(player);

        // wire collision manager and register the player and all walls
        this.collisionManager = context.get(MazeCollisionManager.class);
        this.wallCollidables  = new ArrayList<>();
        collisionManager.registerPlayer(player);
        for (float[] rect : layout.getWallBounds()) {
            WallCollidable wall = new WallCollidable(rect);
            wallCollidables.add(wall);
            collisionManager.registerWall(wall);
        }

        // wire level orchestrator and start the session at easy difficulty
        this.orchestrator = context.get(ILevelOrchestrator.class);
        orchestrator.startLevel(Difficulty.EASY);

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

        // source fonts from the font manager
        IFontManager fontManager = context.get(IFontManager.class);
        this.promptFont = fontManager.getPromptFont();
        this.hudFont    = fontManager.getDarkTextFont(22);

        // allocate per-room answer caches before the loop so closures can capture the array references
        this.roomAnswerTexts   = new String[4];
        this.roomAnswerLayouts = new GlyphLayout[4];
        for (int i = 0; i < 4; i++) {
            roomAnswerLayouts[i] = new GlyphLayout();
        }

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
                    // grey outline marking the room boundary
                    gdx.rect(Color.GRAY, rect[0], rect[1], rect[2], rect[3], false);
                    // layout and text were pre-computed in refreshRoomAnswerCache — no allocation
                    GlyphLayout gl = capturedAnswerLayouts[roomIndex];
                    gdx.drawFont(roomFont, capturedAnswerTexts[roomIndex],
                        rect[0] + (rect[2] - gl.width)  / 2f,
                        rect[1] + rect[3] / 2f + gl.height / 2f);
                }
            });
        }

        // populate the room answer cache for the initial question
        refreshRoomAnswerCache();

        // question panel — begins its slide animation immediately (scene starts at QUESTION_INTRO)
        this.questionPanel = new QuestionPanel(promptFont);
        questionPanel.beginIntro(orchestrator.getCurrentQuestion().getPrompt());
        // prevent onPhaseChanged from firing beginIntro a second time on the first update tick
        this.lastKnownPhase = orchestrator.getPhase();

        // capture hudFont as a final local so closures below are independent of the field lifecycle
        final BitmapFont capturedHudFont = hudFont;

        // pre-compute fixed feedback layouts once — "CORRECT!" and "WRONG!" never change
        final GlyphLayout correctLayout = new GlyphLayout(capturedHudFont, "CORRECT!");
        final GlyphLayout wrongLayout   = new GlyphLayout(capturedHudFont, "WRONG!");

        // feedback overlay: full-screen green/red tint + result text, shown only during FEEDBACK
        final Color overlayColour = new Color();
        this.feedbackOverlay = new IRenderable() {
            private final Transform2D t = new Transform2D(0f, 0f, 1280f, 720f);
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                if (orch.getPhase() != RoundPhase.FEEDBACK) return;
                boolean correct = orch.isLastAnswerCorrect();
                // reuse pre-allocated colour and layout objects — no allocation in this hot path
                overlayColour.set(correct ? OVERLAY_CORRECT : OVERLAY_WRONG);
                GdxDrawContext gdx    = (GdxDrawContext) ctx;
                gdx.drawTintedQuad(overlayColour, 0f, 0f, 1280f, 720f);
                String      msg    = correct ? "CORRECT!" : "WRONG!";
                GlyphLayout layout = correct ? correctLayout : wrongLayout;
                gdx.drawFont(capturedHudFont, msg, 640f - layout.width / 2f, 400f);
            }
        };

        // score display: top-right corner
        this.scoreDisplay = new IRenderable() {
            private final Transform2D t = new Transform2D(1100f, 680f, 0f, 0f);
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                String text = "Score: " + orch.getScore();
                ((GdxDrawContext) ctx).drawFont(capturedHudFont, text, 1100f, 700f);
            }
        };

        // health display: top-left, 3 squares (filled red = remaining health, dark grey = lost)
        this.healthDisplay = new IRenderable() {
            private static final float SQ     = 20f;
            private static final float GAP    = 6f;
            private static final float BASE_X = 30f;
            private static final float BASE_Y = 682f;
            private final Transform2D t = new Transform2D(BASE_X, BASE_Y, 0f, 0f);
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                GdxDrawContext gdx    = (GdxDrawContext) ctx;
                int            health = orch.getHealth();
                for (int i = 0; i < 3; i++) {
                    float x = BASE_X + i * (SQ + GAP);
                    if (i < health) {
                        gdx.rect(Color.RED, x, BASE_Y, SQ, SQ, true);
                    } else {
                        // reuse static constant — no allocation
                        gdx.rect(HEALTH_LOST_COLOUR, x, BASE_Y, SQ, SQ, false);
                    }
                }
            }
        };
    }

    /**
     * releases all scene-owned references so they can be garbage collected.
     *
     * @param context the engine service context
     */
    @Override
    public void onExit(SceneContext context) {
        // unregister the player from movement before clearing references
        movementManager.unregisterMovable(player);

        // unregister all collision participants before clearing references
        collisionManager.unregisterPlayer();
        for (WallCollidable wall : wallCollidables) {
            collisionManager.unregisterWall(wall);
        }

        orchestrator       = null;
        playerInsideRoom   = null;
        roomCooldownTimers = null;
        cachedRoomBounds   = null;
        roomRenderables    = null;
        roomAnswerTexts    = null;
        roomAnswerLayouts  = null;
        lastKnownPhase     = null;
        questionPanel      = null;
        promptFont         = null;
        hudFont            = null;
        feedbackOverlay    = null;
        scoreDisplay       = null;
        healthDisplay      = null;

        layout           = null;
        player           = null;
        inputQuery       = null;
        movementManager  = null;
        collisionManager = null;
        wallCollidables  = null;
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
        RoundPhase phase = orchestrator.getPhase();

        // detect phase change and react (reset player on ROUND_RESET, start hold timer)
        if (phase != lastKnownPhase) {
            onPhaseChanged(lastKnownPhase, phase);
            lastKnownPhase = phase;
        }

        // non-interactive phases: freeze input and auto-advance after hold timer
        if (phase != RoundPhase.CHOOSING) {
            // zeros velocity via phase lock inside player.update()
            player.update(deltaTime, inputQuery, phase);
            // advance the panel slide during the QUESTION_INTRO hold
            if (phase == RoundPhase.QUESTION_INTRO) {
                questionPanel.update(deltaTime);
            }
            if (!isTerminalPhase(phase)) {
                phaseHoldTimer -= deltaTime;
                if (phaseHoldTimer <= 0f) {
                    orchestrator.advance();
                    // re-read and process the resulting phase in the same frame so
                    // reactions like player.resetToSpawn() are not delayed by one tick
                    RoundPhase newPhase = orchestrator.getPhase();
                    if (newPhase != lastKnownPhase) {
                        onPhaseChanged(lastKnownPhase, newPhase);
                        lastKnownPhase = newPhase;
                    }
                }
            }
            return;
        }

        // choosing phase: resolve player input then check room entry
        player.update(deltaTime, inputQuery, phase);
        checkRoomEntry(deltaTime);
    }

    /**
     * submits all renderables to the queue in painter's order:
     * room outlines and answer labels → player → question panel → score → health → feedback overlay.
     *
     * the feedback overlay is queued last so it composites over all other elements.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        if (roomRenderables == null) return; // scene has already exited
        for (IRenderable room : roomRenderables) {
            renderQueue.queue(room);
        }
        renderQueue.queue(player);
        renderQueue.queue(questionPanel);   // slides above the world during QUESTION_INTRO
        renderQueue.queue(scoreDisplay);
        renderQueue.queue(healthDisplay);
        renderQueue.queue(feedbackOverlay); // topmost — composites over all others
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * called whenever the round phase changes. resets the player to spawn on ROUND_RESET
     * and starts the hold timer for non-interactive phases.
     *
     * 'from' is unused here but retained for future HUD transitions in issue #100.
     *
     * @param from the previous phase (null on first frame)
     * @param to   the new phase
     */
    private void onPhaseChanged(RoundPhase from, RoundPhase to) {
        if (to == RoundPhase.ROUND_RESET) {
            // new question loading — return player to spawn and clear room state
            player.resetToSpawn(layout.getSpawnPoint());
            Arrays.fill(playerInsideRoom, false);
            Arrays.fill(roomCooldownTimers, 0f);
        }
        if (to == RoundPhase.QUESTION_INTRO) {
            // update the room answer cache for the new question, then slide the panel
            refreshRoomAnswerCache();
            questionPanel.beginIntro(orchestrator.getCurrentQuestion().getPrompt());
        }
        if (!isTerminalPhase(to) && to != RoundPhase.CHOOSING) {
            // FEEDBACK uses a longer hold so the player can read the result
            phaseHoldTimer = (to == RoundPhase.FEEDBACK) ? FEEDBACK_HOLD_SECONDS : PHASE_HOLD_SECONDS;
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
     * @param deltaTime seconds elapsed since the previous frame, used to tick cooldowns
     */
    private void checkRoomEntry(float deltaTime) {
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
                orchestrator.submitRoomChoice(i);
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
     * returns true if the player's AABB overlaps the given room rectangle.
     *
     * @param playerBounds the player's current AABB
     * @param room         a four-element [x, y, w, h] room rectangle
     * @return true if the two AABBs overlap on both axes
     */
    private boolean overlapsRoom(IBounds playerBounds, float[] room) {
        float[] pMin = playerBounds.getMinPosition();
        float[] pExt = playerBounds.getExtent();
        float pMaxX = pMin[0] + pExt[0];
        float pMaxY = pMin[1] + pExt[1];
        float rMaxX = room[0] + room[2];
        float rMaxY = room[1] + room[3];
        return pMaxX > room[0] && pMin[0] < rMaxX
            && pMaxY > room[1] && pMin[1] < rMaxY;
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
     * reads the current room assignment from the orchestrator and pre-computes the
     * answer text and glyph layout for each room.
     *
     * called once in onEnter and once each time QUESTION_INTRO begins so that
     * room renderables never allocate during their hot render path.
     */
    private void refreshRoomAnswerCache() {
        for (int i = 0; i < 4; i++) {
            roomAnswerTexts[i] = String.valueOf(orchestrator.getRoomAssignment().getAnswerForRoom(i));
            // setText() updates the layout in-place — no new GlyphLayout allocation
            roomAnswerLayouts[i].setText(promptFont, roomAnswerTexts[i]);
        }
    }
}

package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.List;

import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.gameplay.MazeCollisionManager;
import com.p1_7.game.gameplay.MazeLayout;
import com.p1_7.game.gameplay.Player;
import com.p1_7.game.gameplay.RoundPhase;
import com.p1_7.game.gameplay.WallCollidable;

/**
 * placeholder gameplay scene — minimal wiring to make the player visible
 * and exercisable at runtime pending full orchestration in issue #100.
 *
 * the round phase is hardcoded to CHOOSING until GameRound is wired in #100.
 * wall collision is handled by MazeCollisionManager, which runs after this
 * scene's update() each frame. movement will be delegated to MovementManager
 * in issue #112, at which point the direct player.move() call will be removed.
 */
public class GameScene extends Scene {

    /** the fixed spatial layout providing spawn point and wall bounds */
    private MazeLayout  layout;

    /** the player entity — created at scene entry, released on exit */
    private Player      player;

    /** input query for reading action states each frame */
    private IInputQuery inputQuery;

    /** collision manager sourced from the engine service context */
    private MazeCollisionManager collisionManager;

    /** wall collidables registered with the collision manager for this scene */
    private List<WallCollidable> wallCollidables;

    /**
     * constructs the game scene with the scene key "game".
     */
    public GameScene() {
        this.name = "game";
    }

    /**
     * initialises the layout, player, and input query for this scene.
     *
     * @param context the engine service context
     */
    @Override
    public void onEnter(SceneContext context) {
        this.layout     = MazeLayout.createDefault();
        float[] spawn   = layout.getSpawnPoint();
        this.player     = new Player(spawn[0], spawn[1]);
        this.inputQuery = context.get(IInputQuery.class);

        // wire collision manager and register the player and all walls
        this.collisionManager = context.get(MazeCollisionManager.class);
        this.wallCollidables  = new ArrayList<>();

        collisionManager.registerPlayer(player);
        for (float[] rect : layout.getWallBounds()) {
            WallCollidable wall = new WallCollidable(rect);
            wallCollidables.add(wall);
            collisionManager.registerWall(wall);
        }
    }

    /**
     * releases all scene-owned references so they can be garbage collected.
     *
     * @param context the engine service context
     */
    @Override
    public void onExit(SceneContext context) {
        // unregister all collision participants before clearing references
        collisionManager.unregisterPlayer();
        for (WallCollidable wall : wallCollidables) {
            collisionManager.unregisterWall(wall);
        }

        layout            = null;
        player            = null;
        inputQuery        = null;
        collisionManager  = null;
        wallCollidables   = null;
    }

    /**
     * resolves player input, then integrates velocity. wall collision is
     * corrected reactively by MazeCollisionManager after this method returns.
     *
     * @param deltaTime elapsed seconds since the last frame
     * @param context   the engine service context
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        // phase is hardcoded to CHOOSING until GameRound is wired in issue #100
        player.update(deltaTime, inputQuery, RoundPhase.CHOOSING);
        player.move(deltaTime);
    }

    /**
     * submits the player to the render queue for this frame.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(player);
    }
}

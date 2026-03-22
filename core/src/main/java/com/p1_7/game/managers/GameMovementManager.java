package com.p1_7.game.managers;

import com.p1_7.abstractengine.engine.IManager;
import com.p1_7.abstractengine.movement.MovementManager;
import com.p1_7.abstractengine.scene.SceneManager;

/**
 * concrete MovementManager that handles player position integration and
 * viewport clamping for the game's fixed 1280 × 720 screen.
 *
 * declaring SceneManager as a dependency ensures this manager's onUpdate()
 * runs after SceneManager.onUpdate() — which calls GameScene.update() and
 * sets the player's velocity — so position integration always uses the
 * velocity resolved for the current frame.
 */
public final class GameMovementManager extends MovementManager {

    /** screen width in pixels — used as the movement boundary upper bound */
    private static final float SCREEN_WIDTH  = 1280f;

    /** screen height in pixels — used as the movement boundary upper bound */
    private static final float SCREEN_HEIGHT = 720f;

    /**
     * configures viewport boundary clamping for the game's screen dimensions.
     */
    @Override
    protected void onInit() {
        setWorldBounds(new float[]{ 0f, 0f }, new float[]{ SCREEN_WIDTH, SCREEN_HEIGHT });
    }

    /**
     * declares SceneManager as a dependency so this manager's onUpdate() runs
     * after SceneManager.onUpdate() — which calls GameScene.update() and sets
     * the player's velocity — each frame.
     *
     * @return array containing SceneManager.class
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[]{ SceneManager.class };
    }
}

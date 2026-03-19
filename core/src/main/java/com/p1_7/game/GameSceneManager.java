package com.p1_7.game;

import com.p1_7.abstractengine.engine.IManager;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.render.RenderManager;
import com.p1_7.abstractengine.scene.SceneManager;
import com.p1_7.game.managers.FontManager;

/**
 * Game-specific scene manager dependencies.
 */
public final class GameSceneManager extends SceneManager {

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[] {
            EntityManager.class,
            RenderManager.class,
            InputManager.class,
            FontManager.class
        };
    }
}

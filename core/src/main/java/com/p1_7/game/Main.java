package com.p1_7.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import com.p1_7.abstractengine.engine.Engine;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.scene.SceneManager;

import com.p1_7.game.platform.GdxInputSource;
import com.p1_7.game.platform.GdxRenderManager;
import com.p1_7.game.scenes.Menuscene;
import com.p1_7.game.scenes.Settingscene;

/**
 * Entry point for the game application.
 *
 * Bootstraps the engine with the minimum set of managers required to
 * display the hello-world scene: entity management, rendering, and
 * scene orchestration.
 */
public class Main extends ApplicationAdapter {

    private Engine engine;

    /**
     * Initialises the engine and registers all managers and scenes.
     * Called once by libGDX when the application window is ready.
     */
    @Override
    public void create() {
        engine = new Engine();

        // core managers, registration order does not matter;
        // engine reorders managers via topological sort on a directed acyclic graph.
        engine.registerManager(new EntityManager());
        engine.registerManager(new InputManager(new GdxInputSource()));
        engine.registerManager(new GdxRenderManager());

        // scene setup
        SceneManager sceneManager = new SceneManager();

        // main menu (shown first)
        sceneManager.registerScene(new Menuscene());

        // settings screen
        sceneManager.registerScene(new Settingscene());

        sceneManager.setInitialScene("menu"); // start at the main menu
        engine.registerManager(sceneManager);

        engine.init();
    }

    /**
     * Called every frame by libGDX. Delegates update and render to the engine.
     */
    @Override
    public void render() {
        engine.update(Gdx.graphics.getDeltaTime());
        engine.render();
    }

    /**
     * Called by libGDX when the application is closing.
     * Shuts down all managers in reverse dependency order.
     */
    @Override
    public void dispose() {
        engine.shutdown();
    }
}

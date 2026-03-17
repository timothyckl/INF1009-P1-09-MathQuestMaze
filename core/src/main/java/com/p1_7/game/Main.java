package com.p1_7.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import com.p1_7.abstractengine.engine.Engine;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.scene.SceneManager;

import com.p1_7.game.managers.AudioManager;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.platform.GdxInputSource;
import com.p1_7.game.platform.GdxRenderManager;
import com.p1_7.game.scenes.LevelCompleteScene;
import com.p1_7.game.scenes.MenuScene;
import com.p1_7.game.scenes.SettingScene;

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

        AudioManager audioManager = new AudioManager();

        // core managers, registration order does not matter;
        // engine reorders managers via topological sort on a directed acyclic graph.
        engine.registerManager(new EntityManager());
        engine.registerManager(new InputManager(new GdxInputSource()));
        engine.registerManager(new GdxRenderManager());
        engine.registerManager(audioManager);

        // scene setup
        SceneManager sceneManager = new SceneManager();
        sceneManager.registerService(IAudioManager.class, audioManager);

        // main menu (shown first)
        sceneManager.registerScene(new MenuScene());

        // settings screen
        sceneManager.registerScene(new SettingScene());

        // temporary level-complete screen for flow testing
        sceneManager.registerScene(new LevelCompleteScene());

        sceneManager.setInitialScene("menu"); // start at the main menu
        engine.registerManager(sceneManager);

        engine.init();

        // audio setup — load and start background music
        audioManager.loadMusic("bgMusic", "demo_archive/music.mp3");
        audioManager.playMusic("bgMusic", true);
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

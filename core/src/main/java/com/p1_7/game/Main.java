package com.p1_7.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import com.p1_7.abstractengine.engine.Engine;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputManager;

import com.p1_7.game.level.ILevelOrchestrator;
import com.p1_7.game.level.LevelOrchestrator;
import com.p1_7.game.managers.MazeCollisionManager;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.managers.AudioManager;
import com.p1_7.game.managers.FontManager;
import com.p1_7.game.managers.GameMovementManager;
import com.p1_7.game.managers.GameSceneManager;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.managers.IFontManager;
import com.p1_7.game.platform.GdxCursorSource;
import com.p1_7.game.platform.GdxInputSource;
import com.p1_7.game.platform.GdxRenderManager;
import com.p1_7.game.scenes.GameScene;
import com.p1_7.game.scenes.GameOverScene;
import com.p1_7.game.scenes.LevelCompleteScene;
import com.p1_7.game.scenes.MenuScene;
import com.p1_7.game.scenes.settings.SettingScene;

/**
 * Entry point for the game application.
 *
 * Bootstraps the engine with the minimum set of managers required to
 * run the game: entity management, rendering, input, audio, and
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
        FontManager fontManager = new FontManager();
        GameMovementManager movementManager = new GameMovementManager();
        MazeCollisionManager collisionManager = new MazeCollisionManager();
        // orchestrator is a pure domain object with no engine lifecycle; registered as
        // a scene service only, not as an engine manager
        LevelOrchestrator orchestrator = new LevelOrchestrator();

        // build and configure the input manager before handing it to the engine
        // so extensions are available to scenes from the first frame
        InputManager inputManager =
            new InputManager(new GdxInputSource(), GameActions.getDefaultBindings());
        inputManager.registerExtension(ICursorSource.class, new GdxCursorSource());

        // core managers are ordered by their declared dependencies during init.
        engine.registerManager(new EntityManager());
        engine.registerManager(inputManager);
        engine.registerManager(new GdxRenderManager());
        engine.registerManager(audioManager);
        engine.registerManager(fontManager);
        engine.registerManager(movementManager);
        engine.registerManager(collisionManager);

        // scene setup
        GameSceneManager sceneManager = new GameSceneManager();
        sceneManager.registerService(IAudioManager.class, audioManager);
        sceneManager.registerService(IFontManager.class, fontManager);
        sceneManager.registerService(GameMovementManager.class, movementManager);
        sceneManager.registerService(MazeCollisionManager.class, collisionManager);
        sceneManager.registerService(ILevelOrchestrator.class, orchestrator);

        // main menu (shown first)
        sceneManager.registerScene(new MenuScene());

        // settings screen
        sceneManager.registerScene(new SettingScene());

        // level-complete and game-over exit scenes
        sceneManager.registerScene(new LevelCompleteScene());
        sceneManager.registerScene(new GameOverScene());

        // core gameplay scene
        sceneManager.registerScene(new GameScene());

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

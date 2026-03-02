package com.p1_7.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.p1_7.abstractengine.engine.Engine;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputMapping;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.movement.MovementManager;
import com.p1_7.abstractengine.scene.SceneManager;
import com.p1_7.demo.managers.DemoCollisionManager;
import com.p1_7.abstractengine.render.RenderManager;
import com.p1_7.demo.scenes.GameOverScene;
import com.p1_7.demo.scenes.GameScene;
import com.p1_7.demo.scenes.MenuScene;
import com.p1_7.demo.scenes.PauseScene;
import com.p1_7.demo.input.DemoActions;

public class Main extends ApplicationAdapter {

    private Engine engine;
    private EntityManager entityManager;
    private MovementManager movementManager;
    private DemoCollisionManager collisionManager;
    private InputManager inputManager;
    private RenderManager renderManager;
    private SceneManager sceneManager;

    @Override
    public void create() {
        // 1. instantiate engine
        engine = new Engine();

        // 2. create all managers
        entityManager = new EntityManager();
        movementManager = new MovementManager();
        collisionManager = new DemoCollisionManager();
        inputManager = new InputManager();
        renderManager = new RenderManager();
        sceneManager = new SceneManager();

        float[] worldMinBound = { 0f, 0f };
        float[] worldMaxBound = { Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT };

        // 3. configure movement boundaries
        movementManager.setWorldBounds(worldMinBound, worldMaxBound);

        // 4. configure input bindings
        InputMapping mapping = inputManager.getInputMapping();
        mapping.bindKey(Input.Keys.LEFT, DemoActions.LEFT);
        mapping.bindKey(Input.Keys.A, DemoActions.LEFT);
        mapping.bindKey(Input.Keys.RIGHT, DemoActions.RIGHT);
        mapping.bindKey(Input.Keys.D, DemoActions.RIGHT);

        // 5. create and register all scenes
        MenuScene menuScene = new MenuScene();
        GameScene gameScene = new GameScene(movementManager, collisionManager);
        GameOverScene gameOverScene = new GameOverScene();
        PauseScene pauseScene = new PauseScene();

        sceneManager.registerScene(menuScene);
        sceneManager.registerScene(gameScene);
        sceneManager.registerScene(gameOverScene);
        sceneManager.registerScene(pauseScene);

        // set menu as initial scene
        sceneManager.setInitialScene("menu");

        // 6. register managers with engine (order is irrelevant; dependencies are wired automatically)
        engine.registerManager(entityManager);
        engine.registerManager(movementManager);
        engine.registerManager(collisionManager);
        engine.registerManager(inputManager);
        engine.registerManager(renderManager);
        engine.registerManager(sceneManager);

        // 7. initialise engine
        engine.init();
    }

    @Override
    public void render() {
        engine.update(Gdx.graphics.getDeltaTime());
        engine.render();
    }

    @Override
    public void dispose() {
        engine.shutdown();
    }
}

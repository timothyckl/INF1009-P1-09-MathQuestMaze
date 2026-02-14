package com.p1_7.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.p1_7.abstractengine.engine.Engine;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputMapping;
import com.p1_7.abstractengine.input.InputOutputManager;
import com.p1_7.abstractengine.movement.MovementManager;
import com.p1_7.abstractengine.scene.SceneManager;

public class Main extends ApplicationAdapter {

    private Engine engine;
    private EntityManager entityManager;
    private MovementManager movementManager;
    private DemoCollisionManager collisionManager;
    private InputOutputManager inputOutputManager;
    private DemoRenderManager renderManager;
    private SceneManager sceneManager;

    @Override
    public void create() {
        // 1. instantiate engine
        engine = new Engine();

        // 2. create all managers
        entityManager = new EntityManager();
        movementManager = new MovementManager();
        collisionManager = new DemoCollisionManager();
        inputOutputManager = new InputOutputManager();
        renderManager = new DemoRenderManager();
        sceneManager = new SceneManager(entityManager, renderManager.getRenderQueue(), inputOutputManager);

        float[] worldMinBound = { 0f, 0f };
        float[] worldMaxBound = { Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT };

        // 3. configure movement boundaries
        movementManager.setWorldBounds(worldMinBound, worldMaxBound);

        // 4. configure input bindings
        InputMapping mapping = inputOutputManager.getInputMapping();
        mapping.bindKey(Input.Keys.LEFT, DemoActions.LEFT);
        mapping.bindKey(Input.Keys.A, DemoActions.LEFT);
        mapping.bindKey(Input.Keys.RIGHT, DemoActions.RIGHT);
        mapping.bindKey(Input.Keys.D, DemoActions.RIGHT);

        // 5. create and register all scenes
        MenuScene menuScene = new MenuScene();
        sceneManager.registerScene(menuScene);

        GameScene gameScene = new GameScene(movementManager, collisionManager, entityManager);
        sceneManager.registerScene(gameScene);

        GameOverScene gameOverScene = new GameOverScene();
        sceneManager.registerScene(gameOverScene);

        // set menu as initial scene
        sceneManager.setInitialScene("menu");

        // 6. register managers with engine (documented order)
        engine.registerManager(entityManager);
        engine.registerManager(movementManager);
        engine.registerManager(collisionManager);
        engine.registerManager(inputOutputManager);
        engine.registerManager(renderManager);
        engine.registerManager(sceneManager);

        // 7. set render manager for explicit render call
        engine.setRenderManager(renderManager);

        // 8. initialise engine
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

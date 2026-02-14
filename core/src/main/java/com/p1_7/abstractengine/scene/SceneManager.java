package com.p1_7.abstractengine.scene;

import com.badlogic.gdx.utils.ObjectMap;

import com.p1_7.abstractengine.engine.UpdatableManager;
import com.p1_7.abstractengine.entity.IEntityRepository;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IRenderQueue;

/**
 * manages the collection of scenes and drives the active scene's
 * lifecycle and per-frame callbacks.
 *
 * scene transitions are deferred: requestChange(String)
 * stores the target key and the actual swap happens at the top of the
 * next update tick. this avoids mutating state mid-frame.
 */
public class SceneManager extends UpdatableManager {

    /** all registered scenes, keyed by name */
    private final ObjectMap<String, Scene> scenes = new ObjectMap<>();

    /** the name of the currently active scene (may be null) */
    private String currentKey;

    /** the name of the scene to transition to next frame (may be null) */
    private String pendingKey;

    /** the context object passed into scene callbacks */
    private SceneContext context;

    /** injected entity repository — used to build the context */
    private final IEntityRepository entityRepository;

    /** injected render queue — used to build the context */
    private final IRenderQueue renderQueue;

    /** injected input query — used to build the context */
    private final IInputQuery inputQuery;

    /**
     * constructs a SceneManager with the three dependencies
     * it needs to assemble a SceneContext.
     *
     * @param entityRepository the read-only entity store
     * @param renderQueue      the single-frame render queue
     * @param inputQuery       the input query interface
     */
    public SceneManager(IEntityRepository entityRepository,
                        IRenderQueue renderQueue,
                        IInputQuery inputQuery) {
        this.entityRepository = entityRepository;
        this.renderQueue = renderQueue;
        this.inputQuery = inputQuery;
    }

    // ---------------------------------------------------------------
    // Manager lifecycle hooks
    // ---------------------------------------------------------------

    /**
     * assembles the SceneContext from the injected
     * references. if an initial scene has already been set via
     * setInitialScene(String), its Scene.onEnter
     * callback is invoked immediately.
     */
    @Override
    protected void onInit() {
        // build context as an anonymous implementation that closes
        // over the injected references
        context = new SceneContext() {
            @Override
            public IEntityRepository entities() {
                return entityRepository;
            }

            @Override
            public IRenderQueue renderQueue() {
                return renderQueue;
            }

            @Override
            public IInputQuery input() {
                return inputQuery;
            }

            @Override
            public void changeScene(String key) {
                SceneManager.this.requestChange(key);
            }

            @Override
            public Scene getScene(String key) {
                return SceneManager.this.getScene(key);
            }
        };

        // if an initial scene was registered before init, enter it now
        if (currentKey != null && scenes.containsKey(currentKey)) {
            scenes.get(currentKey).onEnter(context);
        }
    }

    /**
     * exits the current scene (if one is active) and clears the
     * scene registry.
     */
    @Override
    protected void onShutdown() {
        if (currentKey != null && scenes.containsKey(currentKey)) {
            scenes.get(currentKey).onExit(context);
        }
        scenes.clear();
    }

    // ---------------------------------------------------------------
    // scene registration & configuration
    // ---------------------------------------------------------------

    /**
     * adds a scene to the registry, keyed by its name.
     *
     * @param scene the scene to register
     */
    public void registerScene(Scene scene) {
        scenes.put(scene.getName(), scene);
    }

    /**
     * sets the initial scene key. must be called before
     * com.p1_7.abstractengine.engine.Engine.init() so that
     * onInit() can enter the scene.
     *
     * @param key the name of the scene to start in
     */
    public void setInitialScene(String key) {
        this.currentKey = key;
    }

    /**
     * requests a deferred transition to the scene identified by
     * key. the transition is resolved at the top of the next
     * update tick.
     *
     * @param key the name of the target scene
     */
    public void requestChange(String key) {
        this.pendingKey = key;
    }

    // ---------------------------------------------------------------
    // accessors
    // ---------------------------------------------------------------

    /**
     * returns the currently active scene.
     *
     * @return the active Scene, or null if none is set
     */
    public Scene getCurrentScene() {
        if (currentKey == null) {
            return null;
        }
        return scenes.get(currentKey);
    }

    /**
     * returns the scene registered under the specified key.
     *
     * @param key the name of the scene to retrieve
     * @return the Scene, or null if not found
     */
    public Scene getScene(String key) {
        return scenes.get(key);
    }

    /**
     * returns an iterable over the names of all registered scenes.
     *
     * @return the set of scene keys
     */
    public Iterable<String> getSceneKeys() {
        return scenes.keys();
    }

    // ---------------------------------------------------------------
    // UpdatableManager hook
    // ---------------------------------------------------------------

    /**
     * resolves any pending scene transition, then drives the active
     * scene's update and render-submission hooks.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        // 1. resolve a pending transition if one was requested
        if (pendingKey != null) {
            // exit the current scene
            if (currentKey != null && scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onExit(context);
            }
            // swap to the new scene
            currentKey = pendingKey;
            pendingKey = null;
            // enter the new scene
            if (scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onEnter(context);
            }
        }

        Scene current = getCurrentScene();
        if (current == null) {
            return;
        }

        // 2. update — skipped while paused
        if (!current.isPaused()) {
            current.update(deltaTime, context);
        }

        // 3. submit renderables — always called
        current.submitRenderable(context);
    }
}

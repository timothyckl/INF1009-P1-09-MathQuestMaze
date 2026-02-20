package com.p1_7.abstractengine.scene;

import com.badlogic.gdx.utils.ObjectMap;

import com.p1_7.abstractengine.engine.UpdatableManager;
import com.p1_7.abstractengine.entity.IEntityManager;
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

    /** the name of the scene to suspend-transition to next frame (may be null) */
    private String pendingSuspendKey;

    /** tracks which scene was suspended (for resume detection) */
    private String suspendedSceneKey;

    /** the context object passed into scene callbacks */
    private SceneContext context;

    /** injected entity manager — used to build the context */
    private final IEntityManager entityManager;

    /** injected render queue — used to build the context */
    private final IRenderQueue renderQueue;

    /** injected input query — used to build the context */
    private final IInputQuery inputQuery;

    /**
     * constructs a SceneManager with the dependencies it needs to
     * assemble a SceneContext.
     *
     * @param entityManager the entity manager providing read and write access
     * @param renderQueue   the single-frame render queue
     * @param inputQuery    the input query interface
     */
    public SceneManager(IEntityManager entityManager,
                        IRenderQueue renderQueue,
                        IInputQuery inputQuery) {
        this.entityManager = entityManager;
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
            public IEntityManager entities() {
                return entityManager;
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
            public void suspendScene(String key) {
                SceneManager.this.requestSuspend(key);
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
     * @throws IllegalArgumentException if key is null or blank
     */
    public void setInitialScene(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be blank");
        }
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

    /**
     * requests a deferred suspend transition to the scene identified by
     * key. the transition is resolved at the top of the next
     * update tick using onSuspend/onResume instead of onExit/onEnter.
     *
     * @param key the name of the target scene
     */
    public void requestSuspend(String key) {
        this.pendingSuspendKey = key;
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

    /**
     * stores the key of the scene that was suspended.
     *
     * @param key the scene key
     */
    private void storePreviousScene(String key) {
        this.suspendedSceneKey = key;
    }

    /**
     * returns the key of the suspended scene, if any.
     *
     * @return the suspended scene key, or null
     */
    private String getSuspendedScene() {
        return suspendedSceneKey;
    }

    /**
     * clears the suspended scene tracking.
     */
    private void clearSuspendedScene() {
        this.suspendedSceneKey = null;
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
        // 1a. resolve a pending suspend transition if one was requested
        if (pendingSuspendKey != null) {
            // suspend the current scene (preserve state)
            if (currentKey != null && scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onSuspend(context);
            }
            // swap to the new scene
            String previousKey = currentKey;
            currentKey = pendingSuspendKey;
            pendingSuspendKey = null;
            // enter the new scene normally (it's starting fresh)
            if (scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onEnter(context);
            }
            // store previous key for resume
            storePreviousScene(previousKey);
        }

        // 1b. resolve a pending change transition if one was requested
        if (pendingKey != null) {
            // check if we're returning to a suspended scene
            boolean isResuming = (pendingKey != null && pendingKey.equals(getSuspendedScene()));

            if (isResuming) {
                // exit current scene normally
                if (currentKey != null && scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onExit(context);
                }
                // swap to the suspended scene
                currentKey = pendingKey;
                pendingKey = null;
                clearSuspendedScene();
                // resume the suspended scene (preserve state)
                if (scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onResume(context);
                }
            } else {
                // normal transition: exit current, enter new
                if (currentKey != null && scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onExit(context);
                }
                currentKey = pendingKey;
                pendingKey = null;
                if (scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onEnter(context);
                }
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

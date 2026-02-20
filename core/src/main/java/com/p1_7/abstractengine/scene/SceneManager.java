package com.p1_7.abstractengine.scene;

import com.badlogic.gdx.utils.ObjectMap;

import com.p1_7.abstractengine.engine.UpdatableManager;
import com.p1_7.abstractengine.entity.IEntityManager;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IRenderQueue;

/**
 * manages the scene registry and drives the active scene's lifecycle and
 * per-frame callbacks.
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

    /**
     * assembles the SceneContext and enters the initial scene if one has been set.
     */
    @Override
    protected void onInit() {
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

    /**
     * adds a scene to the registry, keyed by its name.
     *
     * @param scene the scene to register
     */
    public void registerScene(Scene scene) {
        scenes.put(scene.getName(), scene);
    }

    /**
     * sets the initial scene key; must be called before the engine is initialised.
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
     * schedules a transition to the named scene for the next update tick.
     *
     * @param key the name of the target scene
     */
    public void requestChange(String key) {
        validateSceneKey(key);
        this.pendingKey = key;
    }

    /**
     * schedules a suspend transition to the named scene for the next update tick.
     *
     * @param key the name of the target scene
     */
    public void requestSuspend(String key) {
        validateSceneKey(key);
        this.pendingSuspendKey = key;
    }

    /**
     * validates a scene key for deferred transition requests.
     *
     * @param key the target scene key
     * @throws IllegalArgumentException if key is null, blank, or not registered
     */
    private void validateSceneKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be blank");
        }
        if (!scenes.containsKey(key)) {
            throw new IllegalArgumentException("scene not registered: " + key);
        }
    }

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

    /**
     * resolves any pending scene transition, then drives the active
     * scene's update and render-submission hooks.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        if (pendingSuspendKey != null) {
            if (currentKey != null && scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onSuspend(context);
            }
            String previousKey = currentKey;
            currentKey = pendingSuspendKey;
            pendingSuspendKey = null;
            if (scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onEnter(context);
            }
            storePreviousScene(previousKey);
        }

        if (pendingKey != null) {
            boolean isResuming = (pendingKey != null && pendingKey.equals(getSuspendedScene()));

            if (isResuming) {
                if (currentKey != null && scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onExit(context);
                }
                currentKey = pendingKey;
                pendingKey = null;
                clearSuspendedScene();
                if (scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onResume(context);
                }
            } else {
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

        // skipped while paused
        if (!current.isPaused()) {
            current.update(deltaTime, context);
        }

        // always called
        current.submitRenderable(context);
    }
}

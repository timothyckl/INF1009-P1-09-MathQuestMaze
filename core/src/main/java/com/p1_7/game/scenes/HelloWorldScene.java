package com.p1_7.game.scenes;

import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.entities.HelloWorldText;

/**
 * A minimal scene that renders a centred "Hello, World!" message.
 *
 * The text entity implements ICustomRenderable directly, following the
 * pattern described in the engine API guide.
 */
public class HelloWorldScene extends Scene {

    /** the text entity submitted to the render queue each frame */
    private HelloWorldText helloText;

    public HelloWorldScene() {
        this.name = "hello";
    }

    /**
     * Creates the text entity, centred horizontally and vertically.
     *
     * @param context provides access to engine subsystems
     */
    @Override
    public void onEnter(SceneContext context) {
        // position the text roughly in the centre of the window
        float x = Settings.WINDOW_WIDTH / 2f - 90f;
        float y = Settings.WINDOW_HEIGHT / 2f;
        helloText = new HelloWorldText("Hello, World!", x, y, 2.0f);
    }

    /**
     * Nothing to clean up when leaving this scene.
     *
     * @param context provides access to engine subsystems
     */
    @Override
    public void onExit(SceneContext context) {
        // nothing to dispose
    }

    /**
     * No interactive logic for a static hello-world scene.
     *
     * @param deltaTime seconds elapsed since last frame
     * @param context   provides access to engine subsystems
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        // nothing to update
    }

    /**
     * Queues the text entity for rendering this frame.
     *
     * @param context provides access to the render queue
     */
    @Override
    public void submitRenderable(SceneContext context) {
        context.get(IRenderQueue.class).queue(helloText);
    }
}

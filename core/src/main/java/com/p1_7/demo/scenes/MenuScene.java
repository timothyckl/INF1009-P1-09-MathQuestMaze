package com.p1_7.demo.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.demo.Settings;
import com.p1_7.demo.display.TextDisplay;
import com.p1_7.demo.entities.Background;

/**
 * main menu scene shown at game launch.
 *
 * displays the game title and a prompt to start. transitions to
 * the game scene when the player presses SPACE or ENTER.
 */
public class MenuScene extends Scene {

    /** background image */
    private Background background;

    /** title text display */
    private TextDisplay titleDisplay;

    /** start prompt text display */
    private TextDisplay promptDisplay;

    /**
     * constructs a menu scene.
     */
    public MenuScene() {
        this.name = "menu";
    }

    @Override
    public void onEnter(SceneContext context) {
        // 1. create background
        background = new Background(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);

        // 2. create title text (large, centred at top third)
        String titleText = "CATCH THE DROPLET";
        float titleX = Settings.WINDOW_WIDTH / 2f - 150f; // approximate centering
        float titleY = Settings.WINDOW_HEIGHT * 0.66f;
        titleDisplay = new TextDisplay(titleText, titleX, titleY, 2.0f);

        // 3. create prompt text (normal size, centred at middle)
        String promptText = "Press SPACE to start";
        float promptX = Settings.WINDOW_WIDTH / 2f - 100f; // approximate centering
        float promptY = Settings.WINDOW_HEIGHT * 0.5f;
        promptDisplay = new TextDisplay(promptText, promptX, promptY, 1.0f);
    }

    @Override
    public void onExit(SceneContext context) {
        // dispose fonts
        if (titleDisplay != null) {
            titleDisplay.dispose();
        }
        if (promptDisplay != null) {
            promptDisplay.dispose();
        }
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        // check for space key or enter to start game
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            context.changeScene("game");
        }
    }

    @Override
    public void submitRenderable(SceneContext context) {
        // queue background first
        context.get(IRenderQueue.class).queue(background);

        // queue text displays
        context.get(IRenderQueue.class).queue(titleDisplay);
        context.get(IRenderQueue.class).queue(promptDisplay);
    }
}

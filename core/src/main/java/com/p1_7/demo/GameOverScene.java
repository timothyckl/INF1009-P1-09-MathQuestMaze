package com.p1_7.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;

/**
 * game over scene shown when player loses all lives.
 *
 * displays the final score and provides options to replay the game
 * or return to the main menu.
 */
public class GameOverScene extends Scene {

    /** background image */
    private Background background;

    /** game over title display */
    private TextDisplay titleDisplay;

    /** score display */
    private TextDisplay scoreDisplay;

    /** replay prompt display */
    private TextDisplay promptDisplay;

    /** final score to display */
    private int finalScore = 0;

    /**
     * constructs a game over scene.
     */
    public GameOverScene() {
        this.name = "gameover";
    }

    /**
     * sets the final score to display.
     * should be called before transitioning to this scene.
     *
     * @param score the final score achieved
     */
    public void setScore(int score) {
        this.finalScore = score;
    }

    @Override
    public void onEnter(SceneContext context) {
        // 1. create background
        background = new Background(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);

        // 2. create title text (large, centred at top third)
        String titleText = "GAME OVER";
        float titleX = Settings.WINDOW_WIDTH / 2f - 100f; // approximate centering
        float titleY = Settings.WINDOW_HEIGHT * 0.66f;
        titleDisplay = new TextDisplay(titleText, titleX, titleY, 2.0f);

        // 3. create score text (centred at middle)
        String scoreText = "Score: " + finalScore;
        float scoreX = Settings.WINDOW_WIDTH / 2f - 60f; // approximate centering
        float scoreY = Settings.WINDOW_HEIGHT * 0.5f;
        scoreDisplay = new TextDisplay(scoreText, scoreX, scoreY, 1.5f);

        // 4. create prompt text (normal size, centred at bottom third)
        String promptText = "Press SPACE to play again or ESC for menu";
        float promptX = Settings.WINDOW_WIDTH / 2f - 180f; // approximate centering
        float promptY = Settings.WINDOW_HEIGHT * 0.33f;
        promptDisplay = new TextDisplay(promptText, promptX, promptY, 1.0f);
    }

    @Override
    public void onExit(SceneContext context) {
        // dispose fonts
        titleDisplay.dispose();
        scoreDisplay.dispose();
        promptDisplay.dispose();
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        // space = replay, escape = menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            context.changeScene("game");
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            context.changeScene("menu");
        }
    }

    @Override
    public void submitRenderable(SceneContext context) {
        // queue background first
        context.renderQueue().queue(background);

        // queue text displays
        context.renderQueue().queue(titleDisplay);
        context.renderQueue().queue(scoreDisplay);
        context.renderQueue().queue(promptDisplay);
    }
}

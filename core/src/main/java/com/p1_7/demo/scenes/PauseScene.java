package com.p1_7.demo.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.demo.Settings;
import com.p1_7.demo.display.TextDisplay;
import com.p1_7.demo.display.VolumeSlider;
import com.p1_7.demo.entities.Background;

/**
 * pause scene shown when player pauses the game.
 *
 * displays current lives, current score, volume control slider,
 * and instructions to resume playing.
 */
public class PauseScene extends Scene {

    /** background image */
    private Background background;

    /** pause title display */
    private TextDisplay titleDisplay;

    /** lives display */
    private TextDisplay livesDisplay;

    /** score display */
    private TextDisplay scoreDisplay;

    /** volume label display */
    private TextDisplay volumeLabel;

    /** volume slider component */
    private VolumeSlider volumeSlider;

    /** resume prompt display */
    private TextDisplay resumePrompt;

    /** reference to game music for volume control */
    private Music musicReference;

    /** current lives to display */
    private int currentLives = 0;

    /** current score to display */
    private int currentScore = 0;

    /**
     * constructs a pause scene.
     */
    public PauseScene() {
        this.name = "pause";
    }

    /**
     * sets the game state to display.
     * should be called before transitioning to this scene.
     *
     * @param lives the current lives count
     * @param score the current score
     */
    public void setGameState(int lives, int score) {
        this.currentLives = lives;
        this.currentScore = score;
    }

    /**
     * sets the music reference for volume control.
     * should be called before transitioning to this scene.
     *
     * @param music the music instance to control
     */
    public void setMusicReference(Music music) {
        this.musicReference = music;
    }

    @Override
    public void onEnter(SceneContext context) {
        // 1. create background
        background = new Background(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);

        // 2. create title text (large, centred at top)
        String titleText = "PAUSED";
        float titleX = Settings.WINDOW_WIDTH / 2f - 60f; // approximate centring
        float titleY = Settings.WINDOW_HEIGHT * 0.75f;
        titleDisplay = new TextDisplay(titleText, titleX, titleY, 2.0f);

        // 3. create lives text (upper middle)
        String livesText = "Lives: " + currentLives;
        float livesX = Settings.WINDOW_WIDTH / 2f - 50f;
        float livesY = Settings.WINDOW_HEIGHT * 0.6f;
        livesDisplay = new TextDisplay(livesText, livesX, livesY, 1.2f);

        // 4. create score text (middle)
        String scoreText = "Score: " + currentScore;
        float scoreX = Settings.WINDOW_WIDTH / 2f - 50f;
        float scoreY = Settings.WINDOW_HEIGHT * 0.5f;
        scoreDisplay = new TextDisplay(scoreText, scoreX, scoreY, 1.2f);

        // 5. create volume label with instructions
        String volumeText = "Volume: (Use <- -> to adjust)";
        float volumeLabelX = Settings.WINDOW_WIDTH / 2f - 120f;
        float volumeLabelY = Settings.WINDOW_HEIGHT * 0.35f;
        volumeLabel = new TextDisplay(volumeText, volumeLabelX, volumeLabelY, 1.0f);

        // 6. create volume slider
        float sliderX = Settings.WINDOW_WIDTH / 2f - 100f;
        float sliderY = Settings.WINDOW_HEIGHT * 0.27f;
        volumeSlider = (VolumeSlider) context.entities().createEntity(
            () -> new VolumeSlider(sliderX, sliderY, 200f)
        );

        // 7. create resume prompt (bottom)
        String promptText = "Press ESC or P to resume";
        float promptX = Settings.WINDOW_WIDTH / 2f - 120f;
        float promptY = Settings.WINDOW_HEIGHT * 0.15f;
        resumePrompt = new TextDisplay(promptText, promptX, promptY, 1.0f);
    }

    @Override
    public void onExit(SceneContext context) {
        // dispose fonts
        titleDisplay.dispose();
        livesDisplay.dispose();
        scoreDisplay.dispose();
        volumeLabel.dispose();
        resumePrompt.dispose();

        // remove volume slider entity
        if (volumeSlider != null) {
            context.entities().removeEntity(volumeSlider.getId());
        }
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        // update volume slider
        volumeSlider.update(deltaTime);

        // apply volume changes to music in real-time
        if (musicReference != null) {
            musicReference.setVolume(Settings.MUSIC_VOLUME);
        }

        // check for resume keys
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            context.changeScene("game");
        }
    }

    @Override
    public void submitRenderable(SceneContext context) {
        // queue background first
        context.renderQueue().queue(background);

        // queue text displays
        context.renderQueue().queue(titleDisplay);
        context.renderQueue().queue(livesDisplay);
        context.renderQueue().queue(scoreDisplay);
        context.renderQueue().queue(volumeLabel);

        // queue volume slider
        context.renderQueue().queue(volumeSlider);

        // queue resume prompt
        context.renderQueue().queue(resumePrompt);
    }
}

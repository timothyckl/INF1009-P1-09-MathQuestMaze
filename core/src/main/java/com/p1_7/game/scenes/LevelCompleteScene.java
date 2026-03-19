package com.p1_7.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.entities.BackgroundImage;
import com.p1_7.game.entities.BrightnessOverlay;
import com.p1_7.game.entities.MenuButton;
import com.p1_7.game.entities.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;

public class LevelCompleteScene extends Scene {

    private static final int MAX_LEVEL = 3;
    private static final float INPUT_COOLDOWN_SECONDS = 0.18f;
    private static final String BG_ASSET = "menu/background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";
    private static final String TTF_ASSET = "menu/Kenney_Future.ttf";

    // ── input ────────────────────────────────────────────────────
    private ICursorSource cursorSource;
    private IInputQuery inputQuery;

    private BitmapFont titleFont;
    private BitmapFont promptFont;
    private BitmapFont buttonFont;
    private BackgroundImage background;
    private Text title;
    private Text promptStatus;
    private Text hintSpace;
    private Text hintEsc;
    private MenuButton continueButton;
    private MenuButton mainMenuButton;
    private BrightnessOverlay brightnessOverlay;
    private int currentLevel = 1;
    private float inputCooldown;

    public LevelCompleteScene() {
        this.name = "level-complete";
    }

    @Override
    public void onEnter(SceneContext context) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(TTF_ASSET));

        FreeTypeFontParameter titleParams = new FreeTypeFontParameter();
        titleParams.size = 54;
        titleParams.color = new Color(1f, 0.92f, 0.55f, 1f);
        titleParams.shadowOffsetX = 2;
        titleParams.shadowOffsetY = -2;
        titleParams.shadowColor = new Color(0f, 0f, 0f, 0.5f);
        titleFont = generator.generateFont(titleParams);

        FreeTypeFontParameter promptParams = new FreeTypeFontParameter();
        promptParams.size = 28;
        promptParams.color = new Color(0.10f, 0.16f, 0.24f, 1f); // dark navy for better contrast
        promptParams.shadowOffsetX = 1;
        promptParams.shadowOffsetY = -1;
        promptParams.shadowColor = new Color(1f, 1f, 1f, 0.35f);
        promptFont = generator.generateFont(promptParams);

        FreeTypeFontParameter buttonParams = new FreeTypeFontParameter();
        buttonParams.size = 22;
        buttonParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        buttonFont = generator.generateFont(buttonParams);

        generator.dispose();

        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        inputQuery = context.get(IInputQuery.class);
        if (inputRegistry.hasExtension(ICursorSource.class)) {
            cursorSource = inputRegistry.getExtension(ICursorSource.class);
        }
        // cursorSource stays null if not registered; update() guard handles it cleanly

        float cx = Settings.getWindowWidth() / 2f;
        float cy = Settings.getWindowHeight() / 2f;
        boolean lastLevel = isLastLevel();
        int nextLevel = lastLevel ? 1 : currentLevel + 1;
        String continueLabel = lastLevel ? "PLAY AGAIN" : "CONTINUE";
        String spaceHint = lastLevel ? "SPACE - Play Again" : "SPACE - Continue";
        background = new BackgroundImage(BG_ASSET);
        title = new Text("LEVEL " + currentLevel + " COMPLETE!", cx, cy + 120f, titleFont);
        promptStatus = new Text("Next up: Level " + nextLevel, cx, cy + 55f, promptFont);
        continueButton = MenuButton.withTexture(continueLabel, cx, cy - 10f, buttonFont, BTN_ASSET, HOVER_ASSET);
        mainMenuButton = MenuButton.withTexture("MAIN MENU", cx, cy - 85f, buttonFont, BTN_ASSET, HOVER_ASSET);
        hintSpace = new Text(spaceHint, cx, cy - 175f, promptFont);
        hintEsc = new Text("ESC - Main Menu", cx, cy - 220f, promptFont);
        brightnessOverlay = new BrightnessOverlay();

        inputCooldown = INPUT_COOLDOWN_SECONDS;
    }

    @Override
    public void onExit(SceneContext context) {
        if (continueButton != null) continueButton.dispose();
        if (mainMenuButton != null) mainMenuButton.dispose();
        if (brightnessOverlay != null) brightnessOverlay.dispose();
        if (titleFont      != null) titleFont.dispose();
        if (promptFont     != null) promptFont.dispose();
        if (buttonFont     != null) buttonFont.dispose();
        inputQuery = null;
        cursorSource = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (inputCooldown > 0f) {
            inputCooldown -= deltaTime;
            return;
        }

        if (cursorSource != null) {
            continueButton.updateInput(cursorSource, inputQuery);
            mainMenuButton.updateInput(cursorSource, inputQuery);
        }

        boolean backPressed = inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED;
        boolean confirmPressed =
            inputQuery.getActionState(GameActions.MENU_CONFIRM) == InputState.PRESSED;

        if (backPressed || mainMenuButton.isClicked()) {
            mainMenuButton.resetClick();
            context.changeScene("menu");
            return;
        }

        if (confirmPressed || continueButton.isClicked()) {
            continueButton.resetClick();
            if (isLastLevel()) {
                currentLevel = 1;
            } else {
                currentLevel++;
            }
            context.changeScene("level-complete");
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(title);
        renderQueue.queue(promptStatus);
        renderQueue.queue(continueButton);
        renderQueue.queue(mainMenuButton);
        renderQueue.queue(hintSpace);
        renderQueue.queue(hintEsc);
        renderQueue.queue(brightnessOverlay);
    }

    private boolean isLastLevel() {
        return currentLevel >= MAX_LEVEL;
    }
}

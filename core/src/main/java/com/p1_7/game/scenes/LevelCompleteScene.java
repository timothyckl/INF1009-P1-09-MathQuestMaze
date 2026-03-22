package com.p1_7.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.ui.BackgroundImage;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.MenuButton;
import com.p1_7.game.ui.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.managers.IFontManager;

public class LevelCompleteScene extends Scene {

    private static final int MAX_LEVEL = 3;
    private static final float INPUT_COOLDOWN_SECONDS = 0.18f;
    private static final String BG_ASSET = "menu/background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";

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
        context.get(IAudioManager.class).playMusic("level-complete", false);
        IFontManager fontManager = context.get(IFontManager.class);
        titleFont = fontManager.getGoldDisplayFont(54);
        promptFont = fontManager.getPromptFont();
        buttonFont = fontManager.getDarkTextFont(22);

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
        background        = null;
        title             = null;
        promptStatus      = null;
        hintSpace         = null;
        hintEsc           = null;
        continueButton    = null;
        mainMenuButton    = null;
        brightnessOverlay = null;
        titleFont         = null;
        promptFont        = null;
        buttonFont        = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (inputCooldown > 0f) {
            inputCooldown -= deltaTime;
            return;
        }

        IInputQuery inputQuery = context.get(IInputQuery.class);
        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        ICursorSource cursorSource = inputRegistry.hasExtension(ICursorSource.class)
            ? inputRegistry.getExtension(ICursorSource.class) : null;
        // cursorSource stays null if not registered; guard below handles it cleanly
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
            context.changeScene("game");
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

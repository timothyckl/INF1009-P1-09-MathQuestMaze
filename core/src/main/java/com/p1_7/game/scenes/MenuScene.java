package com.p1_7.game.scenes;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Gdx;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.entities.BackgroundImage;
import com.p1_7.game.entities.BrightnessOverlay;
import com.p1_7.game.entities.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.managers.IFontManager;
import com.p1_7.game.entities.MenuButton;

/**
 * Main menu scene for Math Quest Maze.
 *
 * Uses image assets for background + buttons, and Kenney_Future.ttf
 * loaded via gdx-freetype for crisp text at any size.
 *
 * Assets required in assets/menu/:
 *   background.png
 *   button.png
 *   button_hover.png
 *   Kenney_Future.ttf
 */
public class MenuScene extends Scene {

    // ── asset paths ──────────────────────────────────────────────
    private static final String BG_ASSET    = "menu/background.png";
    private static final String BTN_ASSET   = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";

    // ── layout ───────────────────────────────────────────────────
    // computed in onEnter so they reflect the resolution at scene entry time
    private float centreX;
    private float firstButtonY;
    private static final float BUTTON_SPACING = 80f;

    // ── fonts (generated from TTF, both owned + disposed here) ───
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    // ── input ────────────────────────────────────────────────────
    private ICursorSource cursorSource;
    private IInputQuery inputQuery;

    // ── entities ─────────────────────────────────────────────────
    private BackgroundImage background;
    private Text           titleText;
    private MenuButton     startButton;
    private MenuButton     settingsButton;
    private MenuButton     exitButton;
    private BrightnessOverlay brightnessOverlay;

    public MenuScene() {
        this.name = "menu";
    }

    @Override
    public void onEnter(SceneContext context) {
        // compute layout from the current resolution so changes via setResolution take effect
        centreX       = Settings.getWindowWidth()  / 2f;
        firstButtonY  = Settings.getWindowHeight() * 0.45f;

        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        inputQuery = context.get(IInputQuery.class);
        if (inputRegistry.hasExtension(ICursorSource.class)) {
            cursorSource = inputRegistry.getExtension(ICursorSource.class);
        }
        // cursorSource stays null if not registered; update() guard handles it cleanly

        IAudioManager audio = context.get(IAudioManager.class);
        IFontManager fontManager = context.get(IFontManager.class);

        // start background music (asset pre-loaded in AudioManager.onInit)
        audio.playMusic("bgMusic", true);

        titleFont = fontManager.getGoldDisplayFont(56);
        buttonFont = fontManager.getDarkTextFont(26);

        // ── entities ─────────────────────────────────────────────
        background = new BackgroundImage(BG_ASSET);
        titleText  = new Text("MATH QUEST MAZE", centreX,
                                   Settings.getWindowHeight() * 0.75f, titleFont);

        startButton    = MenuButton.withTexture("START",
                        centreX, firstButtonY,                       buttonFont, BTN_ASSET, HOVER_ASSET);
        settingsButton = MenuButton.withTexture("SETTINGS",
                        centreX, firstButtonY - BUTTON_SPACING,      buttonFont, BTN_ASSET, HOVER_ASSET);
        exitButton     = MenuButton.withTexture("EXIT",
                        centreX, firstButtonY - BUTTON_SPACING * 2f, buttonFont, BTN_ASSET, HOVER_ASSET);
        brightnessOverlay = new BrightnessOverlay();
    }

    @Override
    public void onExit(SceneContext context) {
        if (startButton    != null) startButton.dispose();
        if (settingsButton != null) settingsButton.dispose();
        if (exitButton     != null) exitButton.dispose();
        if (brightnessOverlay != null) brightnessOverlay.dispose();
        titleFont = null;
        buttonFont = null;
        inputQuery = null;
        cursorSource = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            Gdx.app.exit();
            return;
        }

        if (cursorSource == null) return;
        startButton.updateInput(cursorSource, inputQuery);
        settingsButton.updateInput(cursorSource, inputQuery);
        exitButton.updateInput(cursorSource, inputQuery);

        if (startButton.isClicked()) {
            startButton.resetClick();
            context.changeScene("level-complete"); // temporary test route until GameScene exists
            return;
        }
        if (settingsButton.isClicked()) {
            settingsButton.resetClick();
            context.changeScene("settings");
            return;
        }
        if (exitButton.isClicked()) {
            exitButton.resetClick();
            Gdx.app.exit();
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(titleText);
        renderQueue.queue(startButton);
        renderQueue.queue(settingsButton);
        renderQueue.queue(exitButton);
        renderQueue.queue(brightnessOverlay);
    }
}

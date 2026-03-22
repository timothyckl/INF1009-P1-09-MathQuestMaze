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
import com.p1_7.game.ui.BackgroundImage;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.managers.IFontManager;
import com.p1_7.game.ui.MenuButton;

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

    // ── ui components ────────────────────────────────────────────
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

        IAudioManager audio = context.get(IAudioManager.class);
        IFontManager fontManager = context.get(IFontManager.class);

        // start menu music (asset pre-loaded in AudioManager.onInit)
        audio.playMusic("menu", true);

        titleFont = fontManager.getGoldDisplayFont(56);
        buttonFont = fontManager.getDarkTextFont(26);

        // ── ui components ────────────────────────────────────────
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
        background        = null;
        titleText         = null;
        startButton       = null;
        settingsButton    = null;
        exitButton        = null;
        brightnessOverlay = null;
        titleFont         = null;
        buttonFont        = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        IInputQuery inputQuery = context.get(IInputQuery.class);
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            Gdx.app.exit();
            return;
        }

        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        ICursorSource cursorSource = inputRegistry.hasExtension(ICursorSource.class)
            ? inputRegistry.getExtension(ICursorSource.class) : null;
        if (cursorSource == null) return;
        startButton.updateInput(cursorSource, inputQuery);
        settingsButton.updateInput(cursorSource, inputQuery);
        exitButton.updateInput(cursorSource, inputQuery);

        if (startButton.isClicked()) {
            startButton.resetClick();
            context.changeScene("game");
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

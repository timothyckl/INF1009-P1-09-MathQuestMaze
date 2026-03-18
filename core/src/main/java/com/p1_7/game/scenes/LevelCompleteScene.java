package com.p1_7.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.entities.MenuButton;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.platform.GdxDrawContext;

public class LevelCompleteScene extends Scene {

    private static final int MAX_LEVEL = 3;
    private static final float INPUT_COOLDOWN_SECONDS = 0.18f;
    private static final String BG_ASSET = "menu/background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";
    private static final String TTF_ASSET = "menu/Kenney_Future.ttf";

    // ── input ────────────────────────────────────────────────────
    private ICursorSource cursorSource;

    private BitmapFont titleFont;
    private BitmapFont promptFont;
    private BitmapFont buttonFont;
    private Background background;
    private LabelText title;
    private LabelText promptStatus;
    private LabelText hintSpace;
    private LabelText hintEsc;
    private MenuButton continueButton;
    private MenuButton mainMenuButton;
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
        background = new Background(BG_ASSET);
        title = new LabelText("LEVEL " + currentLevel + " COMPLETE!", cx, cy + 120f, titleFont);
        promptStatus = new LabelText("Next up: Level " + nextLevel, cx, cy + 55f, promptFont);
        continueButton = MenuButton.withTexture(continueLabel, cx, cy - 10f, buttonFont, BTN_ASSET, HOVER_ASSET);
        mainMenuButton = MenuButton.withTexture("MAIN MENU", cx, cy - 85f, buttonFont, BTN_ASSET, HOVER_ASSET);
        hintSpace = new LabelText(spaceHint, cx, cy - 175f, promptFont);
        hintEsc = new LabelText("ESC - Main Menu", cx, cy - 220f, promptFont);

        inputCooldown = INPUT_COOLDOWN_SECONDS;
    }

    @Override
    public void onExit(SceneContext context) {
        if (continueButton != null) continueButton.dispose();
        if (mainMenuButton != null) mainMenuButton.dispose();
        if (titleFont      != null) titleFont.dispose();
        if (promptFont     != null) promptFont.dispose();
        if (buttonFont     != null) buttonFont.dispose();
        cursorSource = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (inputCooldown > 0f) {
            inputCooldown -= deltaTime;
            return;
        }

        if (cursorSource == null) return;
        continueButton.updateInput(cursorSource);
        mainMenuButton.updateInput(cursorSource);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || mainMenuButton.isClicked()) {
            mainMenuButton.resetClick();
            context.changeScene("menu");
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || continueButton.isClicked()) {
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
    }

    private boolean isLastLevel() {
        return currentLevel >= MAX_LEVEL;
    }

    private static class Background implements IRenderable {
        private final Transform2D transform;
        private final String assetPath;

        Background(String assetPath) {
            this.assetPath = assetPath;
            this.transform = new Transform2D(0, 0, Settings.getWindowWidth(), Settings.getWindowHeight());
        }

        @Override public String     getAssetPath() { return assetPath; }
        @Override public ITransform getTransform() { return transform; }

        @Override
        public void render(IDrawContext ctx) {
            GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
            gdxCtx.drawTexture(assetPath,
                transform.getPosition(0), transform.getPosition(1),
                transform.getSize(0),     transform.getSize(1));
        }
    }

    private static class LabelText extends Entity implements IRenderable {
        private final Transform2D transform;
        private final BitmapFont font;
        private final String text;

        LabelText(String text, float cx, float cy, BitmapFont font) {
            this.text = text;
            this.font = font;
            this.transform = new Transform2D(cx, cy, 0f, 0f);
        }

        @Override public String     getAssetPath() { return null; }
        @Override public ITransform getTransform() { return transform; }

        @Override
        public void render(IDrawContext ctx) {
            GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
            GlyphLayout layout = new GlyphLayout(font, text);
            gdxCtx.drawFont(font, text,
                transform.getPosition(0) - layout.width  / 2f,
                transform.getPosition(1) + layout.height / 2f);
        }
    }
}

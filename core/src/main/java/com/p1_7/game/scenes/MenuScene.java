package com.p1_7.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.entities.MenuButton;
import com.p1_7.game.platform.GdxShapeRenderer;
import com.p1_7.game.platform.GdxSpriteBatch;

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
    private static final String TTF_ASSET   = "menu/Kenney_Future.ttf";

    // ── layout ───────────────────────────────────────────────────
    // computed in onEnter so they reflect the resolution at scene entry time
    private float centreX;
    private float firstButtonY;
    private static final float BUTTON_SPACING = 80f;

    // ── fonts (generated from TTF, both owned + disposed here) ───
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    // ── entities ─────────────────────────────────────────────────
    private MenuBackground background;
    private TitleText      titleText;
    private MenuButton     btnStart;
    private MenuButton     btnSettings;
    private MenuButton     btnExit;

    public MenuScene() {
        this.name = "menu";
    }

    @Override
    public void onEnter(SceneContext context) {
        // compute layout from the current resolution so changes via setResolution take effect
        centreX       = Settings.getWindowWidth()  / 2f;
        firstButtonY  = Settings.getWindowHeight() * 0.45f;

        IAudioManager audio = context.get(IAudioManager.class);

        // start background music (asset pre-loaded in AudioManager.onInit)
        audio.playMusic("bgMusic", true);

        // ── generate fonts from TTF ──────────────────────────────
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(TTF_ASSET));

        // title font — larger, gold colour
        FreeTypeFontParameter titleParams = new FreeTypeFontParameter();
        titleParams.size  = 56;                              // pixel size (not scale)
        titleParams.color = new Color(1f, 0.92f, 0.55f, 1f); // gold
        titleParams.shadowOffsetX = 2;
        titleParams.shadowOffsetY = -2;
        titleParams.shadowColor   = new Color(0f, 0f, 0f, 0.5f);
        titleFont = generator.generateFont(titleParams);

        // button font — smaller, white
        FreeTypeFontParameter buttonParams = new FreeTypeFontParameter();
        buttonParams.size  = 26;
        buttonParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        buttonFont = generator.generateFont(buttonParams);

        // generator can be disposed immediately after generating fonts
        generator.dispose();

        // ── entities ─────────────────────────────────────────────
        background = new MenuBackground(BG_ASSET);
        titleText  = new TitleText("MATH QUEST MAZE", centreX,
                                   Settings.getWindowHeight() * 0.75f, titleFont);

        btnStart    = MenuButton.withTexture("START",
                        centreX, firstButtonY,                       buttonFont, BTN_ASSET, HOVER_ASSET);
        btnSettings = MenuButton.withTexture("SETTINGS",
                        centreX, firstButtonY - BUTTON_SPACING,      buttonFont, BTN_ASSET, HOVER_ASSET);
        btnExit     = MenuButton.withTexture("EXIT",
                        centreX, firstButtonY - BUTTON_SPACING * 2f, buttonFont, BTN_ASSET, HOVER_ASSET);
    }

    @Override
    public void onExit(SceneContext context) {
        if (btnStart    != null) btnStart.dispose();
        if (btnSettings != null) btnSettings.dispose();
        if (btnExit     != null) btnExit.dispose();
        if (background  != null) background.dispose();
        if (titleFont   != null) titleFont.dispose();
        if (buttonFont  != null) buttonFont.dispose();
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        btnStart.updateInput();
        btnSettings.updateInput();
        btnExit.updateInput();

        if (btnStart.isClicked()) {
            btnStart.resetClick();
            context.changeScene("level-complete"); // temporary test route until GameScene exists
            return;
        }
        if (btnSettings.isClicked()) {
            btnSettings.resetClick();
            context.changeScene("settings");
            return;
        }
        if (btnExit.isClicked()) {
            btnExit.resetClick();
            Gdx.app.exit();
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(titleText);
        renderQueue.queue(btnStart);
        renderQueue.queue(btnSettings);
        renderQueue.queue(btnExit);
    }

    // ── inner entities ────────────────────────────────────────────

    private static class MenuBackground implements IRenderItem {
        private final Transform2D transform;
        private final String      assetPath;
        private final Texture     texture;

        MenuBackground(String assetPath) {
            this.assetPath = assetPath;
            this.texture   = new Texture(Gdx.files.internal(assetPath));
            this.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            this.transform = new Transform2D(0, 0, Settings.getWindowWidth(), Settings.getWindowHeight());
        }

        @Override public String     getAssetPath() { return assetPath; }
        @Override public ITransform getTransform() { return transform; }
        void dispose() { if (texture != null) texture.dispose(); }
    }

    private static class TitleText extends Entity implements IRenderItem, ICustomRenderable {
        private final Transform2D transform;
        private final BitmapFont  font;
        private final String      text;

        TitleText(String text, float cx, float cy, BitmapFont font) {
            this.text      = text;
            this.font      = font;
            this.transform = new Transform2D(cx, cy, 0f, 0f);
        }

        @Override public String     getAssetPath() { return null; }
        @Override public ITransform getTransform() { return transform; }

        @Override
        public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
            ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();
            SpriteBatch   sb = ((GdxSpriteBatch)   batch).unwrap();
            sr.end();
            sb.begin();
            GlyphLayout layout = new GlyphLayout(font, text);
            font.draw(sb, text,
                transform.getPosition(0) - layout.width  / 2f,
                transform.getPosition(1) + layout.height / 2f);
            sb.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);
        }
    }
}

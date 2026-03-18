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
 * Settings scene for Math Quest Maze.
 *
 * Uses Kenney_Future.ttf via gdx-freetype for all text,
 * background.png for the background, and button*.png for buttons.
 *
 * Navigation:
 *   Mouse           - click buttons
 *   ESC / Backspace - back to menu
 */
public class SettingScene extends Scene {

    // ── asset paths ──────────────────────────────────────────────
    private static final String BG_ASSET    = "menu/background.png";
    private static final String BTN_ASSET   = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";
    private static final String TTF_ASSET   = "menu/Kenney_Future.ttf";

    // ── layout ───────────────────────────────────────────────────
    private static final float CENTRE_X = Settings.WINDOW_WIDTH  / 2f;
    private static final float CENTRE_Y = Settings.WINDOW_HEIGHT / 2f;

    // ── fonts ─────────────────────────────────────────────────────
    private BitmapFont headingFont;
    private BitmapFont labelFont;
    private BitmapFont buttonFont;

    // ── entities ─────────────────────────────────────────────────
    /** audio manager resolved once on scene entry */
    private IAudioManager      audio;

    private SettingsBackground background;
    private LabelText          heading;
    private LabelText          volumeLabel;
    private MenuButton         btnVolumeDown;
    private MenuButton         btnVolumeUp;
    private MenuButton         btnBack;

    public SettingScene() {
        this.name = "settings";
    }

    @Override
    public void onEnter(SceneContext context) {
        // ── generate all fonts from the same TTF ──────────────────
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(TTF_ASSET));

        FreeTypeFontParameter headingParams = new FreeTypeFontParameter();
        headingParams.size  = 52;
        headingParams.color = new Color(1f, 0.92f, 0.55f, 1f); // gold
        headingParams.shadowOffsetX = 2;
        headingParams.shadowOffsetY = -2;
        headingParams.shadowColor   = new Color(0f, 0f, 0f, 0.5f);
        headingFont = generator.generateFont(headingParams);

        FreeTypeFontParameter labelParams = new FreeTypeFontParameter();
        labelParams.size  = 28;
        labelParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        labelFont = generator.generateFont(labelParams);

        FreeTypeFontParameter buttonParams = new FreeTypeFontParameter();
        buttonParams.size  = 26;
        buttonParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        buttonFont = generator.generateFont(buttonParams);

        generator.dispose(); // safe to dispose after generating all fonts

        audio = context.get(IAudioManager.class);

        // ── entities ─────────────────────────────────────────────
        background  = new SettingsBackground(BG_ASSET);
        heading     = new LabelText("SETTINGS",   CENTRE_X, Settings.WINDOW_HEIGHT * 0.72f,
                                    headingFont);
        volumeLabel = new LabelText(volumeText(audio), CENTRE_X, CENTRE_Y + 60f,
                                    labelFont);

        btnVolumeDown = MenuButton.withTexture("-",    CENTRE_X - 170f, CENTRE_Y - 10f,  buttonFont, BTN_ASSET, HOVER_ASSET);
        btnVolumeUp   = MenuButton.withTexture("+",    CENTRE_X + 170f, CENTRE_Y - 10f,  buttonFont, BTN_ASSET, HOVER_ASSET);
        btnBack       = MenuButton.withTexture("BACK", CENTRE_X,        CENTRE_Y - 120f, buttonFont, BTN_ASSET, HOVER_ASSET);
    }

    @Override
    public void onExit(SceneContext context) {
        if (btnVolumeDown != null) btnVolumeDown.dispose();
        if (btnVolumeUp   != null) btnVolumeUp.dispose();
        if (btnBack       != null) btnBack.dispose();
        if (background    != null) background.dispose();
        if (headingFont   != null) headingFont.dispose();
        if (labelFont     != null) labelFont.dispose();
        if (buttonFont    != null) buttonFont.dispose();
        audio = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            context.changeScene("menu");
            return;
        }

        btnVolumeDown.updateInput();
        btnVolumeUp.updateInput();
        btnBack.updateInput();

        if (btnVolumeDown.isClicked()) {
            btnVolumeDown.resetClick();
            audio.setMusicVolume(audio.getMusicVolume() - 0.1f);
            volumeLabel.setText(volumeText(audio));
        }
        if (btnVolumeUp.isClicked()) {
            btnVolumeUp.resetClick();
            audio.setMusicVolume(audio.getMusicVolume() + 0.1f);
            volumeLabel.setText(volumeText(audio));
        }
        if (btnBack.isClicked()) {
            btnBack.resetClick();
            context.changeScene("menu");
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(heading);
        renderQueue.queue(volumeLabel);
        renderQueue.queue(btnVolumeDown);
        renderQueue.queue(btnVolumeUp);
        renderQueue.queue(btnBack);
    }

    private String volumeText(IAudioManager audio) {
        return "Music Volume:  " + Math.round(audio.getMusicVolume() * 100) + "%";
    }

    // ── inner entities ────────────────────────────────────────────

    private static class SettingsBackground implements IRenderItem {
        private final Transform2D transform;
        private final String      assetPath;
        private final Texture     texture;

        SettingsBackground(String assetPath) {
            this.assetPath = assetPath;
            this.texture   = new Texture(Gdx.files.internal(assetPath));
            this.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            this.transform = new Transform2D(0, 0, Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
        }

        @Override public String     getAssetPath() { return assetPath; }
        @Override public ITransform getTransform() { return transform; }
        void dispose() { if (texture != null) texture.dispose(); }
    }

    private static class LabelText extends Entity implements IRenderItem, ICustomRenderable {
        private final Transform2D transform;
        private final BitmapFont  font;
        private       String      text;

        LabelText(String text, float cx, float cy, BitmapFont font) {
            this.text      = text;
            this.font      = font;
            this.transform = new Transform2D(cx, cy, 0f, 0f);
        }

        void setText(String t) { this.text = t; }

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

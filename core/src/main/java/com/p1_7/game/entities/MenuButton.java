package com.p1_7.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxShapeRenderer;
import com.p1_7.game.platform.GdxSpriteBatch;
import com.p1_7.game.Settings;

/**
 * Reusable UI button for menus. Supports two modes:
 *
 *   TEXTURED  – draws a PNG for the button body; hover applies a colour tint.
 *               Create via MenuButton.withTexture(...).
 *
 *   PROCEDURAL – draws a plain coloured rectangle (no image files needed).
 *               Create via new MenuButton(...).
 *
 * In both modes the label is drawn centred over the button with the
 * supplied BitmapFont. The font is owned by the scene, not the button.
 *
 * Call updateInput() every frame, then check isClicked().
 * Call resetClick() after handling the action so it fires only once.
 * Call dispose() inside the scene's onExit() to free GPU resources.
 */
public class MenuButton extends Entity implements IRenderItem, ICustomRenderable {

    // ── dimensions ──────────────────────────────────────────────
    public static final float BUTTON_WIDTH  = 260f;
    public static final float BUTTON_HEIGHT = 55f;

    // ── procedural fallback colours ──────────────────────────────
    private static final Color COLOUR_NORMAL = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_HOVER  = new Color(0.35f, 0.35f, 0.80f, 1f);
    private static final Color COLOUR_BORDER = new Color(0.80f, 0.80f, 1.00f, 1f);
    private static final float BORDER_THICK  = 3f;

    // ── textured hover tints ─────────────────────────────────────
    private static final Color TINT_NORMAL = Color.WHITE.cpy();
    private static final Color TINT_HOVER  = new Color(0.75f, 0.88f, 1.0f, 1f);

    // ── state ────────────────────────────────────────────────────
    private final Transform2D transform;
    private final String      label;
    private final BitmapFont  font;
    private final GlyphLayout layout;

    /** null in procedural mode */
    private final Texture texNormal;
    /** null in procedural mode; may equal texNormal if no separate hover image */
    private final Texture texHover;

    private boolean hovered = false;
    private boolean clicked = false;

    // ── factory method (textured) ────────────────────────────────

    /**
     * Creates a textured button that loads its own PNG files.
     *
     * @param label       text shown on the button
     * @param centreX     horizontal centre in world coordinates
     * @param centreY     vertical centre in world coordinates
     * @param font        BitmapFont owned by the scene (not disposed by this button)
     * @param normalPath  asset path for the normal state, e.g. "menu/button.png"
     * @param hoverPath   asset path for the hover state,  e.g. "menu/button_hover.png"
     *                    Pass null to reuse normalPath with a colour tint on hover.
     */
    public static MenuButton withTexture(String label,
                                         float centreX, float centreY,
                                         BitmapFont font,
                                         String normalPath,
                                         String hoverPath) {
        Texture normal = new Texture(Gdx.files.internal(normalPath));
        normal.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture hover = (hoverPath != null)
                ? new Texture(Gdx.files.internal(hoverPath))
                : normal;                        // reuse normal, tinted in renderCustom
        if (hoverPath != null) {
            hover.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        return new MenuButton(label, centreX, centreY, font, normal, hover);
    }

    // ── constructors ─────────────────────────────────────────────

    /**
     * Procedural constructor — no image assets required.
     *
     * @param label   text shown on the button
     * @param centreX horizontal centre in world coordinates
     * @param centreY vertical centre in world coordinates
     * @param font    BitmapFont owned by the scene
     */
    public MenuButton(String label, float centreX, float centreY, BitmapFont font) {
        this(label, centreX, centreY, font, null, null);
    }

    /** Full internal constructor used by both modes. */
    private MenuButton(String label, float centreX, float centreY,
                       BitmapFont font, Texture normal, Texture hover) {
        float x = centreX - BUTTON_WIDTH  / 2f;
        float y = centreY - BUTTON_HEIGHT / 2f;
        this.transform = new Transform2D(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        this.label     = label;
        this.font      = font;
        this.layout    = new GlyphLayout(font, label);
        this.texNormal = normal;
        this.texHover  = hover;
    }

    // ── per-frame input ──────────────────────────────────────────

    /**
     * Polls mouse position and click state.
     * Call once per frame from the scene's update().
     *
     * LibGDX reports Y from the top of the window; we flip it to
     * match the bottom-left world origin used by the render system.
     */
    public void updateInput() {
        float mx = Gdx.input.getX();
        float my = Settings.WINDOW_HEIGHT - Gdx.input.getY();

        float bx = transform.getPosition(0);
        float by = transform.getPosition(1);

        hovered = mx >= bx && mx <= bx + BUTTON_WIDTH
               && my >= by && my <= by + BUTTON_HEIGHT;

        if (hovered && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            clicked = true;
        }
    }

    /** True if the button was clicked this frame. */
    public boolean isClicked() { return clicked; }

    /** Clears the click flag — call after handling the action. */
    public void resetClick()   { clicked = false; }

    // ── IRenderItem ──────────────────────────────────────────────

    /** Returns null — always uses the custom render path. */
    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    // ── ICustomRenderable ────────────────────────────────────────

    /**
     * Draws the button background (textured or procedural) then the label.
     *
     * The engine leaves the ShapeRenderer open when it calls renderCustom(),
     * so we end it before opening the SpriteBatch and restore it afterwards
     * so that subsequent custom renderables work correctly.
     */
    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        SpriteBatch   sb = ((GdxSpriteBatch)   batch).unwrap();
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();

        float x = transform.getPosition(0);
        float y = transform.getPosition(1);

        if (texNormal != null) {
            // ── textured mode ──────────────────────────────────
            sr.end();
            sb.begin();

            sb.setColor(hovered ? TINT_HOVER : TINT_NORMAL);
            sb.draw(hovered ? texHover : texNormal, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);

            // draw centred label
            layout.setText(font, label);
            font.setColor(Color.WHITE);
            font.draw(sb, label,
                x + (BUTTON_WIDTH  - layout.width)  / 2f,
                y + (BUTTON_HEIGHT + layout.height) / 2f);

            sb.setColor(Color.WHITE);   // reset tint
            sb.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);

        } else {
            // ── procedural fallback ────────────────────────────
            sr.setColor(COLOUR_BORDER);
            sr.rect(x - BORDER_THICK, y - BORDER_THICK,
                    BUTTON_WIDTH  + BORDER_THICK * 2,
                    BUTTON_HEIGHT + BORDER_THICK * 2);

            sr.setColor(hovered ? COLOUR_HOVER : COLOUR_NORMAL);
            sr.rect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);

            sr.end();
            sb.begin();
            layout.setText(font, label);
            font.setColor(Color.WHITE);
            font.draw(sb, label,
                x + (BUTTON_WIDTH  - layout.width)  / 2f,
                y + (BUTTON_HEIGHT + layout.height) / 2f);
            sb.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);
        }
    }

    /**
     * Releases textures owned by this button.
     * The font is NOT disposed — the scene owns it.
     */
    public void dispose() {
        if (texNormal != null) texNormal.dispose();
        if (texHover != null && texHover != texNormal) texHover.dispose();
    }
}
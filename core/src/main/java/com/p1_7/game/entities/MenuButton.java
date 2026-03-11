package com.p1_7.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
 * A reusable UI button entity for menus.
 *
 * Draws a filled rectangle with a centred label on top.
 * Detects mouse hover (changes colour) and left-click.
 * The owning scene should call isClicked() each frame and
 * reset it with resetClick() after handling the action.
 *
 * Design note: uses ICustomRenderable so it controls its own
 * draw call, keeping button appearance logic fully self-contained
 * (Single Responsibility Principle).
 */
public class MenuButton extends Entity implements IRenderItem, ICustomRenderable {

    // ── dimensions ──────────────────────────────────────────────
    public static final float BUTTON_WIDTH  = 260f;
    public static final float BUTTON_HEIGHT = 55f;

    // ── colours ──────────────────────────────────────────────────
    private static final Color COLOUR_NORMAL  = new Color(0.20f, 0.20f, 0.55f, 1f); // dark blue
    private static final Color COLOUR_HOVER   = new Color(0.35f, 0.35f, 0.80f, 1f); // lighter blue
    private static final Color COLOUR_BORDER  = new Color(0.80f, 0.80f, 1.00f, 1f); // light blue border
    private static final Color COLOUR_TEXT    = Color.WHITE;
    private static final float BORDER_THICK   = 3f;

    // ── state ────────────────────────────────────────────────────
    private final Transform2D transform;
    private final String      label;
    private final BitmapFont  font;
    private final GlyphLayout layout; // used to measure text for centring
    private boolean           hovered = false;
    private boolean           clicked = false;

    /**
     * Creates a button centred at (centreX, centreY).
     *
     * @param label   text displayed on the button
     * @param centreX horizontal centre of the button in world coordinates
     * @param centreY vertical centre of the button in world coordinates
     * @param scale   font scale (1.5f looks good for menu buttons)
     */
    public MenuButton(String label, float centreX, float centreY, float scale) {
        float x = centreX - BUTTON_WIDTH  / 2f;
        float y = centreY - BUTTON_HEIGHT / 2f;
        this.transform = new Transform2D(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        this.label     = label;
        this.font      = new BitmapFont();
        this.font.getData().setScale(scale);
        this.font.setColor(COLOUR_TEXT);
        this.layout    = new GlyphLayout(font, label);
    }

    // ── per-frame state update ───────────────────────────────────

    /**
     * Updates hover and click state from the current mouse position.
     * Call this once per frame from the owning scene's update() method.
     *
     * LibGDX reports mouse Y from the top of the screen, so we flip it
     * to match the bottom-left origin used by the render system.
     */
    public void updateInput() {
        float mx = Gdx.input.getX();
        float my = Settings.WINDOW_HEIGHT - Gdx.input.getY(); // flip Y axis

        float bx = transform.getPosition(0);
        float by = transform.getPosition(1);
        float bw = transform.getSize(0);
        float bh = transform.getSize(1);

        hovered = mx >= bx && mx <= bx + bw
               && my >= by && my <= by + bh;

        // register click only on the frame the left button is released
        if (hovered && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            clicked = true;
        }
    }

    /**
     * Returns true if the button was clicked this frame.
     * Always call resetClick() after handling so it fires only once.
     *
     * @return true if a click was detected
     */
    public boolean isClicked() {
        return clicked;
    }

    /**
     * Clears the click flag. Call after handling the click action.
     */
    public void resetClick() {
        clicked = false;
    }

    // ── IRenderItem / ICustomRenderable ──────────────────────────

    /** Returns null to signal custom rendering (no sprite asset). */
    @Override
    public String getAssetPath() {
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    /**
     * Draws the button: border → fill → centred label.
     *
     * The render manager leaves the ShapeRenderer open when it calls
     * renderCustom(), so we must end it before the SpriteBatch (for
     * text), then reopen it so later renderables work correctly.
     */
    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();

        float x  = transform.getPosition(0);
        float y  = transform.getPosition(1);
        float w  = transform.getSize(0);
        float h  = transform.getSize(1);

        // ── draw border (slightly larger rect behind the fill) ──
        sr.setColor(COLOUR_BORDER);
        sr.rect(x - BORDER_THICK, y - BORDER_THICK,
                w + BORDER_THICK * 2, h + BORDER_THICK * 2);

        // ── draw button fill ────────────────────────────────────
        sr.setColor(hovered ? COLOUR_HOVER : COLOUR_NORMAL);
        sr.rect(x, y, w, h);

        // ── draw label (needs SpriteBatch, so swap renderers) ───
        sr.end();
        ((GdxSpriteBatch) batch).unwrap().begin();

        // recompute layout in case scale changed (safe to call each frame)
        layout.setText(font, label);
        float textX = x + (w - layout.width)  / 2f;
        float textY = y + (h + layout.height)  / 2f;
        font.draw(((GdxSpriteBatch) batch).unwrap(), label, textX, textY);

        ((GdxSpriteBatch) batch).unwrap().end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }

    /** Frees the BitmapFont — call from the scene's onExit(). */
    public void dispose() {
        font.dispose();
    }
}
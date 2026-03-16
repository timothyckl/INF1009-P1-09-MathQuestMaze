package com.p1_7.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Rectangle2D;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxShapeRenderer;
import com.p1_7.game.platform.GdxSpriteBatch;

/**
 * collectible answer tile placed inside the maze.
 *
 * displays a numeric value on a coloured tile. when the player steps on it,
 * the AnswerTileHandler is notified with whether the value is the correct
 * answer to the current question. fires only once per encounter (triggered
 * flag prevents repeated callbacks while the player stands on it).
 *
 * the scene should call resetTrigger() on all tiles when a new question is
 * generated, and call setCorrect() to update which tile holds the right answer.
 *
 * do NOT register with MovementManager (tiles are static).
 * DO register with CollisionManager.
 *
 * the BitmapFont is owned by the scene — do not dispose it here.
 */
public class AnswerTile extends Entity implements IRenderItem, ICollidable, ICustomRenderable {

    public static final float TILE_SIZE = 64f;

    // tile background colour (same for all tiles — player must deduce the answer)
    private static final Color COLOUR_BG     = new Color(0.20f, 0.40f, 0.80f, 1f); // blue
    private static final Color COLOUR_BORDER = new Color(0.10f, 0.20f, 0.50f, 1f);
    private static final float BORDER        = 3f;

    private final Transform2D transform;
    private final Rectangle2D bounds = new Rectangle2D();

    private final int        value;
    private       boolean    isCorrect;
    private       boolean    triggered = false;

    private final BitmapFont font;
    private final GlyphLayout layout;

    private AnswerTileHandler handler;

    /**
     * @param x         left edge in world coordinates
     * @param y         bottom edge in world coordinates
     * @param value     the number to display on the tile
     * @param isCorrect true if this tile holds the correct answer
     * @param font      BitmapFont owned by the scene (not disposed here)
     */
    public AnswerTile(float x, float y, int value, boolean isCorrect, BitmapFont font) {
        super();
        this.transform = new Transform2D(x, y, TILE_SIZE, TILE_SIZE);
        this.value     = value;
        this.isCorrect = isCorrect;
        this.font      = font;
        this.layout    = new GlyphLayout(font, String.valueOf(value));
    }

    /** sets the handler notified when the player steps on this tile. */
    public void setHandler(AnswerTileHandler handler) {
        this.handler = handler;
    }

    /** updates whether this tile is currently the correct answer. */
    public void setCorrect(boolean correct) { this.isCorrect = correct; }

    /** allows this tile to fire again (call when a new question is generated). */
    public void resetTrigger() { this.triggered = false; }

    public int     getValue()    { return value; }
    public boolean isCorrect()   { return isCorrect; }

    // ── IRenderItem ──────────────────────────────────────────────────────────

    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    // ── ICollidable ──────────────────────────────────────────────────────────

    @Override
    public IBounds getBounds() {
        bounds.set(
            transform.getPosition(0), transform.getPosition(1),
            TILE_SIZE, TILE_SIZE);
        return bounds;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (!triggered && other instanceof Player && handler != null) {
            triggered = true;
            handler.onAnswer(isCorrect, value);
        }
    }

    // ── ICustomRenderable ────────────────────────────────────────────────────

    /**
     * draws the tile background then centres the number on it.
     *
     * the engine holds ShapeRenderer open in Filled mode when this is called.
     * we must end it before opening SpriteBatch for font drawing, then
     * reopen ShapeRenderer so subsequent renderables work correctly.
     */
    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();
        SpriteBatch   sb = ((GdxSpriteBatch)   batch).unwrap();

        float x = transform.getPosition(0);
        float y = transform.getPosition(1);

        // draw border then tile background
        sr.setColor(COLOUR_BORDER);
        sr.rect(x - BORDER, y - BORDER, TILE_SIZE + BORDER * 2, TILE_SIZE + BORDER * 2);
        sr.setColor(COLOUR_BG);
        sr.rect(x, y, TILE_SIZE, TILE_SIZE);

        // switch to sprite batch for the number label
        sr.end();
        sb.begin();

        String text = String.valueOf(value);
        layout.setText(font, text);
        font.setColor(Color.WHITE);
        font.draw(sb, text,
            x + (TILE_SIZE - layout.width)  / 2f,
            y + (TILE_SIZE + layout.height) / 2f);

        sb.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }
}

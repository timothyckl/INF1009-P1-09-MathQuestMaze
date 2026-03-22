package com.p1_7.game.scenes.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Immutable centred text label for use within scene UI.
 *
 * Text is fixed at construction time — use DynamicLabel when the
 * displayed string must change at runtime.
 */
public class StaticLabel extends Entity implements IRenderable {

    /** text content displayed by this label */
    protected String text;

    /** font used to measure and draw the text */
    private final BitmapFont font;

    /** transform storing the logical centre position of this label */
    private final Transform2D transform;

    /**
     * constructs a static label centred at the given coordinates.
     *
     * @param text    the text to display; never updated after construction
     * @param centreX horizontal centre position in screen coordinates
     * @param centreY vertical centre position in screen coordinates
     * @param font    bitmap font used to render the text
     */
    public StaticLabel(String text, float centreX, float centreY, BitmapFont font) {
        this.text = text;
        this.font = font;
        this.transform = new Transform2D(centreX, centreY, 0f, 0f);
    }

    @Override
    public String getAssetPath() {
        // procedural rendering; no external asset required
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    /**
     * renders the label centred at its stored position.
     *
     * @param ctx the draw context used for font rendering
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        GlyphLayout layout = new GlyphLayout(font, text);
        gdxCtx.drawFont(font, text,
            transform.getPosition(0) - layout.width  / 2f,
            transform.getPosition(1) + layout.height / 2f);
    }
}

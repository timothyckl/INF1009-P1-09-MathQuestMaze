package com.p1_7.game.entities;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Reusable centered text renderable for game and scene text.
 */
public class Text extends Entity implements IRenderable {

    private final Transform2D transform;
    private final BitmapFont font;
    private String text;

    public Text(String text, float centreX, float centreY, BitmapFont font) {
        this.text = text;
        this.font = font;
        this.transform = new Transform2D(centreX, centreY, 0f, 0f);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getAssetPath() {
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        GlyphLayout layout = new GlyphLayout(font, text);
        gdxCtx.drawFont(font, text,
            transform.getPosition(0) - layout.width / 2f,
            transform.getPosition(1) + layout.height / 2f);
    }
}

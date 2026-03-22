package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.GameViewport;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Solid top bar reserved for HUD-only content.
 */
public final class HudStrip extends Entity implements IRenderable {

    // TODO palette: replace in palette issue
    private static final Color STRIP_COLOUR = new Color(0.11f, 0.14f, 0.20f, 1f);

    private final Transform2D transform = new Transform2D(
        0f,
        GameViewport.HUD_STRIP_Y,
        GameViewport.SCREEN_WIDTH,
        GameViewport.HUD_STRIP_HEIGHT
    );

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
        ((GdxDrawContext) ctx).drawTintedQuad(
            STRIP_COLOUR,
            transform.getPosition(0),
            transform.getPosition(1),
            transform.getSize(0),
            transform.getSize(1)
        );
    }
}

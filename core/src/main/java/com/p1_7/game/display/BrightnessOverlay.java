package com.p1_7.game.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxShapeRenderer;

/*
Brightness Overlay is a full-screen procedural overlay that darkens the screen based on the user's brightness setting.
it doesn't render if brightness level is close to 100% to optimize performance and prevent unnecessary blending.
Brightness Level could technically go above 100%, but for now limited to 100%
*/
public class BrightnessOverlay extends Entity implements IRenderItem, ICustomRenderable {

    private final Transform2D transform;

    public BrightnessOverlay(float screenWidth, float screenHeight) {
        this.transform = new Transform2D(0, 0, screenWidth, screenHeight);
    }

    @Override
    public String getAssetPath() {
        return null; // Null signals the RenderManager to use renderCustom()
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();
        float alpha = 1.0f - Settings.BRIGHTNESS_LEVEL; 
        
        // Doesn't draw if fully bright
        if (alpha <= 0.01f) return;
        
        float posX = transform.getPosition(0);
        float posY = transform.getPosition(1);
        float width = transform.getSize(0);
        float height = transform.getSize(1);
        
        sr.end();

        // blend the brightness overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, alpha); 
        sr.rect(posX, posY, width, height);
        sr.end();

        // blending bleeding bug fix
        Gdx.gl.glDisable(GL20.GL_BLEND);
        
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }
}
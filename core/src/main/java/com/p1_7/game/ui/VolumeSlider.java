package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Horizontal volume slider entity.
 */
public final class VolumeSlider extends Entity implements IRenderable {

    private static final float KNOB_RADIUS = 14f;
    private static final float TRACK_HALF_H = 5f;

    private static final Color COLOUR_TRACK = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_FILLED = new Color(0.80f, 0.80f, 1.00f, 1f);
    private static final Color COLOUR_KNOB = new Color(0.35f, 0.35f, 0.80f, 1f);
    private static final Color COLOUR_KNOB_DRAG = Color.WHITE;

    private final Transform2D transform;
    private final float trackLeft;
    private final float trackCentreY;
    private final float trackWidth;
    private final float knobRadius;

    private float value;
    private boolean dragging = false;
    private boolean moved = false;

    public VolumeSlider(float centreX, float centreY, float trackWidth, float initialValue) {
        this.trackLeft = centreX - trackWidth / 2f;
        this.trackCentreY = centreY;
        this.trackWidth = trackWidth;
        this.knobRadius = KNOB_RADIUS;
        this.value = initialValue;
        this.transform = new Transform2D(
            trackLeft,
            centreY - knobRadius,
            trackWidth,
            knobRadius * 2f
        );
    }

    public void updateInput(ICursorSource cursor, IInputQuery inputQuery) {
        float mx = cursor.getCursorX();
        float my = cursor.getCursorY();
        float knobX = trackLeft + value * trackWidth;
        InputState pointerState = inputQuery.getActionState(GameActions.POINTER_PRIMARY);

        moved = false;

        if (pointerState == InputState.PRESSED) {
            float dx = mx - knobX;
            float dy = my - trackCentreY;
            float hitRadius = knobRadius * 1.5f;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                dragging = true;
            }
        }

        if (dragging && (pointerState == InputState.PRESSED || pointerState == InputState.HELD)) {
            float clamped = Math.max(trackLeft, Math.min(trackLeft + trackWidth, mx));
            value = (clamped - trackLeft) / trackWidth;
            moved = true;
        }

        if (pointerState == null || pointerState == InputState.RELEASED) {
            dragging = false;
        }
    }

    public float getValue() {
        return value;
    }

    public boolean hasMoved() {
        return moved;
    }

    public void resetMoved() {
        moved = false;
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
        float knobX = trackLeft + value * trackWidth;

        gdxCtx.rect(COLOUR_TRACK, trackLeft, trackCentreY - TRACK_HALF_H,
            trackWidth, TRACK_HALF_H * 2f, true);
        gdxCtx.rect(COLOUR_FILLED, trackLeft, trackCentreY - TRACK_HALF_H,
            value * trackWidth, TRACK_HALF_H * 2f, true);
        gdxCtx.circle(dragging ? COLOUR_KNOB_DRAG : COLOUR_KNOB,
            knobX, trackCentreY, knobRadius, true);
    }

}

package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Horizontal brightness slider entity.
 *
 * Behaves like VolumeSlider, but writes the dragged value directly through
 * Settings.setBrightnessLevel(float). The slider range is [0.1, 1.0].
 */
public class BrightnessSlider extends Entity implements IRenderable {

    private static final float KNOB_RADIUS  = 14f;
    private static final float TRACK_HALF_H = 5f;
    private static final float MIN_VALUE    = Settings.MIN_BRIGHTNESS_LEVEL;
    private static final float VALUE_RANGE  = 1.0f - MIN_VALUE;

    private static final Color COLOUR_TRACK     = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_FILLED    = new Color(1.00f, 0.88f, 0.42f, 1f);
    private static final Color COLOUR_KNOB      = new Color(0.82f, 0.64f, 0.18f, 1f);
    private static final Color COLOUR_KNOB_DRAG = Color.WHITE;

    private final Transform2D transform;
    private final float       trackLeft;
    private final float       trackCentreY;
    private final float       trackWidth;
    private final float       knobRadius;

    private float   value;
    private boolean dragging = false;
    private boolean moved    = false;

    /**
     * Creates a brightness slider centred at (centreX, centreY).
     *
     * @param centreX      horizontal centre of the slider in world coordinates
     * @param centreY      vertical centre of the slider in world coordinates
     * @param trackWidth   pixel span of the full draggable range
     * @param initialValue starting brightness level in [0.1, 1.0]
     */
    public BrightnessSlider(float centreX, float centreY, float trackWidth, float initialValue) {
        this.trackLeft    = centreX - trackWidth / 2f;
        this.trackCentreY = centreY;
        this.trackWidth   = trackWidth;
        this.knobRadius   = KNOB_RADIUS;
        this.value        = clampBrightness(initialValue);

        this.transform = new Transform2D(
            trackLeft,
            centreY - knobRadius,
            trackWidth,
            knobRadius * 2f
        );
    }

    /**
     * Polls cursor position and drag state using the provided cursor source.
     * Call once per frame from the scene's update().
     *
     * @param cursor the world-space cursor source (Y-flip already applied)
     * @param inputQuery the logical input query for this frame
     */
    public void updateInput(ICursorSource cursor, IInputQuery inputQuery) {
        float mx    = cursor.getCursorX();
        float my    = cursor.getCursorY();
        float knobX = trackLeft + normalizedValue() * trackWidth;
        InputState pointerState = inputQuery.getActionState(GameActions.POINTER_PRIMARY);

        moved = false;

        if (pointerState == InputState.PRESSED) {
            float dx        = mx - knobX;
            float dy        = my - trackCentreY;
            float hitRadius = knobRadius * 1.5f;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                dragging = true;
            }
        }

        if (dragging && (pointerState == InputState.PRESSED || pointerState == InputState.HELD)) {
            float clampedTrackX = Math.max(trackLeft, Math.min(trackLeft + trackWidth, mx));
            float normalized    = (clampedTrackX - trackLeft) / trackWidth;
            value = clampBrightness(MIN_VALUE + normalized * VALUE_RANGE);
            Settings.setBrightnessLevel(value);
            moved = true;
        }

        if (pointerState == null || pointerState == InputState.RELEASED) {
            dragging = false;
        }
    }

    /**
     * Returns the current brightness value.
     *
     * @return brightness level in [0.1, 1.0]
     */
    public float getValue() { return value; }

    /**
     * Returns true if the slider value changed this frame.
     *
     * @return true when a drag moved the knob since the last resetMoved() call
     */
    public boolean hasMoved() { return moved; }

    /** Clears the moved flag — call after handling the value change. */
    public void resetMoved() { moved = false; }

    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    /**
     * Draws the track, filled portion, and knob.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        float normalized = normalizedValue();
        float knobX      = trackLeft + normalized * trackWidth;

        gdxCtx.rect(COLOUR_TRACK,  trackLeft, trackCentreY - TRACK_HALF_H,
                    trackWidth,              TRACK_HALF_H * 2f, true);
        gdxCtx.rect(COLOUR_FILLED, trackLeft, trackCentreY - TRACK_HALF_H,
                    normalized * trackWidth, TRACK_HALF_H * 2f, true);
        gdxCtx.circle(dragging ? COLOUR_KNOB_DRAG : COLOUR_KNOB,
                      knobX, trackCentreY, knobRadius, true);
    }

    private float normalizedValue() {
        return (value - MIN_VALUE) / VALUE_RANGE;
    }

    private float clampBrightness(float level) {
        return Math.max(MIN_VALUE, Math.min(1.0f, level));
    }
}

package com.p1_7.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Horizontal volume slider entity.
 *
 * Renders a coloured track with a draggable circular knob. The knob position
 * maps linearly to a [0.0, 1.0] value that callers read and forward to the
 * audio manager.
 *
 * Usage pattern mirrors MenuButton:
 *   call updateInput() every frame, check hasMoved(), then resetMoved().
 *   call dispose() in the scene's onExit().
 */
public class VolumeSlider extends Entity implements IRenderable {

    // ── dimensions ──────────────────────────────────────────────
    private static final float KNOB_RADIUS  = 14f;
    private static final float TRACK_HALF_H = 5f;

    // ── colours (matching MenuButton procedural palette) ────────
    private static final Color COLOUR_TRACK     = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_FILLED    = new Color(0.80f, 0.80f, 1.00f, 1f);
    private static final Color COLOUR_KNOB      = new Color(0.35f, 0.35f, 0.80f, 1f);
    private static final Color COLOUR_KNOB_DRAG = Color.WHITE;

    // ── geometry ────────────────────────────────────────────────
    private final Transform2D transform;
    private final float       trackLeft;
    private final float       trackCentreY;
    private final float       trackWidth;
    private final float       knobRadius;

    // ── state ────────────────────────────────────────────────────
    /** current volume in [0.0, 1.0] */
    private float   value;
    /** true while the left mouse button is held on the knob */
    private boolean dragging = false;
    /** single-frame dirty flag; true when value changed this frame */
    private boolean moved    = false;

    // ── constructor ──────────────────────────────────────────────

    /**
     * Creates a volume slider centred at (centreX, centreY).
     *
     * @param centreX      horizontal centre of the slider in world coordinates
     * @param centreY      vertical centre of the slider in world coordinates
     * @param trackWidth   pixel span of the full draggable range
     * @param initialValue starting knob position in [0.0, 1.0]; pass
     *                     {@code IAudioManager.getMusicVolume()} from the scene
     */
    public VolumeSlider(float centreX, float centreY, float trackWidth, float initialValue) {
        this.trackLeft    = centreX - trackWidth / 2f;
        this.trackCentreY = centreY;
        this.trackWidth   = trackWidth;
        this.knobRadius   = KNOB_RADIUS;
        this.value        = initialValue;

        // bounding box encompasses the knob travel area
        this.transform = new Transform2D(
            trackLeft,
            centreY - knobRadius,
            trackWidth,
            knobRadius * 2f
        );
    }

    // ── per-frame input ──────────────────────────────────────────

    /**
     * Polls cursor position and drag state using the provided cursor source.
     * Call once per frame from the scene's update().
     *
     * @param cursor the world-space cursor source (Y-flip already applied)
     */
    public void updateInput(ICursorSource cursor) {
        float mx    = cursor.getCursorX();
        float my    = cursor.getCursorY();
        float knobX = trackLeft + value * trackWidth;

        moved = false;

        // start drag on press within the knob hit-area (1.5× radius for easier targeting)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float dx           = mx - knobX;
            float dy           = my - trackCentreY;
            float hitRadius    = knobRadius * 1.5f;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                dragging = true;
            }
        }

        // update value while dragging
        if (dragging && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            float clamped = Math.max(trackLeft, Math.min(trackLeft + trackWidth, mx));
            value = (clamped - trackLeft) / trackWidth;
            moved = true;
        }

        // release drag when button is no longer held
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            dragging = false;
        }
    }

    // ── state query ──────────────────────────────────────────────

    /**
     * Returns the current slider value.
     *
     * @return value in [0.0, 1.0]
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

    // ── IRenderable ──────────────────────────────────────────────

    /** Returns null — this entity owns no managed assets. */
    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    /**
     * Draws the track, filled portion, and knob.
     * Pass transitions are managed by GdxDrawContext; no begin/end calls here.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        float knobX = trackLeft + value * trackWidth;

        // unfilled (background) track
        gdxCtx.rect(COLOUR_TRACK,  trackLeft, trackCentreY - TRACK_HALF_H,
                    trackWidth,              TRACK_HALF_H * 2f, true);

        // filled (left) portion indicating the current volume level
        gdxCtx.rect(COLOUR_FILLED, trackLeft, trackCentreY - TRACK_HALF_H,
                    value * trackWidth,      TRACK_HALF_H * 2f, true);

        // draggable knob; colour changes while held
        gdxCtx.circle(dragging ? COLOUR_KNOB_DRAG : COLOUR_KNOB,
                      knobX, trackCentreY, knobRadius, true);
    }

    /**
     * No-op — this entity owns no GPU resources.
     * Included for lifecycle symmetry with MenuButton.
     */
    public void dispose() { }
}

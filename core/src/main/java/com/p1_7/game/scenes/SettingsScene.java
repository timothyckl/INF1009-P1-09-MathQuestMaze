package com.p1_7.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.ActionId;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.input.InputMapping;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.display.HoverableTextDisplay;
import com.p1_7.game.display.Slider;
import com.p1_7.game.display.TextDisplay;
import com.p1_7.game.display.Background;
import com.p1_7.game.display.BrightnessOverlay;
import com.p1_7.game.entities.MousePointer;
import com.p1_7.game.input.MappableActions;

import java.util.ArrayList;
import java.util.List;

public class SettingsScene extends Scene {

    private final InputManager inputManager;

    private float scrollOffset = 0f;
    private float targetScroll = 0f;
    private final float maxScroll = 600f;

    private Background background;
    private BrightnessOverlay brightnessOverlay;
    private MousePointer mousePointer;
    private final List<Entity> uiEntities = new ArrayList<>();
    
    private Slider volSlider;
    private Slider brightSlider;
    private final List<RemapSlot> remapSlots = new ArrayList<>();
    
    private boolean isListeningForRemap = false;
    private RemapSlot activeRemapSlot = null;

    private class RemapSlot {
        ActionId action;
        int currentKey; 
        HoverableTextDisplay display;

        RemapSlot(ActionId action, int currentKey, HoverableTextDisplay display) {
            this.action = action;
            this.currentKey = currentKey;
            this.display = display;
        }
    }

    public SettingsScene(InputManager inputManager) {
        this.name = "settings";
        this.inputManager = inputManager;
    }

    @Override
    public void onEnter(SceneContext context) {
        uiEntities.clear();
        remapSlots.clear();
        scrollOffset = 0f;
        targetScroll = 0f;

        background = new Background(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
        brightnessOverlay = (BrightnessOverlay) context.entities().createEntity(() -> new BrightnessOverlay(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT));
        mousePointer = (MousePointer) context.entities().createEntity(() -> new MousePointer());

        uiEntities.add(context.entities().createEntity(() -> new TextDisplay("AUDIO VOLUME", 100, 500, 1.5f)));
        volSlider = (Slider) context.entities().createEntity(() -> new Slider(100.0f, 450.0f, 400.0f, 30.0f, Settings.VOLUME_LEVEL));
        uiEntities.add(volSlider);

        uiEntities.add(context.entities().createEntity(() -> new TextDisplay("BRIGHTNESS LEVEL", 100, 350, 1.5f)));
        brightSlider = (Slider) context.entities().createEntity(() -> new Slider(100, 300, 400, 30, Settings.BRIGHTNESS_LEVEL));
        uiEntities.add(brightSlider);

        uiEntities.add(context.entities().createEntity(() -> new TextDisplay("KEY MAPPINGS", 100, 250, 1.5f)));
        
        uiEntities.add(context.entities().createEntity(() -> new TextDisplay("ACTION", 100, 200, 1.2f)));
        uiEntities.add(context.entities().createEntity(() -> new TextDisplay("PRIMARY", 350, 200, 1.2f)));
        uiEntities.add(context.entities().createEntity(() -> new TextDisplay("ALTERNATE", 550, 200, 1.2f)));

        addRemapRow(context, "Move Up", MappableActions.UP, 150);
        addRemapRow(context, "Move Down", MappableActions.DOWN, 100);
        addRemapRow(context, "Move Left", MappableActions.LEFT, 50);
        addRemapRow(context, "Move Right", MappableActions.RIGHT, 0);
    }

    private void addRemapRow(SceneContext context, String label, ActionId action, float y) {
        uiEntities.add(context.entities().createEntity(() -> new TextDisplay(label, 100, y, 1.2f)));

        List<Integer> keys = inputManager.getInputMapping().getKeysForAction(action);
        int primaryKey = keys.size() > 0 ? keys.get(0) : -1;
        int altKey = keys.size() > 1 ? keys.get(1) : -1;

        HoverableTextDisplay primaryDisplay = (HoverableTextDisplay) context.entities().createEntity(() -> 
            new HoverableTextDisplay(getKeyString(primaryKey), 350, y, 150, 30, 1.2f));
        uiEntities.add(primaryDisplay);
        remapSlots.add(new RemapSlot(action, primaryKey, primaryDisplay));

        HoverableTextDisplay altDisplay = (HoverableTextDisplay) context.entities().createEntity(() -> 
            new HoverableTextDisplay(getKeyString(altKey), 550, y, 150, 30, 1.2f));
        uiEntities.add(altDisplay);
        remapSlots.add(new RemapSlot(action, altKey, altDisplay));
    }

    private String getKeyString(int keycode) {
        if (keycode == -1) return "[ None ]";
        return "[ " + Input.Keys.toString(keycode) + " ]";
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (isListeningForRemap) {
            for (int i = 0; i < 256; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    if (i == Input.Keys.ESCAPE) {
                        isListeningForRemap = false;
                        activeRemapSlot.display.setText(getKeyString(activeRemapSlot.currentKey));
                        activeRemapSlot = null;
                        return;
                    }
                    InputMapping mapping = inputManager.getInputMapping();
                    if (activeRemapSlot.currentKey != -1) mapping.unbindKey(activeRemapSlot.currentKey);
                    mapping.bindKey(i, activeRemapSlot.action);
                    activeRemapSlot.currentKey = i;
                    activeRemapSlot.display.setText(getKeyString(i));
                    isListeningForRemap = false;
                    activeRemapSlot = null;
                    return;
                }
            }
            return; 
        }

        // Handle Scrolling Input
        if (context.input().isActionActive(MappableActions.UP)) targetScroll -= 400f * deltaTime;
        if (context.input().isActionActive(MappableActions.DOWN)) targetScroll += 400f * deltaTime;
        if (context.input().isActionActive(MappableActions.SCROLL_UP)) targetScroll -= 60f;
        if (context.input().isActionActive(MappableActions.SCROLL_DOWN)) targetScroll += 60f;
        
        targetScroll = Math.max(0f, Math.min(targetScroll, maxScroll));
        float prev = scrollOffset;
        scrollOffset += (targetScroll - scrollOffset) * 10f * deltaTime;
        float delta = scrollOffset - prev;

        // Apply Scroll Transformation visually
        if (Math.abs(delta) > 0.01f) {
            for (Entity entity : uiEntities) {
                if (entity instanceof IRenderItem) {
                    ITransform t = ((IRenderItem) entity).getTransform();
                    t.setPosition(1, t.getPosition(1) + delta);
                }
            }
        }

        // Update Mouse Pointer Position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); 
        mousePointer.updatePosition(mouseX, mouseY);

        // Manually trigger visual hover state via the ICollidable interface 
        for (RemapSlot slot : remapSlots) {
            if (mousePointer.getBounds().overlaps(slot.display.getBounds())) {
                slot.display.onCollision(mousePointer); 
            }
        }

        // MousePointer collision
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (mousePointer.getBounds().overlaps(volSlider.getBounds())) {
                float posX = volSlider.getTransform().getPosition(0);
                volSlider.setValue((mouseX - posX) / volSlider.getTransform().getSize(0));
                Settings.setMusicVolume(volSlider.getValue()); 
            }
            if (mousePointer.getBounds().overlaps(brightSlider.getBounds())) {
                float posX = brightSlider.getTransform().getPosition(0);
                brightSlider.setValue((mouseX - posX) / brightSlider.getTransform().getSize(0));
                Settings.BRIGHTNESS_LEVEL = brightSlider.getValue();
            }
        }
        
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (RemapSlot slot : remapSlots) {
                if (mousePointer.getBounds().overlaps(slot.display.getBounds())) {
                    isListeningForRemap = true;
                    activeRemapSlot = slot;
                    slot.display.setText("[ Press Key ]");
                }
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            context.changeScene("hello_world");
        }
    }

    @Override
    public void submitRenderable(SceneContext context) {
        if (background != null) context.renderQueue().queue(background);
        if (brightnessOverlay != null && brightnessOverlay.isActive()) context.renderQueue().queue(brightnessOverlay); //brightness overlay is below UI so that user can still and edit the settings
        for (Entity entity : uiEntities) {
            if (entity.isActive() && entity instanceof IRenderItem) context.renderQueue().queue((IRenderItem) entity);
        }
    }

    @Override
    public void onExit(SceneContext context) {
        context.entities().removeEntity(mousePointer.getId());
        context.entities().removeEntity(brightnessOverlay.getId());
        for (Entity entity : uiEntities) context.entities().removeEntity(entity.getId());
        uiEntities.clear();
        remapSlots.clear();
    }
}
package com.p1_7.game.scenes.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputManager;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.entities.BackgroundImage;
import com.p1_7.game.entities.BrightnessOverlay;
import com.p1_7.game.entities.BrightnessSlider;
import com.p1_7.game.entities.MenuButton;
import com.p1_7.game.entities.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.managers.IAudioManager;

/**
 * Settings scene for Math Quest Maze.
 */
public class SettingScene extends Scene {

    private static final String BG_ASSET = "menu/background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";
    private static final String TTF_ASSET = "menu/Kenney_Future.ttf";

    private float centreX;
    private float centreY;

    private BitmapFont headingFont;
    private BitmapFont labelFont;
    private BitmapFont tableFont;
    private BitmapFont buttonFont;

    private ICursorSource cursorSource;
    private IInputQuery inputQuery;
    private IInputManager inputManager;
    private InputProcessor previousInputProcessor;
    private final InputProcessor remapInputProcessor = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (!isListeningForRemap()) {
                return false;
            }
            if (keycode == Input.Keys.ESCAPE) {
                cancelActiveRemap();
            } else {
                applyActiveRemap(keycode);
            }
            return true;
        }
    };

    private IAudioManager audio;

    private BackgroundImage background;
    private Text heading;
    private Text volumeLabel;
    private VolumeSlider volumeSlider;
    private Text brightnessLabel;
    private BrightnessSlider brightnessSlider;
    private Text controlsHeading;
    private Text remapHint;
    private Text actionHeader;
    private Text primaryHeader;
    private Text alternateHeader;
    private MenuButton backButton;
    private BrightnessOverlay brightnessOverlay;
    private final List<RemapSlot> remapSlots = new ArrayList<>();
    private RemapSlot activeRemapSlot;
    private RemapSlot.BindingColumn activeRemapColumn;

    public SettingScene() {
        this.name = "settings";
    }

    @Override
    public void onEnter(SceneContext context) {
        computeSceneCenter();
        resolveSceneServices(context);
        createFonts();
        createSceneComponents();
        syncRemapBindings();
    }

    @Override
    public void onExit(SceneContext context) {
        stopListening();
        clearRemapState();
        disposeSceneComponents();
        disposeFonts();
        clearResolvedServices();
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (isListeningForRemap()) {
            return;
        }

        if (handleSceneExit(context)) {
            return;
        }

        if (cursorSource == null) {
            return;
        }

        updateSliderInputs();
        updateRemapInput();
        updateBackButtonInput();
        applySliderChanges();
        handleBackButtonClick(context);
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        queuePrimaryRenderables(renderQueue);
        queueRemapRenderables(renderQueue);
        renderQueue.queue(backButton);
        renderQueue.queue(brightnessOverlay);
    }

    private void computeSceneCenter() {
        centreX = Settings.getWindowWidth() / 2f;
        centreY = Settings.getWindowHeight() / 2f;
    }

    private void resolveSceneServices(SceneContext context) {
        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        if (inputRegistry.hasExtension(ICursorSource.class)) {
            cursorSource = inputRegistry.getExtension(ICursorSource.class);
        }

        inputManager = context.get(IInputManager.class);
        inputQuery = context.get(IInputQuery.class);
        audio = context.get(IAudioManager.class);
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(TTF_ASSET));
        try {
            headingFont = createHeadingFont(generator);
            labelFont = createLabelFont(generator);
            tableFont = createTableFont(generator);
            buttonFont = createButtonFont(generator);
        } finally {
            generator.dispose();
        }
    }

    private BitmapFont createHeadingFont(FreeTypeFontGenerator generator) {
        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = 52;
        params.color = new Color(1f, 0.92f, 0.55f, 1f);
        params.shadowOffsetX = 2;
        params.shadowOffsetY = -2;
        params.shadowColor = new Color(0f, 0f, 0f, 0.5f);
        return generator.generateFont(params);
    }

    private BitmapFont createLabelFont(FreeTypeFontGenerator generator) {
        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = 28;
        params.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        return generator.generateFont(params);
    }

    private BitmapFont createTableFont(FreeTypeFontGenerator generator) {
        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = 22;
        params.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        return generator.generateFont(params);
    }

    private BitmapFont createButtonFont(FreeTypeFontGenerator generator) {
        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = 26;
        params.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        return generator.generateFont(params);
    }

    private void createSceneComponents() {
        float screenHeight = Settings.getWindowHeight();
        float backButtonY = screenHeight * 0.085f;
        float hintY = backButtonY + 54f;
        float rowSpacing = 36f;
        float firstRowY = hintY + 176f;
        float tableHeaderY = firstRowY + 46f;
        float controlsHeadingY = tableHeaderY + 45f;
        float brightnessSliderY = controlsHeadingY + 58f;
        float brightnessLabelY = brightnessSliderY + 48f;
        float volumeSliderY = brightnessLabelY + 62f;
        float volumeLabelY = volumeSliderY + 50f;
        float headingY = volumeLabelY + 66f;

        background = new BackgroundImage(BG_ASSET);
        heading = createCenteredLabel("SETTINGS", headingY, headingFont);
        volumeLabel = createCenteredLabel(volumeText(), volumeLabelY, labelFont);
        brightnessLabel = createCenteredLabel(brightnessText(), brightnessLabelY, labelFont);
        controlsHeading = createCenteredLabel("CONTROLS", controlsHeadingY, buttonFont);
        remapHint = createCenteredLabel(idleRemapHintText(), hintY, tableFont);
        volumeSlider = new VolumeSlider(centreX, volumeSliderY, 340f, audio.getMusicVolume());
        brightnessSlider = new BrightnessSlider(centreX, brightnessSliderY, 340f, Settings.getBrightnessLevel());
        backButton = MenuButton.withTexture("BACK", centreX, backButtonY, buttonFont, BTN_ASSET, HOVER_ASSET);
        brightnessOverlay = new BrightnessOverlay();

        createRemapHeaders(tableHeaderY);
        buildRemapSlots(firstRowY, rowSpacing);
    }

    private Text createCenteredLabel(String text, float centreYPosition, BitmapFont font) {
        return new Text(text, centreX, centreYPosition, font);
    }

    private void createRemapHeaders(float tableHeaderY) {
        float tableLeft = centreX - RemapSlot.TABLE_WIDTH / 2f;
        actionHeader = new Text("ACTION",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH / 2f,
            tableHeaderY,
            tableFont);
        primaryHeader = new Text("PRIMARY",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH + RemapSlot.CELL_GAP + RemapSlot.KEY_COLUMN_WIDTH / 2f,
            tableHeaderY,
            tableFont);
        alternateHeader = new Text("ALTERNATE",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH + RemapSlot.CELL_GAP * 2f
                + RemapSlot.KEY_COLUMN_WIDTH * 1.5f,
            tableHeaderY,
            tableFont);
    }

    private void clearRemapState() {
        remapSlots.clear();
        activeRemapSlot = null;
        activeRemapColumn = null;
    }

    private void disposeSceneComponents() {
        if (volumeSlider != null) {
            volumeSlider.dispose();
        }
        if (brightnessSlider != null) {
            brightnessSlider.dispose();
        }
        if (backButton != null) {
            backButton.dispose();
        }
        if (brightnessOverlay != null) {
            brightnessOverlay.dispose();
        }
        background = null;
        heading = null;
        volumeLabel = null;
        volumeSlider = null;
        brightnessLabel = null;
        brightnessSlider = null;
        controlsHeading = null;
        remapHint = null;
        actionHeader = null;
        primaryHeader = null;
        alternateHeader = null;
        backButton = null;
        brightnessOverlay = null;
    }

    private void disposeFonts() {
        if (headingFont != null) {
            headingFont.dispose();
        }
        if (labelFont != null) {
            labelFont.dispose();
        }
        if (tableFont != null) {
            tableFont.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }

        headingFont = null;
        labelFont = null;
        tableFont = null;
        buttonFont = null;
    }

    private void clearResolvedServices() {
        audio = null;
        cursorSource = null;
        inputQuery = null;
        inputManager = null;
        previousInputProcessor = null;
    }

    private boolean handleSceneExit(SceneContext context) {
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            context.changeScene("menu");
            return true;
        }
        return false;
    }

    private void updateSliderInputs() {
        volumeSlider.updateInput(cursorSource, inputQuery);
        brightnessSlider.updateInput(cursorSource, inputQuery);
    }

    private void updateBackButtonInput() {
        backButton.updateInput(cursorSource, inputQuery);
    }

    private void applySliderChanges() {
        if (volumeSlider.hasMoved()) {
            audio.setMusicVolume(volumeSlider.getValue());
            volumeLabel.setText(volumeText());
            volumeSlider.resetMoved();
        }
        if (brightnessSlider.hasMoved()) {
            brightnessLabel.setText(brightnessText());
            brightnessSlider.resetMoved();
        }
    }

    private void handleBackButtonClick(SceneContext context) {
        if (backButton.isClicked()) {
            backButton.resetClick();
            context.changeScene("menu");
        }
    }

    private void queuePrimaryRenderables(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(heading);
        renderQueue.queue(volumeLabel);
        renderQueue.queue(volumeSlider);
        renderQueue.queue(brightnessLabel);
        renderQueue.queue(brightnessSlider);
        renderQueue.queue(controlsHeading);
    }

    private void queueRemapRenderables(IRenderQueue renderQueue) {
        renderQueue.queue(actionHeader);
        renderQueue.queue(primaryHeader);
        renderQueue.queue(alternateHeader);
        for (int i = 0; i < remapSlots.size(); i++) {
            renderQueue.queue(remapSlots.get(i));
        }
        renderQueue.queue(remapHint);
    }

    private String volumeText() {
        return "Music Volume:  " + Math.round(audio.getMusicVolume() * 100) + "%";
    }

    private String brightnessText() {
        return "Brightness:  " + Math.round(Settings.getBrightnessLevel() * 100) + "%";
    }

    private String idleRemapHintText() {
        return "Click a binding to remap it";
    }

    private void buildRemapSlots(float firstRowY, float rowSpacing) {
        remapSlots.clear();
        List<GameActions.BindingSpec> bindings = GameActions.getMovementBindings();
        float rowY = firstRowY;
        for (int i = 0; i < bindings.size(); i++) {
            GameActions.BindingSpec binding = bindings.get(i);
            remapSlots.add(createRemapSlot(binding, rowY));
            rowY -= rowSpacing;
        }
    }

    private RemapSlot createRemapSlot(GameActions.BindingSpec binding, float rowCentreY) {
        List<Integer> keys = new ArrayList<>(inputManager.getKeysForAction(binding.getActionId()));
        Collections.sort(keys);

        boolean hasPrimaryDefault = keys.remove(Integer.valueOf(binding.getPrimaryKeyCode()));
        boolean hasAlternateDefault = keys.remove(Integer.valueOf(binding.getAlternateKeyCode()));

        int primaryKeyCode = hasPrimaryDefault
            ? binding.getPrimaryKeyCode()
            : takeFirstDistinctKey(keys, binding.getAlternateKeyCode(), binding.getPrimaryKeyCode());

        int alternateKeyCode = hasAlternateDefault
            ? binding.getAlternateKeyCode()
            : takeFirstDistinctKey(keys, primaryKeyCode, binding.getAlternateKeyCode());

        return new RemapSlot(
            binding.getLabel(),
            binding.getActionId(),
            primaryKeyCode,
            alternateKeyCode,
            centreX,
            rowCentreY,
            tableFont
        );
    }

    private int takeFirstDistinctKey(List<Integer> keys, int disallowedKeyCode, int fallbackKeyCode) {
        for (int i = 0; i < keys.size(); i++) {
            int keyCode = keys.get(i);
            if (keyCode != disallowedKeyCode) {
                keys.remove(i);
                return keyCode;
            }
        }
        return fallbackKeyCode;
    }

    private void updateRemapInput() {
        float mx = cursorSource.getCursorX();
        float my = cursorSource.getCursorY();
        boolean clickStarted =
            inputQuery.getActionState(GameActions.POINTER_PRIMARY) == InputState.PRESSED;

        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            RemapSlot.BindingColumn hitColumn = slot.hitTest(mx, my);
            slot.setHoveredColumn(hitColumn);
            slot.setActiveColumn(activeRemapSlot == slot ? activeRemapColumn : null);
            if (clickStarted && hitColumn != null) {
                startListening(slot, hitColumn);
                return;
            }
        }
    }

    private void startListening(RemapSlot slot, RemapSlot.BindingColumn column) {
        activeRemapSlot = slot;
        activeRemapColumn = column;
        remapHint.setText("Press a key for " + slot.getLabel() + " (" + column.getLabel() + ")");
        previousInputProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(remapInputProcessor);
        refreshRemapVisualState();
    }

    private boolean isListeningForRemap() {
        return activeRemapSlot != null && activeRemapColumn != null;
    }

    private void cancelActiveRemap() {
        stopListening();
    }

    private void applyActiveRemap(int keyCode) {
        if (!isListeningForRemap()) {
            return;
        }

        String reservedUiKeyMessage = getReservedUiKeyMessage(keyCode);
        if (reservedUiKeyMessage != null) {
            remapHint.setText(reservedUiKeyMessage);
            refreshRemapVisualState();
            return;
        }

        int previousKeyCode = activeRemapSlot.getKeyCode(activeRemapColumn);
        int siblingKeyCode = activeRemapSlot.getOtherKeyCode(activeRemapColumn);

        if (keyCode == previousKeyCode || keyCode == siblingKeyCode) {
            stopListening();
            return;
        }

        RemapSlot ownerSlot = null;
        RemapSlot.BindingColumn ownerColumn = null;
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            RemapSlot.BindingColumn column = slot.findColumnForKey(keyCode);
            if (column != null) {
                ownerSlot = slot;
                ownerColumn = column;
                break;
            }
        }

        if (ownerSlot != null && ownerColumn != null) {
            ownerSlot.setKeyCode(ownerColumn, previousKeyCode);
        }
        activeRemapSlot.setKeyCode(activeRemapColumn, keyCode);

        syncRemapBindings();
        stopListening();
    }

    private String getReservedUiKeyMessage(int keyCode) {
        if (keyCode == Input.Keys.SPACE) {
            return Input.Keys.toString(keyCode) + " is reserved for menu confirm";
        }
        if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACKSPACE) {
            return Input.Keys.toString(keyCode) + " is reserved for menu back";
        }
        return null;
    }

    private void syncRemapBindings() {
        for (int i = 0; i < remapSlots.size(); i++) {
            inputManager.unbindAction(remapSlots.get(i).getActionId());
        }
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            inputManager.bindKey(slot.getPrimaryKeyCode(), slot.getActionId());
            inputManager.bindKey(slot.getAlternateKeyCode(), slot.getActionId());
        }
    }

    private void stopListening() {
        if (Gdx.input.getInputProcessor() == remapInputProcessor) {
            Gdx.input.setInputProcessor(previousInputProcessor);
        }
        previousInputProcessor = null;
        activeRemapSlot = null;
        activeRemapColumn = null;
        if (remapHint != null) {
            remapHint.setText(idleRemapHintText());
        }
        refreshRemapVisualState();
    }

    private void refreshRemapVisualState() {
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            slot.setHoveredColumn(null);
            slot.setActiveColumn(activeRemapSlot == slot ? activeRemapColumn : null);
        }
    }
}

package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputManager;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.entities.BrightnessOverlay;
import com.p1_7.game.entities.BrightnessSlider;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.managers.IAudioManager;
import com.p1_7.game.entities.MenuButton;
import com.p1_7.game.entities.VolumeSlider;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Settings scene for Math Quest Maze.
 *
 * Uses Kenney_Future.ttf via gdx-freetype for all text,
 * background.png for the background, and button*.png for buttons.
 *
 * Navigation:
 *   Mouse           - click buttons
 *   ESC / Backspace - back to menu
 */
public class SettingScene extends Scene {

    // ── asset paths ──────────────────────────────────────────────
    private static final String BG_ASSET    = "menu/background.png";
    private static final String BTN_ASSET   = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";
    private static final String TTF_ASSET   = "menu/Kenney_Future.ttf";

    // ── layout ───────────────────────────────────────────────────
    // computed in onEnter so they reflect the resolution at scene entry time
    private float centreX;
    private float centreY;

    // ── fonts ─────────────────────────────────────────────────────
    private BitmapFont headingFont;
    private BitmapFont labelFont;
    private BitmapFont tableFont;
    private BitmapFont buttonFont;

    // ── input ────────────────────────────────────────────────────
    private ICursorSource cursorSource;
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

    /** audio manager resolved once on scene entry */
    private IAudioManager      audio;

    private SettingsBackground background;
    private LabelText          heading;
    private LabelText          volumeLabel;
    private VolumeSlider       volumeSlider;
    private LabelText          brightnessLabel;
    private BrightnessSlider   brightnessSlider;
    private LabelText          controlsHeading;
    private LabelText          remapHint;
    private LabelText          actionHeader;
    private LabelText          primaryHeader;
    private LabelText          alternateHeader;
    private MenuButton         backButton;
    private BrightnessOverlay  brightnessOverlay;
    private final List<RemapSlot> remapSlots = new ArrayList<>();
    private RemapSlot          activeRemapSlot;
    private BindingColumn      activeRemapColumn;

    public SettingScene() {
        this.name = "settings";
    }

    @Override
    public void onEnter(SceneContext context) {
        // compute layout from the current resolution so changes via setResolution take effect
        centreX = Settings.getWindowWidth()  / 2f;
        centreY = Settings.getWindowHeight() / 2f;

        // ── generate all fonts from the same TTF ──────────────────
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal(TTF_ASSET));

        FreeTypeFontParameter headingParams = new FreeTypeFontParameter();
        headingParams.size  = 52;
        headingParams.color = new Color(1f, 0.92f, 0.55f, 1f); // gold
        headingParams.shadowOffsetX = 2;
        headingParams.shadowOffsetY = -2;
        headingParams.shadowColor   = new Color(0f, 0f, 0f, 0.5f);
        headingFont = generator.generateFont(headingParams);

        FreeTypeFontParameter labelParams = new FreeTypeFontParameter();
        labelParams.size  = 28;
        labelParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        labelFont = generator.generateFont(labelParams);

        FreeTypeFontParameter tableParams = new FreeTypeFontParameter();
        tableParams.size  = 22;
        tableParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        tableFont = generator.generateFont(tableParams);

        FreeTypeFontParameter buttonParams = new FreeTypeFontParameter();
        buttonParams.size  = 26;
        buttonParams.color = new Color(0.10f, 0.16f, 0.24f, 1f);
        buttonFont = generator.generateFont(buttonParams);

        generator.dispose(); // safe to dispose after generating all fonts

        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        if (inputRegistry.hasExtension(ICursorSource.class)) {
            cursorSource = inputRegistry.getExtension(ICursorSource.class);
        }
        // cursorSource stays null if not registered; update() guard handles it cleanly

        inputManager = context.get(IInputManager.class);
        audio = context.get(IAudioManager.class);

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

        // ── entities ─────────────────────────────────────────────
        background  = new SettingsBackground(BG_ASSET);
        heading     = new LabelText("SETTINGS",   centreX, headingY,
                                    headingFont);
        volumeLabel = new LabelText(volumeText(audio), centreX, volumeLabelY,
                                    labelFont);
        brightnessLabel = new LabelText(brightnessText(), centreX, brightnessLabelY,
                                        labelFont);
        controlsHeading = new LabelText("CONTROLS", centreX, controlsHeadingY, buttonFont);
        remapHint = new LabelText(idleRemapHintText(), centreX, hintY, tableFont);

        volumeSlider = new VolumeSlider(centreX, volumeSliderY, 340f, audio.getMusicVolume());
        brightnessSlider = new BrightnessSlider(centreX, brightnessSliderY, 340f,
                                                Settings.getBrightnessLevel());
        backButton   = MenuButton.withTexture("BACK", centreX, backButtonY, buttonFont, BTN_ASSET, HOVER_ASSET);
        brightnessOverlay = new BrightnessOverlay();

        float tableLeft = centreX - RemapSlot.TABLE_WIDTH / 2f;
        actionHeader = new LabelText("ACTION",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH / 2f,
            tableHeaderY,
            tableFont);
        primaryHeader = new LabelText("PRIMARY",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH + RemapSlot.CELL_GAP + RemapSlot.KEY_COLUMN_WIDTH / 2f,
            tableHeaderY,
            tableFont);
        alternateHeader = new LabelText("ALTERNATE",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH + RemapSlot.CELL_GAP * 2f + RemapSlot.KEY_COLUMN_WIDTH * 1.5f,
            tableHeaderY,
            tableFont);

        buildRemapSlots(firstRowY, rowSpacing);
        syncRemapBindings();
    }

    @Override
    public void onExit(SceneContext context) {
        stopListening();
        remapSlots.clear();
        if (volumeSlider != null) volumeSlider.dispose();
        if (brightnessSlider != null) brightnessSlider.dispose();
        if (backButton   != null) backButton.dispose();
        if (brightnessOverlay != null) brightnessOverlay.dispose();
        if (background       != null) background.dispose();
        if (headingFont      != null) headingFont.dispose();
        if (labelFont        != null) labelFont.dispose();
        if (tableFont        != null) tableFont.dispose();
        if (buttonFont       != null) buttonFont.dispose();
        audio = null;
        cursorSource = null;
        inputManager = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (isListeningForRemap()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            context.changeScene("menu");
            return;
        }

        if (cursorSource == null) return;
        volumeSlider.updateInput(cursorSource);
        brightnessSlider.updateInput(cursorSource);
        updateRemapInput();
        backButton.updateInput(cursorSource);

        if (volumeSlider.hasMoved()) {
            audio.setMusicVolume(volumeSlider.getValue());
            volumeLabel.setText(volumeText(audio));
            volumeSlider.resetMoved();
        }
        if (brightnessSlider.hasMoved()) {
            brightnessLabel.setText(brightnessText());
            brightnessSlider.resetMoved();
        }
        if (backButton.isClicked()) {
            backButton.resetClick();
            context.changeScene("menu");
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(heading);
        renderQueue.queue(volumeLabel);
        renderQueue.queue(volumeSlider);
        renderQueue.queue(brightnessLabel);
        renderQueue.queue(brightnessSlider);
        renderQueue.queue(controlsHeading);
        renderQueue.queue(actionHeader);
        renderQueue.queue(primaryHeader);
        renderQueue.queue(alternateHeader);
        for (int i = 0; i < remapSlots.size(); i++) {
            renderQueue.queue(remapSlots.get(i));
        }
        renderQueue.queue(remapHint);
        renderQueue.queue(backButton);
        renderQueue.queue(brightnessOverlay);
    }

    private String volumeText(IAudioManager audio) {
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

    private RemapSlot createRemapSlot(GameActions.BindingSpec binding, float centreY) {
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

        return new RemapSlot(binding.getLabel(), binding.getActionId(), primaryKeyCode, alternateKeyCode,
            centreX, centreY, tableFont);
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
        boolean clickStarted = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            BindingColumn hitColumn = slot.hitTest(mx, my);
            slot.setHoveredColumn(hitColumn);
            slot.setActiveColumn(activeRemapSlot == slot ? activeRemapColumn : null);
            if (clickStarted && hitColumn != null) {
                startListening(slot, hitColumn);
                return;
            }
        }
    }

    private void startListening(RemapSlot slot, BindingColumn column) {
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

        int previousKeyCode = activeRemapSlot.getKeyCode(activeRemapColumn);
        int siblingKeyCode = activeRemapSlot.getOtherKeyCode(activeRemapColumn);

        if (keyCode == previousKeyCode || keyCode == siblingKeyCode) {
            stopListening();
            return;
        }

        RemapSlot ownerSlot = null;
        BindingColumn ownerColumn = null;
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            BindingColumn column = slot.findColumnForKey(keyCode);
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
        remapHint.setText(idleRemapHintText());
        refreshRemapVisualState();
    }

    private void refreshRemapVisualState() {
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            slot.setHoveredColumn(null);
            slot.setActiveColumn(activeRemapSlot == slot ? activeRemapColumn : null);
        }
    }

    // ── inner entities ────────────────────────────────────────────

    private enum BindingColumn {
        PRIMARY("Primary"),
        ALTERNATE("Alternate");

        private final String label;

        BindingColumn(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private static class RemapSlot extends Entity implements IRenderable {
        private static final float TABLE_WIDTH = 560f;
        private static final float ACTION_COLUMN_WIDTH = 190f;
        private static final float KEY_COLUMN_WIDTH = 170f;
        private static final float ROW_HEIGHT = 34f;
        private static final float CELL_HEIGHT = 30f;
        private static final float CELL_GAP = 15f;

        private static final Color CELL_FILL = new Color(0.54f, 0.58f, 0.86f, 0.94f);
        private static final Color CELL_HOVER = new Color(0.66f, 0.71f, 0.95f, 0.98f);
        private static final Color CELL_ACTIVE = new Color(0.93f, 0.77f, 0.36f, 1f);
        private static final Color CELL_BORDER = new Color(0.90f, 0.94f, 1.0f, 1f);
        private static final Color TEXT_COLOR = new Color(0.12f, 0.16f, 0.28f, 1f);

        private final String      label;
        private final com.p1_7.abstractengine.input.ActionId actionId;
        private final BitmapFont  font;
        private final float       actionColumnLeft;
        private final float       actionColumnWidth;
        private final float       cellBaselineY;
        private final float       primaryCellX;
        private final float       alternateCellX;
        private final float       cellY;

        private int primaryKeyCode;
        private int alternateKeyCode;
        private BindingColumn hoveredColumn;
        private BindingColumn activeColumn;

        RemapSlot(String label,
                  com.p1_7.abstractengine.input.ActionId actionId,
                  int primaryKeyCode,
                  int alternateKeyCode,
                  float centreX,
                  float centreY,
                  BitmapFont font) {
            float tableLeft = centreX - TABLE_WIDTH / 2f;
            this.label = label;
            this.actionId = actionId;
            this.primaryKeyCode = primaryKeyCode;
            this.alternateKeyCode = alternateKeyCode;
            this.font = font;
            this.actionColumnLeft = tableLeft;
            this.actionColumnWidth = ACTION_COLUMN_WIDTH;
            this.primaryCellX = tableLeft + ACTION_COLUMN_WIDTH + CELL_GAP;
            this.alternateCellX = primaryCellX + KEY_COLUMN_WIDTH + CELL_GAP;
            this.cellY = centreY - CELL_HEIGHT / 2f;
            this.cellBaselineY = centreY + 8f;
        }

        public String getLabel() {
            return label;
        }

        public com.p1_7.abstractengine.input.ActionId getActionId() {
            return actionId;
        }

        public int getPrimaryKeyCode() {
            return primaryKeyCode;
        }

        public int getAlternateKeyCode() {
            return alternateKeyCode;
        }

        public BindingColumn hitTest(float x, float y) {
            if (contains(primaryCellX, y, x)) {
                return BindingColumn.PRIMARY;
            }
            if (contains(alternateCellX, y, x)) {
                return BindingColumn.ALTERNATE;
            }
            return null;
        }

        private boolean contains(float cellX, float y, float x) {
            return x >= cellX && x <= cellX + KEY_COLUMN_WIDTH
                && y >= cellY && y <= cellY + CELL_HEIGHT;
        }

        public void setHoveredColumn(BindingColumn column) {
            this.hoveredColumn = column;
        }

        public void setActiveColumn(BindingColumn column) {
            this.activeColumn = column;
        }

        public BindingColumn findColumnForKey(int keyCode) {
            if (primaryKeyCode == keyCode) {
                return BindingColumn.PRIMARY;
            }
            if (alternateKeyCode == keyCode) {
                return BindingColumn.ALTERNATE;
            }
            return null;
        }

        public int getKeyCode(BindingColumn column) {
            return column == BindingColumn.PRIMARY ? primaryKeyCode : alternateKeyCode;
        }

        public int getOtherKeyCode(BindingColumn column) {
            return column == BindingColumn.PRIMARY ? alternateKeyCode : primaryKeyCode;
        }

        public void setKeyCode(BindingColumn column, int keyCode) {
            if (column == BindingColumn.PRIMARY) {
                primaryKeyCode = keyCode;
            } else {
                alternateKeyCode = keyCode;
            }
        }

        @Override public String getAssetPath() { return null; }
        @Override public com.p1_7.abstractengine.transform.ITransform getTransform() { return null; }

        @Override
        public void render(IDrawContext ctx) {
            GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
            GlyphLayout actionLayout = new GlyphLayout(font, label);
            gdxCtx.drawFont(font, label,
                actionColumnLeft + (actionColumnWidth - actionLayout.width) / 2f,
                cellBaselineY);

            renderCell(gdxCtx, BindingColumn.PRIMARY, primaryCellX, displayText(BindingColumn.PRIMARY));
            renderCell(gdxCtx, BindingColumn.ALTERNATE, alternateCellX, displayText(BindingColumn.ALTERNATE));
        }

        private void renderCell(GdxDrawContext gdxCtx, BindingColumn column, float cellX, String text) {
            Color fill = CELL_FILL;
            if (activeColumn == column) {
                fill = CELL_ACTIVE;
            } else if (hoveredColumn == column) {
                fill = CELL_HOVER;
            }

            gdxCtx.rect(CELL_BORDER, cellX - 2f, cellY - 2f, KEY_COLUMN_WIDTH + 4f, CELL_HEIGHT + 4f, true);
            gdxCtx.rect(fill, cellX, cellY, KEY_COLUMN_WIDTH, CELL_HEIGHT, true);

            Color originalColor = font.getColor().cpy();
            font.setColor(TEXT_COLOR);
            GlyphLayout keyLayout = new GlyphLayout(font, text);
            gdxCtx.drawFont(font, text,
                cellX + (KEY_COLUMN_WIDTH - keyLayout.width) / 2f,
                cellBaselineY);
            font.setColor(originalColor);
        }

        private String displayText(BindingColumn column) {
            if (activeColumn == column) {
                return "PRESS KEY";
            }
            return formatKey(getKeyCode(column));
        }

        private String formatKey(int keyCode) {
            String keyText = Input.Keys.toString(keyCode);
            if (keyText == null || keyText.trim().isEmpty()) {
                return "UNKNOWN";
            }
            return keyText.toUpperCase();
        }
    }

    private static class SettingsBackground implements IRenderable {
        private final Transform2D transform;
        private final String      assetPath;
        private final Texture     texture;

        SettingsBackground(String assetPath) {
            this.assetPath = assetPath;
            this.texture   = new Texture(Gdx.files.internal(assetPath));
            this.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            this.transform = new Transform2D(0, 0, Settings.getWindowWidth(), Settings.getWindowHeight());
        }

        @Override public String     getAssetPath() { return assetPath; }
        @Override public ITransform getTransform() { return transform; }

        @Override
        public void render(IDrawContext ctx) {
            GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
            gdxCtx.drawTexture(assetPath,
                transform.getPosition(0), transform.getPosition(1),
                transform.getSize(0),     transform.getSize(1));
        }

        void dispose() { if (texture != null) texture.dispose(); }
    }

    private static class LabelText extends Entity implements IRenderable {
        private final Transform2D transform;
        private final BitmapFont  font;
        private       String      text;

        LabelText(String text, float cx, float cy, BitmapFont font) {
            this.text      = text;
            this.font      = font;
            this.transform = new Transform2D(cx, cy, 0f, 0f);
        }

        void setText(String t) { this.text = t; }

        @Override public String     getAssetPath() { return null; }
        @Override public ITransform getTransform() { return transform; }

        @Override
        public void render(IDrawContext ctx) {
            GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
            GlyphLayout layout = new GlyphLayout(font, text);
            gdxCtx.drawFont(font, text,
                transform.getPosition(0) - layout.width  / 2f,
                transform.getPosition(1) + layout.height / 2f);
        }
    }
}

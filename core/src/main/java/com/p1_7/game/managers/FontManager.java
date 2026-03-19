package com.p1_7.game.managers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.p1_7.abstractengine.engine.Manager;

/**
 * Engine-managed cache of shared game fonts generated from the menu TTF asset.
 */
public class FontManager extends Manager implements IFontManager {

    private static final String TTF_ASSET = "menu/Kenney_Future.ttf";

    private final Files files = Gdx.files;
    private final Map<String, BitmapFont> fontCache = new HashMap<>();

    private FreeTypeFontGenerator generator;

    @Override
    protected void onInit() {
        generator = new FreeTypeFontGenerator(files.internal(TTF_ASSET));
    }

    @Override
    protected void onShutdown() {
        for (BitmapFont font : fontCache.values()) {
            font.dispose();
        }
        fontCache.clear();

        if (generator != null) {
            generator.dispose();
            generator = null;
        }
    }

    @Override
    public BitmapFont getGoldDisplayFont(int size) {
        return getOrCreate("gold-display:" + size,
            size,
            new Color(1f, 0.92f, 0.55f, 1f),
            2,
            -2,
            new Color(0f, 0f, 0f, 0.5f));
    }

    @Override
    public BitmapFont getDarkTextFont(int size) {
        return getOrCreate("dark-text:" + size,
            size,
            new Color(0.10f, 0.16f, 0.24f, 1f),
            0,
            0,
            null);
    }

    @Override
    public BitmapFont getPromptFont() {
        return getOrCreate("prompt",
            28,
            new Color(0.10f, 0.16f, 0.24f, 1f),
            1,
            -1,
            new Color(1f, 1f, 1f, 0.35f));
    }

    private BitmapFont getOrCreate(String key, int size, Color color,
                                   int shadowOffsetX, int shadowOffsetY, Color shadowColor) {
        BitmapFont cached = fontCache.get(key);
        if (cached != null) {
            return cached;
        }

        ensureInitialised();

        FreeTypeFontParameter params = new FreeTypeFontParameter();
        params.size = size;
        params.color = color;
        params.shadowOffsetX = shadowOffsetX;
        params.shadowOffsetY = shadowOffsetY;
        params.shadowColor = shadowColor;

        BitmapFont font = generator.generateFont(params);
        fontCache.put(key, font);
        return font;
    }

    private void ensureInitialised() {
        if (generator == null) {
            throw new IllegalStateException("FontManager has not been initialised");
        }
    }
}

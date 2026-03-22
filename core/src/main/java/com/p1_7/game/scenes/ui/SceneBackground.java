package com.p1_7.game.scenes.ui;

import com.p1_7.game.entities.BackgroundImage;

/**
 * Scene-specific full-screen background that narrows the usage contract to the
 * scenes layer. Delegates all rendering and transform logic to BackgroundImage.
 */
public class SceneBackground extends BackgroundImage {

    /**
     * constructs a scene background using the given texture asset.
     *
     * @param assetPath path to the background texture, relative to the assets root
     */
    public SceneBackground(String assetPath) {
        super(assetPath);
    }
}

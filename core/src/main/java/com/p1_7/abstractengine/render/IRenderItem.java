package com.p1_7.abstractengine.render;

import com.p1_7.abstractengine.transform.ITransformable;

/**
 * combines renderable and spatial contracts into a single interface
 * that the IRenderQueue and RenderManager operate on.
 *
 * no additional methods are declared; the interface exists to give
 * the render pipeline a single type to work with rather than
 * requiring casts.
 */
public interface IRenderItem extends IRenderable, ITransformable {
}

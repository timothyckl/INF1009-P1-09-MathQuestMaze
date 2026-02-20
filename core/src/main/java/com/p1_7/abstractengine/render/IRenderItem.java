package com.p1_7.abstractengine.render;

import com.p1_7.abstractengine.transform.ITransformable;

/**
 * combines IRenderable and ITransformable into a single type for the render pipeline.
 */
public interface IRenderItem extends IRenderable, ITransformable {
}

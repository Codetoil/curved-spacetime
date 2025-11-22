package io.codetoil.curved_spacetime.render;

import io.codetoil.curved_spacetime.scene.Scene;

public abstract class RenderModuleSceneRenderContext
{
	protected final Scene scene;

	protected RenderModuleSceneRenderContext(Scene scene)
	{
		this.scene = scene;
	}
}

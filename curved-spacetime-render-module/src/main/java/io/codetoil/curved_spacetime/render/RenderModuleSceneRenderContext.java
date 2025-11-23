package io.codetoil.curved_spacetime.render;

import io.codetoil.curved_spacetime.render.render_enviornments.RenderEnvironment;

public abstract class RenderModuleSceneRenderContext
{
	protected final RenderEnvironment renderEnvironment;

	protected RenderModuleSceneRenderContext(RenderEnvironment renderEnvironment)
	{
		this.renderEnvironment = renderEnvironment;
	}
}

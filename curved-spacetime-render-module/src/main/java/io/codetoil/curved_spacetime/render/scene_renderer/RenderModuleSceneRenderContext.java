package io.codetoil.curved_spacetime.render.scene_renderer;

public abstract class RenderModuleSceneRenderContext
{
	protected final RenderModuleSceneRenderer renderModuleSceneRenderer;

	protected RenderModuleSceneRenderContext(RenderModuleSceneRenderer renderModuleSceneRenderer)
	{
		this.renderModuleSceneRenderer = renderModuleSceneRenderer;
	}
}

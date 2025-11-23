package io.codetoil.curved_spacetime.render.scene_renderer;

import java.util.function.Function;

public record RenderModuleSceneRendererCallbackSupplier(
		Function<RenderModuleSceneRenderer, RenderModuleSceneRendererCallback> supplier)
		implements Function<RenderModuleSceneRenderer, RenderModuleSceneRendererCallback>
{
	public RenderModuleSceneRendererCallback apply(RenderModuleSceneRenderer args)
	{
		return supplier.apply(args);
	}
}

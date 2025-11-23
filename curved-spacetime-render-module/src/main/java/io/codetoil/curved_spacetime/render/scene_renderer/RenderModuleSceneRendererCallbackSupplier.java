package io.codetoil.curved_spacetime.render.scene_renderer;

import java.util.function.Function;

public record RenderModuleSceneRendererCallbackSupplier(
		Function<RenderModuleSceneRenderer, RenderModuleSceneRenderCallback> supplier)
		implements Function<RenderModuleSceneRenderer, RenderModuleSceneRenderCallback>
{
	public RenderModuleSceneRenderCallback apply(RenderModuleSceneRenderer args)
	{
		return supplier.apply(args);
	}
}

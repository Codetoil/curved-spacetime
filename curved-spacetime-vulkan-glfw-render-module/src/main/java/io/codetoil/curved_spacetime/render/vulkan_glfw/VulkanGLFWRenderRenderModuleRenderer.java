package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.engine.Engine;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleRenderer;

import java.util.Set;
import java.util.stream.Collectors;

public class VulkanGLFWRenderRenderModuleRenderer extends VulkanRenderModuleRenderer
{
	protected final VulkanGLFWRenderModuleEntrypoint entrypoint;
	protected Set<VulkanGLFWRenderRenderModuleSceneRenderContext> sceneRenderContexts;

	public VulkanGLFWRenderRenderModuleRenderer(VulkanGLFWRenderModuleEntrypoint entrypoint)
	{
		super(VulkanGLFWRenderModuleRenderContext::new);
		this.entrypoint = entrypoint;
	}

	@Override
	public void init()
	{
		sceneRenderContexts = Engine.getInstance().getScenes().stream()
				.map((scene) -> new VulkanGLFWRenderRenderModuleSceneRenderContext(this.entrypoint, scene))
				.collect(Collectors.toSet());
		sceneRenderContexts.forEach((sceneRenderContext) ->
				sceneRenderContext.init(this));
	}

}

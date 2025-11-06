package io.codetoil.curved_spacetime.api.render.vulkan_glfw;

import io.codetoil.curved_spacetime.api.render.vulkan.VulkanRenderModuleRenderer;
import io.codetoil.curved_spacetime.api.render.vulkan.VulkanRenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.api.scene.Scene;

public class VulkanGLFWRenderModuleSceneRenderer extends VulkanRenderModuleSceneRenderer
{
	public VulkanGLFWRenderModuleSceneRenderer(
			VulkanGLFWRenderRenderModuleSceneRenderContext sceneRenderContext,
			VulkanRenderModuleRenderer renderer,
			Scene scene)
	{
		super(sceneRenderContext, renderer, scene);
	}
}

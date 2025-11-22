package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleRenderer;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.scene.Scene;

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

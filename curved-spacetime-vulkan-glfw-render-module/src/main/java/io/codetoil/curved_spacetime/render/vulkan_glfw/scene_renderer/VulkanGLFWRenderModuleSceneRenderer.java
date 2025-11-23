package io.codetoil.curved_spacetime.render.vulkan_glfw.scene_renderer;

import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleRenderModuleSceneSceneRenderer;

public class VulkanGLFWRenderModuleSceneRenderer
		extends VulkanRenderModuleRenderModuleSceneSceneRenderer
{
	public VulkanGLFWRenderModuleSceneRenderer(
			VulkanGLFWRenderModuleSceneRenderContext sceneRenderContext,
			RenderModuleSceneRenderer renderModuleSceneRenderer)
	{
		super(sceneRenderContext, renderModuleSceneRenderer);
		sceneRenderContext.init();
	}
}

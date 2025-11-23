package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.render.render_enviornments.RenderEnvironment;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderEnviornmentCallback;

public class VulkanGLFWRenderEnviornmentCallback extends VulkanRenderEnviornmentCallback
{
	public VulkanGLFWRenderEnviornmentCallback(
			VulkanGLFWRenderModuleSceneRenderContext sceneRenderContext,
			RenderEnvironment renderEnvironment)
	{
		super(sceneRenderContext, renderEnvironment);
	}
}

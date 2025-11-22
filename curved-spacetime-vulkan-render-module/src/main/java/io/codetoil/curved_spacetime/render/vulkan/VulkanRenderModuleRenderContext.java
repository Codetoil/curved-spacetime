package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.render.RenderModuleRenderContext;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanContext;

public abstract class VulkanRenderModuleRenderContext extends RenderModuleRenderContext
{
	protected VulkanModuleVulkanContext context;
	protected VulkanRenderModuleGraphicsModuleQueue vulkanRenderModuleGraphicsQueue = null;
	protected VulkanRenderModuleRenderer renderer;

	public void init(VulkanRenderModuleRenderer renderer,
					 VulkanModuleVulkanContext context)
	{
		this.renderer = renderer;
		this.context = context;
		this.vulkanRenderModuleGraphicsQueue = new VulkanRenderModuleGraphicsModuleQueue(this, 0);
	}

	public void clean()
	{
		this.vulkanRenderModuleGraphicsQueue.waitIdle();
	}

	public VulkanModuleVulkanContext getContext()
	{
		return context;
	}

	public VulkanRenderModuleGraphicsModuleQueue getGraphicsQueue()
	{
		return vulkanRenderModuleGraphicsQueue;
	}
}

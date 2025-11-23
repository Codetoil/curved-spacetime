package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.render.RenderModuleWindow;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderContext;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanContext;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public abstract class VulkanRenderModuleSceneRenderContext extends RenderModuleSceneRenderContext
{
	protected RenderModuleWindow window;
	protected VulkanRenderModuleSurface vulkanRenderModuleSurface = null;
	protected VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain = null;
	protected VulkanModuleVulkanContext context;
	protected VulkanRenderModuleGraphicsModuleQueue vulkanRenderModuleGraphicsQueue = null;

	protected VulkanRenderModuleSceneRenderContext(RenderModuleSceneRenderer renderModuleSceneRenderer)
	{
		super(renderModuleSceneRenderer);
	}

	protected void init(VulkanModuleVulkanContext context,
						Supplier<RenderModuleWindow> windowSupplier,
						Supplier<VulkanRenderModuleSurface> surfaceSupplier,
						IntSupplier requestImagesSupplier,
						BooleanSupplier vSyncSupplier)
	{
		this.context = context;
		this.vulkanRenderModuleGraphicsQueue = new VulkanRenderModuleGraphicsModuleQueue(this, 0);
		this.window = windowSupplier.get();
		this.vulkanRenderModuleSurface = surfaceSupplier.get();
		this.vulkanRenderModuleSwapChain =
				new VulkanRenderModuleSwapChain(this.getContext().getLogicalDevice(),
						this.getSurface(),
						this.getWindow(), requestImagesSupplier.getAsInt(), vSyncSupplier.getAsBoolean());
	}

	public VulkanModuleVulkanContext getContext()
	{
		return context;
	}

	public VulkanRenderModuleSurface getSurface()
	{
		return vulkanRenderModuleSurface;
	}

	public RenderModuleWindow getWindow()
	{
		return window;
	}

	protected void loop()
	{
		this.window.loop();
	}

	protected void clean()
	{
		this.vulkanRenderModuleGraphicsQueue.waitIdle();
		this.vulkanRenderModuleSwapChain.cleanup();
		this.vulkanRenderModuleSurface.cleanup();
		this.window.setShouldClose();
		this.window.clean();
	}

	public VulkanRenderModuleSwapChain getSwapChain()
	{
		return vulkanRenderModuleSwapChain;
	}

	public VulkanRenderModuleGraphicsModuleQueue getGraphicsQueue()
	{
		return vulkanRenderModuleGraphicsQueue;
	}
}

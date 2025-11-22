package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.Window;
import io.codetoil.curved_spacetime.render.RenderModuleSceneRenderContext;
import io.codetoil.curved_spacetime.scene.Scene;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public abstract class VulkanRenderModuleSceneRenderContext extends RenderModuleSceneRenderContext
{
	protected VulkanRenderModuleRenderContext context;
	protected Window window;
	protected VulkanRenderModuleSurface vulkanRenderModuleSurface = null;
	protected VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain = null;

	protected VulkanRenderModuleSceneRenderContext(Scene scene)
	{
		super(scene);
	}

	protected void init(VulkanRenderModuleRenderContext context,
						Supplier<Window> windowSupplier,
						Supplier<VulkanRenderModuleSurface> surfaceSupplier,
						IntSupplier requestImagesSupplier,
						BooleanSupplier vSyncSupplier)
	{
		this.context = context;
		this.window = windowSupplier.get();
		this.vulkanRenderModuleSurface = surfaceSupplier.get();
		this.vulkanRenderModuleSwapChain =
				new VulkanRenderModuleSwapChain(this.getRenderContext().getContext().getLogicalDevice(),
						this.getSurface(),
						this.getWindow(), requestImagesSupplier.getAsInt(), vSyncSupplier.getAsBoolean());
	}

	public VulkanRenderModuleRenderContext getRenderContext()
	{
		return context;
	}

	public VulkanRenderModuleSurface getSurface()
	{
		return vulkanRenderModuleSurface;
	}

	public Window getWindow()
	{
		return window;
	}

	protected void loop()
	{
		this.window.loop();
	}

	protected void clean()
	{
		this.vulkanRenderModuleSwapChain.cleanup();
		this.vulkanRenderModuleSurface.cleanup();
		this.window.setShouldClose();
		this.window.clean();
	}

	public VulkanRenderModuleSwapChain getSwapChain()
	{
		return vulkanRenderModuleSwapChain;
	}
}

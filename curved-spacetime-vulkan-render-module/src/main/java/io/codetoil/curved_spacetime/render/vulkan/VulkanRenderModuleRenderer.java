package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.render.RenderModuleRenderer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandBuffer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleFence;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleSemaphore;

import java.util.Arrays;
import java.util.function.Supplier;

public abstract class VulkanRenderModuleRenderer extends RenderModuleRenderer
{
	public static final int MAX_IN_FLIGHT = 2;
	protected final VulkanRenderModuleRenderContext vulkanRenderModuleRenderContext;
	protected VulkanModuleCommandBuffer[] commandBuffers;
	protected VulkanModuleCommandPool[] commandPools;
	protected VulkanModuleFence[] fences;
	protected VulkanRenderModuleGraphicsModuleQueue graphicsQueue;
	protected VulkanModuleSemaphore[] presentationCompleteSemaphores;
	protected int currentFrame;

	public VulkanRenderModuleRenderer(Supplier<VulkanRenderModuleRenderContext> vulkanRenderContextSupplier)
	{
		this.vulkanRenderModuleRenderContext = vulkanRenderContextSupplier.get();
	}

	public void init()
	{
		currentFrame = 0;
		graphicsQueue = new VulkanRenderModuleGraphicsModuleQueue(this.vulkanRenderModuleRenderContext, 0);
		commandPools = new VulkanModuleCommandPool[MAX_IN_FLIGHT];
		commandBuffers = new VulkanModuleCommandBuffer[MAX_IN_FLIGHT];
		fences = new VulkanModuleFence[MAX_IN_FLIGHT];
		presentationCompleteSemaphores = new VulkanModuleSemaphore[MAX_IN_FLIGHT];
		for (int i = 0; i < MAX_IN_FLIGHT; i++)
		{
			commandPools[i] = new VulkanModuleCommandPool(vulkanRenderModuleRenderContext.getContext(),
					graphicsQueue.getQueueFamilyIndex(), false);
			commandBuffers[i] = new VulkanModuleCommandBuffer(commandPools[i], true, true);
			presentationCompleteSemaphores[i] = new VulkanModuleSemaphore(vulkanRenderModuleRenderContext.getContext());
			fences[i] = new VulkanModuleFence(vulkanRenderModuleRenderContext.getContext(), true);
		}
	}

	public void loop()
	{
	}

	public void clean()
	{
		vulkanRenderModuleRenderContext.context.getLogicalDevice().waitIdle();

		Arrays.asList(presentationCompleteSemaphores).forEach(VulkanModuleSemaphore::clean);
		Arrays.asList(fences).forEach(VulkanModuleFence::clean);
		Arrays.asList(commandBuffers).forEach((buffer) -> {
			VulkanModuleCommandPool pool = buffer.getCommandPool();
			buffer.cleanup();
			pool.cleanup();
		});
		vulkanRenderModuleRenderContext.clean();
		super.clean();
	}

	protected void recordingStart(VulkanModuleCommandBuffer commandBuffer)
	{
		commandBuffer.getCommandPool().reset();
		commandBuffer.beginRecording();
	}

	protected void recordingEnd(VulkanModuleCommandBuffer commandBuffer)
	{
		commandBuffer.endRecording();
	}

	protected void waitForFence(int currentFrame)
	{
		VulkanModuleFence fence = fences[currentFrame];
		fence.fenceWait();
	}
}

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.render.RenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleGraphicsModuleQueue.VulkanRenderModuleGraphicsPresentModuleQueue;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandBuffer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleFence;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleSemaphore;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;

import java.util.Arrays;

public abstract class VulkanRenderModuleSceneRenderer extends RenderModuleSceneRenderer
{
	protected final VulkanRenderModuleSceneRenderContext sceneRenderContext;
	protected final VulkanRenderModuleRenderer renderer;
	protected final VulkanRenderModuleGraphicsPresentModuleQueue presentQueue;
	protected final VulkanModuleSemaphore[] renderCompleteSemaphores;

	public VulkanRenderModuleSceneRenderer(VulkanRenderModuleSceneRenderContext sceneRenderContext,
										   VulkanRenderModuleRenderer renderer, Scene scene)
	{
		super(scene);
		this.sceneRenderContext = sceneRenderContext;
		this.renderer = renderer;
		presentQueue = new VulkanRenderModuleGraphicsPresentModuleQueue(this.getSceneRenderContext(), 0);
		renderCompleteSemaphores = new VulkanModuleSemaphore[this.getSceneRenderContext().getSwapChain()
				.getVulkanImageViews().length];
		for (int i = 0; i < renderCompleteSemaphores.length; i++)
		{
			renderCompleteSemaphores[i] = new VulkanModuleSemaphore(this.getSceneRenderContext().getRenderContext()
					.getContext());
		}
	}

	public VulkanRenderModuleSceneRenderContext getSceneRenderContext()
	{
		return sceneRenderContext;
	}

	@Override
	public void init()
	{

	}

	@Override
	public void loop()
	{
		VulkanRenderModuleSwapChain swapChain = this.getSceneRenderContext().getSwapChain();
		this.getRenderer().waitForFence(this.getRenderer().currentFrame);
		VulkanModuleCommandBuffer commandBuffer = this.getRenderer().commandBuffers[this.getRenderer().currentFrame];
		this.getRenderer().recordingStart(commandBuffer);
		int imageIndex = swapChain.acquireNextImage(this.getSceneRenderContext().getRenderContext().getContext()
				.getLogicalDevice(), this.getRenderer().presentationCompleteSemaphores[this.getRenderer()
				.currentFrame]);
		if (imageIndex < 0)
		{
			this.getRenderer().recordingEnd(commandBuffer);
			return;
		}

		// Main Body

		this.getRenderer().recordingEnd(commandBuffer);
		submit(commandBuffer, this.getRenderer().currentFrame, imageIndex);
		swapChain.presentImage(presentQueue, renderCompleteSemaphores[imageIndex], imageIndex);
		this.getRenderer().currentFrame =
				(this.getRenderer().currentFrame + 1) % VulkanRenderModuleRenderer.MAX_IN_FLIGHT;
	}

	@Override
	public void clean()
	{
		Arrays.asList(renderCompleteSemaphores).forEach(VulkanModuleSemaphore::clean);

	}

	public VulkanRenderModuleRenderer getRenderer()
	{
		return renderer;
	}

	protected void submit(VulkanModuleCommandBuffer commandBuffer, int currentFrame, int imageIndex)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanModuleFence fence = this.getRenderer().fences[currentFrame];
			fence.reset();
			VkCommandBufferSubmitInfo.Buffer commands =
					VkCommandBufferSubmitInfo.calloc(1, stack)
							.sType$Default()
							.commandBuffer(commandBuffer.getVkCommandBuffer());
			VkSemaphoreSubmitInfo.Buffer waitSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
					.sType$Default()
					.stageMask(VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
					.semaphore(this.getRenderer().presentationCompleteSemaphores[currentFrame].getVkSemaphore());
			VkSemaphoreSubmitInfo.Buffer signalSemaphore = VkSemaphoreSubmitInfo.calloc(1, stack)
					.sType$Default()
					.stageMask(VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)
					.semaphore(renderCompleteSemaphores[imageIndex].getVkSemaphore());
			this.getRenderer().graphicsQueue.submit(commands, waitSemaphores, signalSemaphore, fence);
		}
	}
}

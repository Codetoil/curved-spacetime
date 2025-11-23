package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRendererCallback;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleGraphicsModuleQueue.VulkanRenderModuleGraphicsPresentModuleQueue;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandBuffer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleFence;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleSemaphore;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;

import java.util.Arrays;

public abstract class VulkanRenderModuleRendererModuleSceneRenderer implements RenderModuleSceneRendererCallback
{
	public static final int MAX_IN_FLIGHT = 2;
	protected final VulkanRenderModuleSceneRenderContext sceneRenderContext;
	protected final VulkanRenderModuleGraphicsPresentModuleQueue presentQueue;
	protected final VulkanModuleSemaphore[] renderCompleteSemaphores;
	protected final RenderModuleSceneRenderer renderEnviornment;
	protected VulkanModuleCommandBuffer[] commandBuffers;
	protected VulkanModuleCommandPool[] commandPools;
	protected VulkanModuleFence[] fences;
	protected VulkanRenderModuleGraphicsModuleQueue graphicsQueue;
	protected VulkanModuleSemaphore[] presentationCompleteSemaphores;
	protected int currentFrame;

	public VulkanRenderModuleRendererModuleSceneRenderer(VulkanRenderModuleSceneRenderContext sceneRenderContext,
														 RenderModuleSceneRenderer renderModuleSceneRenderer)
	{
		this.renderEnviornment = renderModuleSceneRenderer;
		this.sceneRenderContext = sceneRenderContext;
		presentQueue = new VulkanRenderModuleGraphicsPresentModuleQueue(this.getSceneRenderContext(), 0);
		renderCompleteSemaphores = new VulkanModuleSemaphore[this.getSceneRenderContext().getSwapChain()
				.getVulkanImageViews().length];
		for (int i = 0; i < renderCompleteSemaphores.length; i++)
		{
			renderCompleteSemaphores[i] = new VulkanModuleSemaphore(this.getSceneRenderContext().getContext());
		}
	}

	public VulkanRenderModuleSceneRenderContext getSceneRenderContext()
	{
		return sceneRenderContext;
	}

	public void init()
	{
		currentFrame = 0;
		graphicsQueue = new VulkanRenderModuleGraphicsModuleQueue(this.getSceneRenderContext(), 0);
		commandPools = new VulkanModuleCommandPool[MAX_IN_FLIGHT];
		commandBuffers = new VulkanModuleCommandBuffer[MAX_IN_FLIGHT];
		fences = new VulkanModuleFence[MAX_IN_FLIGHT];
		presentationCompleteSemaphores = new VulkanModuleSemaphore[MAX_IN_FLIGHT];
		for (int i = 0; i < MAX_IN_FLIGHT; i++)
		{
			commandPools[i] = new VulkanModuleCommandPool(getSceneRenderContext().getContext(),
					graphicsQueue.getQueueFamilyIndex(), false);
			commandBuffers[i] = new VulkanModuleCommandBuffer(commandPools[i], true, true);
			presentationCompleteSemaphores[i] = new VulkanModuleSemaphore(getSceneRenderContext().getContext());
			fences[i] = new VulkanModuleFence(getSceneRenderContext().getContext(), true);
		}
	}

	@Override
	public void loop()
	{
		VulkanRenderModuleSwapChain swapChain = this.getSceneRenderContext().getSwapChain();
		this.waitForFence(this.currentFrame);
		VulkanModuleCommandBuffer commandBuffer = this.commandBuffers[this.currentFrame];
		this.recordingStart(commandBuffer);
		int imageIndex = swapChain.acquireNextImage(this.getSceneRenderContext().getContext()
				.getLogicalDevice(), this.presentationCompleteSemaphores[this.currentFrame]);
		if (imageIndex < 0)
		{
			this.recordingEnd(commandBuffer);
			return;
		}

		// Main Body

		this.recordingEnd(commandBuffer);
		submit(commandBuffer, this.currentFrame, imageIndex);
		swapChain.presentImage(presentQueue, renderCompleteSemaphores[imageIndex], imageIndex);
		this.currentFrame =
				(this.currentFrame + 1) % VulkanRenderModuleRendererModuleSceneRenderer.MAX_IN_FLIGHT;
	}

	public void clean()
	{
		sceneRenderContext.getContext().getLogicalDevice().waitIdle();

		Arrays.asList(presentationCompleteSemaphores).forEach(VulkanModuleSemaphore::clean);
		Arrays.asList(fences).forEach(VulkanModuleFence::clean);
		Arrays.asList(commandBuffers).forEach((buffer) -> {
			VulkanModuleCommandPool pool = buffer.getCommandPool();
			buffer.cleanup();
			pool.cleanup();
		});
		sceneRenderContext.clean();
		Arrays.asList(renderCompleteSemaphores).forEach(VulkanModuleSemaphore::clean);
	}

	@Override
	public RenderModuleSceneRenderer renderEnvironment()
	{
		return this.renderEnviornment;
	}

	protected void waitForFence(int currentFrame)
	{
		VulkanModuleFence fence = fences[currentFrame];
		fence.fenceWait();
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

	protected void submit(VulkanModuleCommandBuffer commandBuffer, int currentFrame, int imageIndex)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanModuleFence fence = this.fences[currentFrame];
			fence.reset();
			VkCommandBufferSubmitInfo.Buffer commands =
					VkCommandBufferSubmitInfo.calloc(1, stack)
							.sType$Default()
							.commandBuffer(commandBuffer.getVkCommandBuffer());
			VkSemaphoreSubmitInfo.Buffer waitSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
					.sType$Default()
					.stageMask(VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
					.semaphore(this.presentationCompleteSemaphores[currentFrame].getVkSemaphore());
			VkSemaphoreSubmitInfo.Buffer signalSemaphore = VkSemaphoreSubmitInfo.calloc(1, stack)
					.sType$Default()
					.stageMask(VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)
					.semaphore(renderCompleteSemaphores[imageIndex].getVkSemaphore());
			this.graphicsQueue.submit(commands, waitSemaphores, signalSemaphore, fence);
		}
	}
}

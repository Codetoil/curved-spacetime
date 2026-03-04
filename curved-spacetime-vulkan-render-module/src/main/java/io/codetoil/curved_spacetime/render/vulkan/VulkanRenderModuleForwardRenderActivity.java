/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br> it under the terms of the GNU General
 * Public License as published by <br> the Free Software Foundation, either version 3 of the License, or <br> (at your
 * option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br> but WITHOUT ANY WARRANTY; without even the
 * implied warranty of<br> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br> GNU General Public License
 * for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br> along with this program.  If not, see <a
 * href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandBuffer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleFence;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import vulkan.*;

import java.lang.foreign.MemorySegment;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public class VulkanRenderModuleForwardRenderActivity
{
	private final VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain;
	private final VulkanRenderModuleSwapChainRenderPass vulkanRenderModuleSwapChainRenderPass;
	private final VulkanRenderModuleFrameBuffer[] vulkanRenderModuleFrameBuffers;
	private final VulkanModuleCommandBuffer[] vulkanModuleCommandBuffers;
	private final VulkanModuleFence[] vulkanModuleFences;
	private final Logger logger;

	public VulkanRenderModuleForwardRenderActivity(VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain,
												   VulkanModuleCommandPool vulkanModuleCommandPool,
												   Logger logger)
	{
		this.logger = logger;
		this.vulkanRenderModuleSwapChain = vulkanRenderModuleSwapChain;
		VulkanModuleLogicalDevice vulkanModuleLogicalDevice =
				this.vulkanRenderModuleSwapChain.getVulkanLogicalDevice();
		MemorySegment vulkanSwapChainExtent = this.vulkanRenderModuleSwapChain.getVulkanSwapChainExtent();
		VulkanRenderModuleImageView[] vulkanRenderModuleImageViews =
				this.vulkanRenderModuleSwapChain.getVulkanImageViews();
		int numImages = vulkanRenderModuleImageViews.length;

		this.vulkanRenderModuleSwapChainRenderPass =
				new VulkanRenderModuleSwapChainRenderPass(vulkanRenderModuleSwapChain);

		MemorySegment pAttachments;
		this.vulkanRenderModuleFrameBuffers = new VulkanRenderModuleFrameBuffer[numImages];
		for (int i = 0; i < numImages; i++)
		{
			pAttachments = vulkanRenderModuleImageViews[i].getVkImageView();
			this.vulkanRenderModuleFrameBuffers[i] =
					new VulkanRenderModuleFrameBuffer(vulkanModuleLogicalDevice,
							VkExtent2D.width(vulkanSwapChainExtent), VkExtent2D.height(vulkanSwapChainExtent),
							pAttachments, this.vulkanRenderModuleSwapChainRenderPass.getVkRenderPass());
		}

		this.vulkanModuleCommandBuffers = new VulkanModuleCommandBuffer[numImages];
		this.vulkanModuleFences = new VulkanModuleFence[numImages];
		for (int i = 0; i < numImages; i++)
		{
			this.vulkanModuleCommandBuffers[i] =
					new VulkanModuleCommandBuffer(vulkanModuleCommandPool, true, false, this.logger);
			this.vulkanModuleFences[i] = new VulkanModuleFence(vulkanModuleLogicalDevice, true);
			recordVulkanCommandBuffer(this.vulkanModuleCommandBuffers[i], this.vulkanRenderModuleFrameBuffers[i],
					VkExtent2D.width(vulkanSwapChainExtent), VkExtent2D.height(vulkanSwapChainExtent));
		}
	}

	private void recordVulkanCommandBuffer(VulkanModuleCommandBuffer vulkanModuleCommandBuffer,
										   VulkanRenderModuleFrameBuffer vulkanRenderModuleFrameBuffer,
										   int width, int height)
	{
		MemorySegment clearValues = VkClearValue.allocateArray(1, arena);
		MemorySegment firstClearValue = clearValues.asSlice(0, VkClearValue.sizeof());
		MemorySegment color = VkClearColorValue.allocate(arena);
		VkClearColorValue.float32(color, 0, 0.5f);
		VkClearColorValue.float32(color, 1, 0.7f);
		VkClearColorValue.float32(color, 2, 1.0f);
		VkClearColorValue.float32(color, 3, 0.0f);
		VkClearValue.color(firstClearValue, color);
		MemorySegment renderPassBeginInfo = VkRenderPassBeginInfo.allocate(arena);
		VkRenderPassBeginInfo.sType(renderPassBeginInfo, Vulkan.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO());
		VkRenderPassBeginInfo.pClearValues(renderPassBeginInfo, clearValues);
		MemorySegment renderArea = VkExtent2D.allocate(arena);
		VkExtent2D.width(renderArea, width);
		VkExtent2D.height(renderArea, height);
		VkRenderPassBeginInfo.renderArea(renderPassBeginInfo, renderArea);
		VkRenderPassBeginInfo.framebuffer(renderPassBeginInfo, vulkanRenderModuleFrameBuffer.getVkFrameBuffer());

		vulkanModuleCommandBuffer.beginRecording();
		Vulkan.vkCmdBeginRenderPass(vulkanModuleCommandBuffer.getVkCommandBuffer(), renderPassBeginInfo,
				Vulkan.VK_SUBPASS_CONTENTS_INLINE());
		Vulkan.vkCmdEndRenderPass(vulkanModuleCommandBuffer.getVkCommandBuffer());
		vulkanModuleCommandBuffer.endRecording();
	}

	public void cleanup()
	{
		Arrays.asList(this.vulkanRenderModuleFrameBuffers).forEach(VulkanRenderModuleFrameBuffer::cleanup);
		this.vulkanRenderModuleSwapChainRenderPass.cleanup();
		Arrays.asList(this.vulkanModuleCommandBuffers).forEach(VulkanModuleCommandBuffer::cleanup);
		Arrays.asList(this.vulkanModuleFences).forEach(VulkanModuleFence::cleanup);
	}

	public void waitForVulkanFence()
	{
		int idx = this.vulkanRenderModuleSwapChain.getCurrentFrame();
		VulkanModuleFence currentVulkanModuleFence = this.vulkanModuleFences[idx];
		currentVulkanModuleFence.vulkanFenceWait();
	}

	public void submit(VulkanRenderModuleGraphicsQueue vulkanGraphicsQueue)
	{
		int idx = this.vulkanRenderModuleSwapChain.getCurrentFrame();
		VulkanModuleCommandBuffer vulkanModuleCommandBuffer = this.vulkanModuleCommandBuffers[idx];
		VulkanModuleFence currentVulkanModuleFence = this.vulkanModuleFences[idx];
		currentVulkanModuleFence.reset();
		//VulkanSwapChain.SynchronizationVulkanSemaphores synchronizationVulkanSemaphores =
		//		this.vulkanSwapChain.getSyncVulkanSemaphoreList()[idx];
		//vulkanGraphicsQueue.submit(stack.pointers(vulkanCommandBuffer.getVkCommandBuffer()),
		//		stack.longs(synchronizationVulkanSemaphores.imageAcquisitionVulkanSemaphore().getVkSemaphore()),
		//		stack.ints(VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
		//		stack.longs(synchronizationVulkanSemaphores.renderCompleteVulkanSemaphore().getVkSemaphore()),
		//		currentVulkanFence);
	}
}

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

package io.codetoil.curved_spacetime.render.vulkan.ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleCommandBuffer;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleFence;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleLogicalDevice;
import vulkan.*;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.logging.Logger;

public class FFMVulkanRenderModuleForwardRenderActivity
{
	private final FFMVulkanRenderModuleSwapChain ffmVulkanRenderModuleSwapChain;
	private final FFMVulkanRenderModuleSwapChainRenderPass ffmVulkanRenderModuleSwapChainRenderPass;
	private final FFMVulkanRenderModuleFrameBuffer[] ffmVulkanRenderModuleFrameBuffers;
	private final FFMVulkanModuleCommandBuffer[] ffmVulkanModuleCommandBuffers;
	private final FFMVulkanModuleFence[] ffmVulkanModuleFences;
	private final Logger logger;

	public FFMVulkanRenderModuleForwardRenderActivity(FFMVulkanRenderModuleSwapChain ffmVulkanRenderModuleSwapChain,
	                                                  FFMVulkanModuleCommandPool ffmVulkanModuleCommandPool,
	                                                  Logger logger)
	{
		this.logger = logger;
		this.ffmVulkanRenderModuleSwapChain = ffmVulkanRenderModuleSwapChain;
		FFMVulkanModuleLogicalDevice ffmVulkanModuleLogicalDevice =
				this.ffmVulkanRenderModuleSwapChain.getVulkanLogicalDevice();
		MemorySegment vulkanSwapChainExtent = this.ffmVulkanRenderModuleSwapChain.getVulkanSwapChainExtent();
		FFMVulkanRenderModuleImageView[] ffmVulkanRenderModuleImageViews =
				this.ffmVulkanRenderModuleSwapChain.getVulkanImageViews();
		int numImages = ffmVulkanRenderModuleImageViews.length;

		this.ffmVulkanRenderModuleSwapChainRenderPass =
				new FFMVulkanRenderModuleSwapChainRenderPass(ffmVulkanRenderModuleSwapChain);

		MemorySegment pAttachments;
		this.ffmVulkanRenderModuleFrameBuffers = new FFMVulkanRenderModuleFrameBuffer[numImages];
		for (int i = 0; i < numImages; i++)
		{
			pAttachments = ffmVulkanRenderModuleImageViews[i].getVkImageView();
			this.ffmVulkanRenderModuleFrameBuffers[i] =
					new FFMVulkanRenderModuleFrameBuffer(ffmVulkanModuleLogicalDevice,
							VkExtent2D.width(vulkanSwapChainExtent), VkExtent2D.height(vulkanSwapChainExtent),
							pAttachments, this.ffmVulkanRenderModuleSwapChainRenderPass.getVkRenderPass());
		}

		this.ffmVulkanModuleCommandBuffers = new FFMVulkanModuleCommandBuffer[numImages];
		this.ffmVulkanModuleFences = new FFMVulkanModuleFence[numImages];
		for (int i = 0; i < numImages; i++)
		{
			this.ffmVulkanModuleCommandBuffers[i] =
					new FFMVulkanModuleCommandBuffer(ffmVulkanModuleCommandPool, true, false, this.logger);
			this.ffmVulkanModuleFences[i] = new FFMVulkanModuleFence(ffmVulkanModuleLogicalDevice, true);
			recordVulkanCommandBuffer(this.ffmVulkanModuleCommandBuffers[i], this.ffmVulkanRenderModuleFrameBuffers[i],
					VkExtent2D.width(vulkanSwapChainExtent), VkExtent2D.height(vulkanSwapChainExtent));
		}
	}

	private void recordVulkanCommandBuffer(FFMVulkanModuleCommandBuffer ffmVulkanModuleCommandBuffer,
	                                       FFMVulkanRenderModuleFrameBuffer ffmVulkanRenderModuleFrameBuffer,
	                                       int width, int height)
	{
		MemorySegment clearValues = VkClearValue.allocateArray(1, MainModuleEngine.getInstance().nativeAllocator);
		MemorySegment firstClearValue = clearValues.asSlice(0, VkClearValue.sizeof());
		MemorySegment color = VkClearColorValue.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkClearColorValue.float32(color, 0, 0.5f);
		VkClearColorValue.float32(color, 1, 0.7f);
		VkClearColorValue.float32(color, 2, 1.0f);
		VkClearColorValue.float32(color, 3, 0.0f);
		VkClearValue.color(firstClearValue, color);
		MemorySegment renderPassBeginInfo =
				VkRenderPassBeginInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkRenderPassBeginInfo.sType(renderPassBeginInfo, Vulkan.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO());
		VkRenderPassBeginInfo.pClearValues(renderPassBeginInfo, clearValues);
		MemorySegment renderArea = VkExtent2D.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkExtent2D.width(renderArea, width);
		VkExtent2D.height(renderArea, height);
		VkRenderPassBeginInfo.renderArea(renderPassBeginInfo, renderArea);
		VkRenderPassBeginInfo.framebuffer(renderPassBeginInfo, ffmVulkanRenderModuleFrameBuffer.getVkFrameBuffer());

		ffmVulkanModuleCommandBuffer.beginRecording();
		Vulkan.vkCmdBeginRenderPass(ffmVulkanModuleCommandBuffer.getVkCommandBuffer(), renderPassBeginInfo,
				Vulkan.VK_SUBPASS_CONTENTS_INLINE());
		Vulkan.vkCmdEndRenderPass(ffmVulkanModuleCommandBuffer.getVkCommandBuffer());
		ffmVulkanModuleCommandBuffer.endRecording();
	}

	public void cleanup()
	{
		Arrays.asList(this.ffmVulkanRenderModuleFrameBuffers).forEach(FFMVulkanRenderModuleFrameBuffer::cleanup);
		this.ffmVulkanRenderModuleSwapChainRenderPass.cleanup();
		Arrays.asList(this.ffmVulkanModuleCommandBuffers).forEach(FFMVulkanModuleCommandBuffer::cleanup);
		Arrays.asList(this.ffmVulkanModuleFences).forEach(FFMVulkanModuleFence::cleanup);
	}

	public void waitForVulkanFence()
	{
		int idx = this.ffmVulkanRenderModuleSwapChain.getCurrentFrame();
		FFMVulkanModuleFence currentFFMVulkanModuleFence = this.ffmVulkanModuleFences[idx];
		currentFFMVulkanModuleFence.vulkanFenceWait();
	}

	public void submit(FFMVulkanRenderModuleGraphicsQueue vulkanGraphicsQueue)
	{
		int idx = this.ffmVulkanRenderModuleSwapChain.getCurrentFrame();
		FFMVulkanModuleCommandBuffer ffmVulkanModuleCommandBuffer = this.ffmVulkanModuleCommandBuffers[idx];
		FFMVulkanModuleFence currentFFMVulkanModuleFence = this.ffmVulkanModuleFences[idx];
		currentFFMVulkanModuleFence.reset();
		//VulkanSwapChain.SynchronizationVulkanSemaphores synchronizationVulkanSemaphores =
		//		this.vulkanSwapChain.getSyncVulkanSemaphoreList()[idx];
		//vulkanGraphicsQueue.submit(stack.pointers(vulkanCommandBuffer.getVkCommandBuffer()),
		//		stack.longs(synchronizationVulkanSemaphores.imageAcquisitionVulkanSemaphore().getVkSemaphore()),
		//		stack.ints(VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
		//		stack.longs(synchronizationVulkanSemaphores.renderCompleteVulkanSemaphore().getVkSemaphore()),
		//		currentVulkanFence);
	}
}

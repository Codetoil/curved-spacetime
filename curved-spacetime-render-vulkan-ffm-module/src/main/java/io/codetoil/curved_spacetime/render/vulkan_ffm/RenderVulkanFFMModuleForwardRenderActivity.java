/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2025 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.render.vulkan_ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleCommandBuffer;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleFence;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleLogicalDevice;
import vulkan.*;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.logging.Logger;

public class RenderVulkanFFMModuleForwardRenderActivity
{
	private final RenderVulkanFFMModuleSwapChain renderVulkanFFMModuleSwapChain;
	private final RenderVulkanFFMModuleSwapChainRenderPass renderVulkanFFMModuleSwapChainRenderPass;
	private final RenderVulkanFFMModuleFrameBuffer[] renderVulkanFFMModuleFrameBuffers;
	private final VulkanFFMModuleCommandBuffer[] vulkanFFMModuleCommandBuffers;
	private final VulkanFFMModuleFence[] vulkanFFMModuleFences;
	private final Logger logger;

	public RenderVulkanFFMModuleForwardRenderActivity(RenderVulkanFFMModuleSwapChain renderVulkanFFMModuleSwapChain,
													  VulkanFFMModuleCommandPool vulkanFFMModuleCommandPool,
													  Logger logger)
	{
		this.logger = logger;
		this.renderVulkanFFMModuleSwapChain = renderVulkanFFMModuleSwapChain;
		VulkanFFMModuleLogicalDevice vulkanFFMModuleLogicalDevice =
				this.renderVulkanFFMModuleSwapChain.getVulkanLogicalDevice();
		MemorySegment vulkanSwapChainExtent = this.renderVulkanFFMModuleSwapChain.getVulkanSwapChainExtent();
		RenderVulkanFFMModuleImageView[] renderVulkanFFMModuleImageViews =
				this.renderVulkanFFMModuleSwapChain.getVulkanImageViews();
		int numImages = renderVulkanFFMModuleImageViews.length;

		this.renderVulkanFFMModuleSwapChainRenderPass =
				new RenderVulkanFFMModuleSwapChainRenderPass(renderVulkanFFMModuleSwapChain);

		MemorySegment pAttachments;
		this.renderVulkanFFMModuleFrameBuffers = new RenderVulkanFFMModuleFrameBuffer[numImages];
		for (int i = 0; i < numImages; i++)
		{
			pAttachments = renderVulkanFFMModuleImageViews[i].getVkImageView();
			this.renderVulkanFFMModuleFrameBuffers[i] =
					new RenderVulkanFFMModuleFrameBuffer(vulkanFFMModuleLogicalDevice,
							VkExtent2D.width(vulkanSwapChainExtent), VkExtent2D.height(vulkanSwapChainExtent),
							pAttachments, this.renderVulkanFFMModuleSwapChainRenderPass.getVkRenderPass());
		}

		this.vulkanFFMModuleCommandBuffers = new VulkanFFMModuleCommandBuffer[numImages];
		this.vulkanFFMModuleFences = new VulkanFFMModuleFence[numImages];
		for (int i = 0; i < numImages; i++)
		{
			this.vulkanFFMModuleCommandBuffers[i] =
					new VulkanFFMModuleCommandBuffer(vulkanFFMModuleCommandPool, true, false, this.logger);
			this.vulkanFFMModuleFences[i] = new VulkanFFMModuleFence(vulkanFFMModuleLogicalDevice, true);
			recordVulkanCommandBuffer(this.vulkanFFMModuleCommandBuffers[i], this.renderVulkanFFMModuleFrameBuffers[i],
					VkExtent2D.width(vulkanSwapChainExtent), VkExtent2D.height(vulkanSwapChainExtent));
		}
	}

	private void recordVulkanCommandBuffer(VulkanFFMModuleCommandBuffer vulkanFFMModuleCommandBuffer,
										   RenderVulkanFFMModuleFrameBuffer renderVulkanFFMModuleFrameBuffer,
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
		VkRenderPassBeginInfo.framebuffer(renderPassBeginInfo, renderVulkanFFMModuleFrameBuffer.getVkFrameBuffer());

		vulkanFFMModuleCommandBuffer.beginRecording();
		Vulkan.vkCmdBeginRenderPass(vulkanFFMModuleCommandBuffer.getVkCommandBuffer(), renderPassBeginInfo,
				Vulkan.VK_SUBPASS_CONTENTS_INLINE());
		Vulkan.vkCmdEndRenderPass(vulkanFFMModuleCommandBuffer.getVkCommandBuffer());
		vulkanFFMModuleCommandBuffer.endRecording();
	}

	public void cleanup()
	{
		Arrays.asList(this.renderVulkanFFMModuleFrameBuffers).forEach(RenderVulkanFFMModuleFrameBuffer::cleanup);
		this.renderVulkanFFMModuleSwapChainRenderPass.cleanup();
		Arrays.asList(this.vulkanFFMModuleCommandBuffers).forEach(VulkanFFMModuleCommandBuffer::cleanup);
		Arrays.asList(this.vulkanFFMModuleFences).forEach(VulkanFFMModuleFence::cleanup);
	}

	public void waitForVulkanFence()
	{
		int idx = this.renderVulkanFFMModuleSwapChain.getCurrentFrame();
		VulkanFFMModuleFence currentFFMVulkanModuleFence = this.vulkanFFMModuleFences[idx];
		currentFFMVulkanModuleFence.vulkanFenceWait();
	}

	public void submit(RenderVulkanFFMModuleGraphicsQueue vulkanGraphicsQueue)
	{
		int idx = this.renderVulkanFFMModuleSwapChain.getCurrentFrame();
		VulkanFFMModuleCommandBuffer vulkanFFMModuleCommandBuffer = this.vulkanFFMModuleCommandBuffers[idx];
		VulkanFFMModuleFence currentFFMVulkanModuleFence = this.vulkanFFMModuleFences[idx];
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

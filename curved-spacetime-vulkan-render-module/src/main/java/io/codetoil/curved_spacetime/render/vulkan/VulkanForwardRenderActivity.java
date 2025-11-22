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

import io.codetoil.curved_spacetime.vulkan.VulkanCommandBuffer;
import io.codetoil.curved_spacetime.vulkan.VulkanCommandPool;
import io.codetoil.curved_spacetime.vulkan.VulkanFence;
import io.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import java.nio.LongBuffer;
import java.util.Arrays;

public class VulkanForwardRenderActivity
{
	private final VulkanSwapChain vulkanSwapChain;
	private final VulkanSwapChainRenderPass vulkanSwapChainRenderPass;
	private final VulkanFrameBuffer[] vulkanFrameBuffers;
	private final VulkanCommandBuffer[] vulkanCommandBuffers;
	private final VulkanFence[] vulkanFences;

	public VulkanForwardRenderActivity(VulkanSwapChain vulkanSwapChain, VulkanCommandPool vulkanCommandPool)
	{
		this.vulkanSwapChain = vulkanSwapChain;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanLogicalDevice vulkanLogicalDevice = this.vulkanSwapChain.getVulkanLogicalDevice();
			VkExtent2D vulkanSwapChainExtent = this.vulkanSwapChain.getVulkanSwapChainExtent();
			VulkanImageView[] vulkanImageViews = this.vulkanSwapChain.getVulkanImageViews();
			int numImages = vulkanImageViews.length;

			this.vulkanSwapChainRenderPass = new VulkanSwapChainRenderPass(vulkanSwapChain);

			LongBuffer pAttachments = stack.mallocLong(1);
			this.vulkanFrameBuffers = new VulkanFrameBuffer[numImages];
			for (int i = 0; i < numImages; i++)
			{
				pAttachments.put(0, vulkanImageViews[i].getVkImageView());
				this.vulkanFrameBuffers[i] = new VulkanFrameBuffer(vulkanLogicalDevice, vulkanSwapChainExtent.width(),
						vulkanSwapChainExtent.height(), pAttachments, this.vulkanSwapChainRenderPass.getVkRenderPass());
			}

			this.vulkanCommandBuffers = new VulkanCommandBuffer[numImages];
			this.vulkanFences = new VulkanFence[numImages];
			for (int i = 0; i < numImages; i++)
			{
				this.vulkanCommandBuffers[i] = new VulkanCommandBuffer(vulkanCommandPool, true, false);
				this.vulkanFences[i] = new VulkanFence(vulkanLogicalDevice, true);
				recordVulkanCommandBuffer(this.vulkanCommandBuffers[i], this.vulkanFrameBuffers[i],
						vulkanSwapChainExtent.width(), vulkanSwapChainExtent.height());
			}
		}
	}

	private void recordVulkanCommandBuffer(VulkanCommandBuffer vulkanCommandBuffer, VulkanFrameBuffer vulkanFrameBuffer,
										   int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
			clearValues.apply(0, v -> v.color().float32(0, 0.5f).float32(1, 0.7f).float32(2, 1.0f));
			VkRenderPassBeginInfo renderPassBeginInfo =
					VkRenderPassBeginInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
							.pClearValues(clearValues).renderArea(a -> a.extent().set(width, height))
							.framebuffer(vulkanFrameBuffer.getVkFrameBuffer());

			vulkanCommandBuffer.beginRecording();
			VK13.vkCmdBeginRenderPass(vulkanCommandBuffer.getVkCommandBuffer(), renderPassBeginInfo,
					VK13.VK_SUBPASS_CONTENTS_INLINE);
			VK13.vkCmdEndRenderPass(vulkanCommandBuffer.getVkCommandBuffer());
			vulkanCommandBuffer.endRecording();
		}
	}

	public void cleanup()
	{
		Arrays.asList(this.vulkanFrameBuffers).forEach(VulkanFrameBuffer::cleanup);
		this.vulkanSwapChainRenderPass.cleanup();
		Arrays.asList(this.vulkanCommandBuffers).forEach(VulkanCommandBuffer::cleanup);
		Arrays.asList(this.vulkanFences).forEach(VulkanFence::cleanup);
	}

	public void waitForVulkanFence()
	{
		int idx = this.vulkanSwapChain.getCurrentFrame();
		VulkanFence currentVulkanFence = this.vulkanFences[idx];
		currentVulkanFence.vulkanFenceWait();
	}

	public void submit(VulkanGraphicsQueue vulkanGraphicsQueue)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			int idx = this.vulkanSwapChain.getCurrentFrame();
			VulkanCommandBuffer vulkanCommandBuffer = this.vulkanCommandBuffers[idx];
			VulkanFence currentVulkanFence = this.vulkanFences[idx];
			currentVulkanFence.reset();
			//VulkanSwapChain.SynchronizationVulkanSemaphores synchronizationVulkanSemaphores =
			//		this.vulkanSwapChain.getSyncVulkanSemaphoreList()[idx];
			//vulkanGraphicsQueue.submit(stack.pointers(vulkanCommandBuffer.getVkCommandBuffer()),
			//		stack.longs(synchronizationVulkanSemaphores.imageAcquisitionVulkanSemaphore().getVkSemaphore()),
			//		stack.ints(VK13.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
			//		stack.longs(synchronizationVulkanSemaphores.renderCompleteVulkanSemaphore().getVkSemaphore()),
			//		currentVulkanFence);
		}
	}
}

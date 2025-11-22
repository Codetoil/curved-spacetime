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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import java.nio.LongBuffer;
import java.util.Arrays;

public class VulkanRenderModuleForwardRenderActivity
{
	private final VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain;
	private final VulkanRenderModuleSwapChainRenderPass vulkanRenderModuleSwapChainRenderPass;
	private final VulkanRenderModuleFrameBuffer[] vulkanRenderModuleFrameBuffers;
	private final VulkanModuleCommandBuffer[] vulkanModuleCommandBuffers;
	private final VulkanModuleFence[] vulkanModuleFences;

	public VulkanRenderModuleForwardRenderActivity(VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain,
												   VulkanModuleCommandPool vulkanModuleCommandPool)
	{
		this.vulkanRenderModuleSwapChain = vulkanRenderModuleSwapChain;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VulkanModuleLogicalDevice vulkanModuleLogicalDevice =
					this.vulkanRenderModuleSwapChain.getVulkanLogicalDevice();
			VkExtent2D vulkanSwapChainExtent = this.vulkanRenderModuleSwapChain.getVulkanSwapChainExtent();
			VulkanRenderModuleImageView[] vulkanRenderModuleImageViews =
					this.vulkanRenderModuleSwapChain.getVulkanImageViews();
			int numImages = vulkanRenderModuleImageViews.length;

			this.vulkanRenderModuleSwapChainRenderPass =
					new VulkanRenderModuleSwapChainRenderPass(vulkanRenderModuleSwapChain);

			LongBuffer pAttachments = stack.mallocLong(1);
			this.vulkanRenderModuleFrameBuffers = new VulkanRenderModuleFrameBuffer[numImages];
			for (int i = 0; i < numImages; i++)
			{
				pAttachments.put(0, vulkanRenderModuleImageViews[i].getVkImageView());
				this.vulkanRenderModuleFrameBuffers[i] =
						new VulkanRenderModuleFrameBuffer(vulkanModuleLogicalDevice, vulkanSwapChainExtent.width(),
								vulkanSwapChainExtent.height(), pAttachments,
								this.vulkanRenderModuleSwapChainRenderPass.getVkRenderPass());
			}

			this.vulkanModuleCommandBuffers = new VulkanModuleCommandBuffer[numImages];
			this.vulkanModuleFences = new VulkanModuleFence[numImages];
			for (int i = 0; i < numImages; i++)
			{
				this.vulkanModuleCommandBuffers[i] =
						new VulkanModuleCommandBuffer(vulkanModuleCommandPool, true, false);
				this.vulkanModuleFences[i] = new VulkanModuleFence(vulkanModuleLogicalDevice, true);
				recordVulkanCommandBuffer(this.vulkanModuleCommandBuffers[i], this.vulkanRenderModuleFrameBuffers[i],
						vulkanSwapChainExtent.width(), vulkanSwapChainExtent.height());
			}
		}
	}

	private void recordVulkanCommandBuffer(VulkanModuleCommandBuffer vulkanModuleCommandBuffer,
										   VulkanRenderModuleFrameBuffer vulkanRenderModuleFrameBuffer,
										   int width, int height)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
			clearValues.apply(0, v -> v.color().float32(0, 0.5f).float32(1, 0.7f).float32(2, 1.0f));
			VkRenderPassBeginInfo renderPassBeginInfo =
					VkRenderPassBeginInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
							.pClearValues(clearValues).renderArea(a -> a.extent().set(width, height))
							.framebuffer(vulkanRenderModuleFrameBuffer.getVkFrameBuffer());

			vulkanModuleCommandBuffer.beginRecording();
			VK13.vkCmdBeginRenderPass(vulkanModuleCommandBuffer.getVkCommandBuffer(), renderPassBeginInfo,
					VK13.VK_SUBPASS_CONTENTS_INLINE);
			VK13.vkCmdEndRenderPass(vulkanModuleCommandBuffer.getVkCommandBuffer());
			vulkanModuleCommandBuffer.endRecording();
		}
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
		try (MemoryStack stack = MemoryStack.stackPush())
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
}

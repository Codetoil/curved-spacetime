/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.api.render.vulkan;

import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanSwapChainRenderPass
{
	private final VulkanSwapChain swapChain;
	private final long vkRenderPass;

	public VulkanSwapChainRenderPass(VulkanSwapChain swapChain)
	{
		this.swapChain = swapChain;

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);

			// Color attachment
			attachments.get(0).format(swapChain.getVulkanSurfaceFormat().imageFormat())
					.samples(VK10.VK_SAMPLE_COUNT_1_BIT).loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE).initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
					.finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

			VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1, stack).attachment(0)
					.layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

			VkSubpassDescription.Buffer subPass =
					VkSubpassDescription.calloc(1, stack).pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
							.colorAttachmentCount(colorReference.remaining()).pColorAttachments(colorReference);

			VkSubpassDependency.Buffer subpassDependencies = VkSubpassDependency.calloc(1, stack);
			subpassDependencies.get(0).srcSubpass(VK10.VK_SUBPASS_EXTERNAL).dstSubpass(0)
					.srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT).srcAccessMask(0)
					.dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

			VkRenderPassCreateInfo renderPassInfo =
					VkRenderPassCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
							.pAttachments(attachments).pSubpasses(subPass).pDependencies(subpassDependencies);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(
					VK10.vkCreateRenderPass(swapChain.getVulkanLogicalDevice().getVkDevice(), renderPassInfo, null, lp),
					"Failed to create render pass");
			this.vkRenderPass = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK10.vkDestroyRenderPass(this.swapChain.getVulkanLogicalDevice().getVkDevice(), this.vkRenderPass, null);
	}

	public long getVkRenderPass()
	{
		return this.vkRenderPass;
	}
}

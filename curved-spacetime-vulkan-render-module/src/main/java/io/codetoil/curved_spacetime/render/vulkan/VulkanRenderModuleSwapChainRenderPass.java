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

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import vulkan.*;

import java.lang.foreign.MemorySegment;

public class VulkanRenderModuleSwapChainRenderPass
{
	private final VulkanRenderModuleSwapChain swapChain;
	private final MemorySegment vkRenderPass;

	public VulkanRenderModuleSwapChainRenderPass(VulkanRenderModuleSwapChain swapChain)
	{
		this.swapChain = swapChain;

		// Color attachment
		MemorySegment attachments =
				VkAttachmentDescription.allocateArray(1, MainModuleEngine.getInstance().nativeAllocator);
		MemorySegment attachment = attachments.asSlice(0, VkAttachmentDescription.layout());
		VkAttachmentDescription.format(attachment, swapChain.getVulkanSurfaceFormat().imageFormat());
		VkAttachmentDescription.samples(attachment, Vulkan.VK_SAMPLE_COUNT_1_BIT());
		VkAttachmentDescription.loadOp(attachment, Vulkan.VK_ATTACHMENT_LOAD_OP_CLEAR());
		VkAttachmentDescription.storeOp(attachment, Vulkan.VK_ATTACHMENT_STORE_OP_STORE());
		VkAttachmentDescription.initialLayout(attachment, Vulkan.VK_IMAGE_LAYOUT_UNDEFINED());
		VkAttachmentDescription.finalLayout(attachment, Vulkan.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR());

		MemorySegment colorReferences =
				VkAttachmentReference.allocateArray(1, MainModuleEngine.getInstance().nativeAllocator);
		MemorySegment colorReference = colorReferences.asSlice(0, VkAttachmentReference.layout());
		VkAttachmentReference.attachment(colorReference, 0);
		VkAttachmentReference.layout(colorReference, Vulkan.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL());

		MemorySegment subPassDescriptions =
				VkSubpassDescription.allocateArray(1, MainModuleEngine.getInstance().nativeAllocator);
		MemorySegment subPassDescription = subPassDescriptions.asSlice(0, VkSubpassDescription.layout());
		VkSubpassDescription.pipelineBindPoint(subPassDescription, Vulkan.VK_PIPELINE_BIND_POINT_GRAPHICS());
		VkSubpassDescription.colorAttachmentCount(subPassDescription,
				(int) (colorReferences.byteSize() / VkAttachmentReference.sizeof()));
		VkSubpassDescription.pColorAttachments(subPassDescription, colorReferences);

		MemorySegment subpassDependencies =
				VkSubpassDependency.allocateArray(1, MainModuleEngine.getInstance().nativeAllocator);
		MemorySegment subpassDependency = subpassDependencies.asSlice(0, VkSubpassDependency.layout());
		VkSubpassDependency.srcSubpass(subpassDependency, Vulkan.VK_SUBPASS_EXTERNAL());
		VkSubpassDependency.dstSubpass(subpassDependency, 0);
		VkSubpassDependency.srcStageMask(subpassDependency, Vulkan.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT());
		VkSubpassDependency.dstStageMask(subpassDependency, Vulkan.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT());
		VkSubpassDependency.srcAccessMask(subpassDependency, 0);
		VkSubpassDependency.dstAccessMask(subpassDependency, Vulkan.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT());

		MemorySegment renderPassInfo = VkRenderPassCreateInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkRenderPassCreateInfo.sType(renderPassInfo, Vulkan.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO());
		VkRenderPassCreateInfo.pAttachments(renderPassInfo, attachments);
		VkRenderPassCreateInfo.pSubpasses(renderPassInfo, subPassDescriptions);
		VkRenderPassCreateInfo.pDependencies(renderPassInfo, subpassDependencies);

		this.vkRenderPass = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkRenderPass);
		VulkanUtils.vkCheck(Vulkan.vkCreateRenderPass(swapChain.getVulkanLogicalDevice().getVkDevice(),
				renderPassInfo, null, this.vkRenderPass), "Failed to create render pass");

	}

	public void cleanup()
	{
		Vulkan.vkDestroyRenderPass(this.swapChain.getVulkanLogicalDevice().getVkDevice(), this.vkRenderPass, null);
	}

	public MemorySegment getVkRenderPass()
	{
		return this.vkRenderPass;
	}
}

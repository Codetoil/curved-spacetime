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

import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import vulkan.VkFramebufferCreateInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public class VulkanRenderModuleFrameBuffer
{
	private final VulkanModuleLogicalDevice logicalDevice;
	private final MemorySegment vkFrameBuffer;

	public VulkanRenderModuleFrameBuffer(VulkanModuleLogicalDevice logicalDevice, int width, int height,
										 MemorySegment pAttachments,
										 MemorySegment renderPass)
	{
		this.logicalDevice = logicalDevice;

		MemorySegment framebufferCreateInfo = VkFramebufferCreateInfo.allocate(arena);
		VkFramebufferCreateInfo.sType(framebufferCreateInfo, Vulkan.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO());
		VkFramebufferCreateInfo.pAttachments(framebufferCreateInfo, pAttachments);
		VkFramebufferCreateInfo.width(framebufferCreateInfo, width);
		VkFramebufferCreateInfo.height(framebufferCreateInfo, height);
		VkFramebufferCreateInfo.layers(framebufferCreateInfo, 1);
		VkFramebufferCreateInfo.renderPass(framebufferCreateInfo, renderPass);

		this.vkFrameBuffer = arena.allocate(Vulkan.VkFramebuffer);
		VulkanUtils.vkCheck(Vulkan.vkCreateFramebuffer(logicalDevice.getVkDevice(), framebufferCreateInfo,
						null, this.vkFrameBuffer),
				"Failed to create FrameBuffer");
	}

	public void cleanup()
	{
		Vulkan.vkDestroyFramebuffer(this.logicalDevice.getVkDevice(), this.vkFrameBuffer, null);
	}

	public MemorySegment getVkFrameBuffer()
	{
		return this.vkFrameBuffer;
	}
}

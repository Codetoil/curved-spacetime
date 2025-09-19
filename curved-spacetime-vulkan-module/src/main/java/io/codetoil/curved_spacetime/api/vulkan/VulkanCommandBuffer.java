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

package io.codetoil.curved_spacetime.api.vulkan;

import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

public class VulkanCommandBuffer
{
	private final VulkanCommandPool commandPool;
	private final boolean oneTimeSubmit;
	private final VkCommandBuffer vkCommandBuffer;
	private final boolean primary;

	public VulkanCommandBuffer(VulkanCommandPool commandPool, boolean primary, boolean oneTimeSubmit)
	{
		Logger.trace("Creating command buffer");
		this.commandPool = commandPool;
		this.primary = primary;
		this.oneTimeSubmit = oneTimeSubmit;
		VkDevice vkDevice = commandPool.getVulkanLogicalDevice().getVkDevice();

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferAllocateInfo cmdBufAllocateInfo =
					VkCommandBufferAllocateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
							.commandPool(commandPool.getVkCommandPool())
							.level(primary ? VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY :
									VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY).commandBufferCount(1);
			PointerBuffer pb = stack.mallocPointer(1);
			VulkanUtils.vkCheck(VK10.vkAllocateCommandBuffers(vkDevice, cmdBufAllocateInfo, pb),
					"Failed to allocate render command buffer");

			this.vkCommandBuffer = new VkCommandBuffer(pb.get(0), vkDevice);
		}
	}

	public void beginRecording()
	{
		this.beginRecording(null);
	}

	public void beginRecording(VkCommandBufferInheritanceInfo inheritanceInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo cmdBufInfo =
					VkCommandBufferBeginInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

			if (this.oneTimeSubmit)
			{
				cmdBufInfo.flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
			}
			if (!this.primary)
			{
				if (inheritanceInfo == null)
				{
					throw new RuntimeException("Secondary buffers must declare inheritance info");
				}
				VkCommandBufferInheritanceInfo vkInheritanceInfo = VkCommandBufferInheritanceInfo.calloc(stack)
						.sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
						.renderPass(inheritanceInfo.renderPass()).subpass(inheritanceInfo.subpass())
						.framebuffer(inheritanceInfo.framebuffer());
				cmdBufInfo.pInheritanceInfo(vkInheritanceInfo);
				cmdBufInfo.flags(VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
			}
			VulkanUtils.vkCheck(VK10.vkBeginCommandBuffer(this.vkCommandBuffer, cmdBufInfo),
					"Failed to create command buffer");
		}
	}

	public void cleanup()
	{
		Logger.trace("Destroying command buffer");
		VK10.vkFreeCommandBuffers(this.commandPool.getVulkanLogicalDevice().getVkDevice(),
				this.commandPool.getVkCommandPool(), this.vkCommandBuffer);
	}

	public void endRecording()
	{
		VulkanUtils.vkCheck(VK10.vkEndCommandBuffer(this.vkCommandBuffer), "Failed to end command buffer");
	}

	public VkCommandBuffer getVkCommandBuffer()
	{
		return this.vkCommandBuffer;
	}

	public void reset()
	{
		VK10.vkResetCommandBuffer(this.vkCommandBuffer, VK10.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
	}
}

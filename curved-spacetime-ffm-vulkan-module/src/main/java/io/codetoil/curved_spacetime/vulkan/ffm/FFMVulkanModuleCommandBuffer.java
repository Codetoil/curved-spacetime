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

package io.codetoil.curved_spacetime.vulkan.ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.vulkan.ffm.utils.FFMVulkanUtils;
import vulkan.VkCommandBufferAllocateInfo;
import vulkan.VkCommandBufferBeginInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.util.logging.Logger;

public class FFMVulkanModuleCommandBuffer
{
	private final FFMVulkanModuleCommandPool commandPool;
	private final boolean oneTimeSubmit;
	private final MemorySegment vkCommandBuffer;
	private final boolean primary;
	private final Logger logger;

	public FFMVulkanModuleCommandBuffer(FFMVulkanModuleCommandPool commandPool, boolean primary, boolean oneTimeSubmit,
	                                    Logger logger)
	{
		this.logger = logger;
		this.logger.finer("Creating command buffer");
		this.commandPool = commandPool;
		this.primary = primary;
		this.oneTimeSubmit = oneTimeSubmit;
		MemorySegment vkDevice = commandPool.getVulkanLogicalDevice().getVkDevice();

		MemorySegment cmdBufAllocateInfo =
				VkCommandBufferAllocateInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkCommandBufferAllocateInfo.sType(cmdBufAllocateInfo, Vulkan.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO());
		VkCommandBufferAllocateInfo.commandPool(cmdBufAllocateInfo, commandPool.getVkCommandPool());
		VkCommandBufferAllocateInfo.level(cmdBufAllocateInfo, primary ? Vulkan.VK_COMMAND_BUFFER_LEVEL_PRIMARY() :
				Vulkan.VK_COMMAND_BUFFER_LEVEL_SECONDARY());
		VkCommandBufferAllocateInfo.commandBufferCount(cmdBufAllocateInfo, 1);
		this.vkCommandBuffer = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkCommandBuffer);
		FFMVulkanUtils.vkCheck(Vulkan.vkAllocateCommandBuffers(vkDevice, cmdBufAllocateInfo, this.vkCommandBuffer),
				"Failed to allocate render command buffer");
	}

	public void beginRecording()
	{
		this.beginRecording(null);
	}

	public void beginRecording(MemorySegment inheritanceInfo)
	{
		MemorySegment cmdBufInfo = VkCommandBufferBeginInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkCommandBufferBeginInfo.sType(cmdBufInfo, Vulkan.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO());

		if (this.oneTimeSubmit)
		{
			VkCommandBufferBeginInfo.flags(cmdBufInfo, Vulkan.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT());
		}
		if (!this.primary)
		{
			if (inheritanceInfo == null)
			{
				throw new RuntimeException("Secondary buffers must declare inheritance info");
			}
			VkCommandBufferBeginInfo.pInheritanceInfo(cmdBufInfo, inheritanceInfo);
			VkCommandBufferBeginInfo.flags(cmdBufInfo, Vulkan.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT());
		}
		FFMVulkanUtils.vkCheck(Vulkan.vkBeginCommandBuffer(this.vkCommandBuffer, cmdBufInfo),
				"Failed to create command buffer");
	}

	public void cleanup()
	{
		this.logger.finer("Destroying command buffer");
		Vulkan.vkFreeCommandBuffers(this.commandPool.getVulkanLogicalDevice().getVkDevice(),
				this.commandPool.getVkCommandPool(), 1, this.vkCommandBuffer);
	}

	public void endRecording()
	{
		FFMVulkanUtils.vkCheck(Vulkan.vkEndCommandBuffer(this.vkCommandBuffer), "Failed to end command buffer");
	}

	public MemorySegment getVkCommandBuffer()
	{
		return this.vkCommandBuffer;
	}

	public void reset()
	{
		Vulkan.vkResetCommandBuffer(this.vkCommandBuffer, Vulkan.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT());
	}
}

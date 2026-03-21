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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import vulkan.VkCommandPoolCreateInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.util.logging.Logger;

public class VulkanModuleCommandPool
{
	private final VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	private final MemorySegment vkCommandPool;
	private final Logger logger;

	public VulkanModuleCommandPool(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueFamilyIndex,
								   Logger logger)
	{
		this.logger = logger;
		this.logger.fine("Creating Vulkan CommandPool for " + vulkanModuleLogicalDevice);

		this.vulkanModuleLogicalDevice = vulkanModuleLogicalDevice;
		MemorySegment cmdPoolInfo = VkCommandPoolCreateInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkCommandPoolCreateInfo.sType(cmdPoolInfo, Vulkan.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO());
		VkCommandPoolCreateInfo.flags(cmdPoolInfo, Vulkan.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT());
		VkCommandPoolCreateInfo.queueFamilyIndex(cmdPoolInfo, queueFamilyIndex);

		this.vkCommandPool = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkCommandPool);
		VulkanUtils.vkCheck(
				Vulkan.vkCreateCommandPool(vulkanModuleLogicalDevice.getVkDevice(), cmdPoolInfo, null,
						this.vkCommandPool), "failed to create command pool");
	}

	public void cleanup()
	{
		Vulkan.vkDestroyCommandPool(this.vulkanModuleLogicalDevice.getVkDevice(), this.vkCommandPool, null);
	}

	public VulkanModuleLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanModuleLogicalDevice;
	}

	public MemorySegment getVkCommandPool()
	{
		return this.vkCommandPool;
	}
}

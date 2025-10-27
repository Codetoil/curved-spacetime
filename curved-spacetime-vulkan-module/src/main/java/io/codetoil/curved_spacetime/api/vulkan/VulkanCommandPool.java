/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025 Anthony Michalek
 * (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.tinylog.Logger;

import java.nio.LongBuffer;

public class VulkanCommandPool
{
	private final VulkanLogicalDevice vulkanLogicalDevice;
	private final long vkCommandPool;

	public VulkanCommandPool(VulkanLogicalDevice vulkanLogicalDevice, int queueFamilyIndex)
	{
		Logger.debug("Creating Vulkan CommandPool for " + vulkanLogicalDevice);

		this.vulkanLogicalDevice = vulkanLogicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandPoolCreateInfo cmdPoolInfo =
					VkCommandPoolCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
							.flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
							.queueFamilyIndex(queueFamilyIndex);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK10.vkCreateCommandPool(vulkanLogicalDevice.getVkDevice(), cmdPoolInfo, null, lp),
					"failed to create command pool");

			this.vkCommandPool = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK10.vkDestroyCommandPool(this.vulkanLogicalDevice.getVkDevice(), this.vkCommandPool, null);
	}

	public VulkanLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanLogicalDevice;
	}

	public long getVkCommandPool()
	{
		return this.vkCommandPool;
	}
}

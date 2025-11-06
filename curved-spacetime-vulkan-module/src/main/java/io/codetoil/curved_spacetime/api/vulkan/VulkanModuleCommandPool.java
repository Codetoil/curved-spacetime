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
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.tinylog.Logger;

import java.nio.LongBuffer;

public class VulkanModuleCommandPool
{
	private final VulkanModuleVulkanContext vulkanModuleVulkanContext;
	private final long vkCommandPool;

	public VulkanModuleCommandPool(VulkanModuleVulkanContext context, int queueFamilyIndex, boolean supportReset)
	{
		Logger.debug("Creating Vulkan CommandPool for " + context.getLogicalDevice());

		this.vulkanModuleVulkanContext = context;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandPoolCreateInfo cmdPoolInfo =
					VkCommandPoolCreateInfo.calloc(stack)
							.sType$Default()
							.queueFamilyIndex(queueFamilyIndex);
			if (supportReset)
			{
				cmdPoolInfo.flags(VK13.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
			}

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(
					VK13.vkCreateCommandPool(context.getLogicalDevice().getVkDevice(), cmdPoolInfo, null, lp),
					"failed to create command pool");

			this.vkCommandPool = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK13.vkDestroyCommandPool(this.vulkanModuleVulkanContext.getLogicalDevice().getVkDevice(), this.vkCommandPool,
				null);
	}

	public long getVkCommandPool()
	{
		return this.vkCommandPool;
	}

	public VulkanModuleVulkanContext getContext()
	{
		return this.vulkanModuleVulkanContext;
	}

	public void reset()
	{
		VK13.vkResetCommandPool(this.vulkanModuleVulkanContext.getLogicalDevice().getVkDevice(), this.vkCommandPool, 0);
	}
}

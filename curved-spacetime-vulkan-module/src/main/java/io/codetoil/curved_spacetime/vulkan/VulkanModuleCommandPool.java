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

import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.nio.LongBuffer;
import java.util.logging.Logger;

/**
 * A Vulkan command pool, from which command buffers for one queue family are allocated.
 * <p>
 * A pool is tied to the queue family it was created for, so buffers allocated from it can only be
 * submitted to queues in that family. Created with
 * {@code VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT}, so individual buffers can be reset and
 * re-recorded rather than the whole pool having to be reset at once.
 */
public class VulkanModuleCommandPool
{
	private final VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	private final long vkCommandPool;
	private final Logger logger;

	/**
	 * Creates a command pool for one queue family.
	 *
	 * @param vulkanModuleLogicalDevice the device to create the pool on
	 * @param queueFamilyIndex          the queue family buffers from this pool may be submitted to
	 * @param logger                    the logger to write pool diagnostics to
	 * @throws AssertionError if the pool cannot be created
	 */
	public VulkanModuleCommandPool(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueFamilyIndex,
								   Logger logger)
	{
		this.logger = logger;
		this.logger.fine("Creating Vulkan CommandPool for " + vulkanModuleLogicalDevice);

		this.vulkanModuleLogicalDevice = vulkanModuleLogicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandPoolCreateInfo cmdPoolInfo =
					VkCommandPoolCreateInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
							.flags(VK13.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
							.queueFamilyIndex(queueFamilyIndex);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(
					VK13.vkCreateCommandPool(vulkanModuleLogicalDevice.getVkDevice(), cmdPoolInfo, null, lp),
					"failed to create command pool");

			this.vkCommandPool = lp.get(0);
		}
	}

	/**
	 * Destroys the pool, freeing every command buffer allocated from it.
	 */
	public void cleanup()
	{
		VK13.vkDestroyCommandPool(this.vulkanModuleLogicalDevice.getVkDevice(), this.vkCommandPool, null);
	}

	/**
	 * Returns the device this pool was created on.
	 *
	 * @return the owning logical device
	 */
	public VulkanModuleLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanModuleLogicalDevice;
	}

	/**
	 * Returns the underlying Vulkan handle.
	 *
	 * @return the {@code VkCommandPool} handle
	 */
	public long getVkCommandPool()
	{
		return this.vkCommandPool;
	}
}

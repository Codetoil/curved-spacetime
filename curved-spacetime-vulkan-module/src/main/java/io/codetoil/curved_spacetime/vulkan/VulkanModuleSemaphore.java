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
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;

/**
 * A Vulkan semaphore, used to order work between queue submissions on the device.
 * <p>
 * Semaphores synchronise GPU work against other GPU work; use a {@link VulkanModuleFence} when the
 * host needs to wait instead. The handle is created on construction and lives until
 * {@link #cleanup()}.
 */
public class VulkanModuleSemaphore
{
	private final VulkanModuleLogicalDevice logicalDevice;
	private final long vkSemaphore;

	/**
	 * Creates a semaphore on the given device.
	 *
	 * @param logicalDevice the device to create the semaphore on
	 * @throws AssertionError if the semaphore cannot be created
	 */
	public VulkanModuleSemaphore(VulkanModuleLogicalDevice logicalDevice)
	{
		this.logicalDevice = logicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSemaphoreCreateInfo semaphoreCreateInfo =
					VkSemaphoreCreateInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK13.vkCreateSemaphore(logicalDevice.getVkDevice(), semaphoreCreateInfo, null, lp),
					"Failed to create semaphore");
			this.vkSemaphore = lp.get(0);
		}
	}

	/**
	 * Destroys the semaphore.
	 */
	public void cleanup()
	{
		VK13.vkDestroySemaphore(this.logicalDevice.getVkDevice(), this.vkSemaphore, null);
	}

	/**
	 * Returns the underlying Vulkan handle.
	 *
	 * @return the {@code VkSemaphore} handle
	 */
	public long getVkSemaphore()
	{
		return this.vkSemaphore;
	}
}

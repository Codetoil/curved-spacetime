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
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;

public class VulkanSemaphore
{
	private final VulkanLogicalDevice logicalDevice;
	private final long vkSemaphore;

	public VulkanSemaphore(VulkanLogicalDevice logicalDevice)
	{
		this.logicalDevice = logicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSemaphoreCreateInfo semaphoreCreateInfo =
					VkSemaphoreCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK10.vkCreateSemaphore(logicalDevice.getVkDevice(), semaphoreCreateInfo, null, lp),
					"Failed to create semaphore");
			this.vkSemaphore = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK10.vkDestroySemaphore(this.logicalDevice.getVkDevice(), this.vkSemaphore, null);
	}

	public long getVkSemaphore()
	{
		return this.vkSemaphore;
	}
}

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
import vulkan.VkSemaphoreCreateInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;

public class FFMVulkanModuleSemaphore
{
	private final FFMVulkanModuleLogicalDevice logicalDevice;
	private final MemorySegment vkSemaphore;

	public FFMVulkanModuleSemaphore(FFMVulkanModuleLogicalDevice logicalDevice)
	{
		this.logicalDevice = logicalDevice;
		MemorySegment semaphoreCreateInfo =
				VkSemaphoreCreateInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkSemaphoreCreateInfo.sType(semaphoreCreateInfo, Vulkan.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO());
		this.vkSemaphore = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkSemaphore);
		FFMVulkanUtils.vkCheck(Vulkan.vkCreateSemaphore(logicalDevice.getVkDevice(), semaphoreCreateInfo, null,
				this.vkSemaphore), "Failed to create semaphore");
	}

	public void cleanup()
	{
		Vulkan.vkDestroySemaphore(this.logicalDevice.getVkDevice(), this.vkSemaphore, null);
	}

	public MemorySegment getVkSemaphore()
	{
		return this.vkSemaphore;
	}
}

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
import vulkan.VkFenceCreateInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;

public class FFMVulkanModuleFence
{
	private final FFMVulkanModuleLogicalDevice logicalDevice;
	private final MemorySegment vkFence;

	public FFMVulkanModuleFence(FFMVulkanModuleLogicalDevice logicalDevice, boolean signaled)
	{
		this.logicalDevice = logicalDevice;
		MemorySegment fenceCreateInfo = VkFenceCreateInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkFenceCreateInfo.sType(fenceCreateInfo, Vulkan.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO());
		VkFenceCreateInfo.flags(fenceCreateInfo, signaled ? Vulkan.VK_FENCE_CREATE_SIGNALED_BIT() : 0);

		this.vkFence = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkFence);
		FFMVulkanUtils.vkCheck(Vulkan.vkCreateFence(logicalDevice.getVkDevice(), fenceCreateInfo, null,
						this.vkFence),
				"Failed to create fence");
	}

	public void cleanup()
	{
		Vulkan.vkDestroyFence(this.logicalDevice.getVkDevice(), this.vkFence, null);
	}

	public void vulkanFenceWait()
	{
		Vulkan.vkWaitForFences(this.logicalDevice.getVkDevice(), 1, this.vkFence,
				Vulkan.VK_TRUE(), Long.MAX_VALUE);
	}

	public MemorySegment getVkFence()
	{
		return this.vkFence;
	}

	public void reset()
	{
		Vulkan.vkResetFences(this.logicalDevice.getVkDevice(), 1, this.vkFence);
	}
}

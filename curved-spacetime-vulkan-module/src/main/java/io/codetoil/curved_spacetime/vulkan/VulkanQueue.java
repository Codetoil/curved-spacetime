/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright (c) 2024 Antonio Hernández Bejarano<br>
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
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class VulkanQueue
{

	protected final int queueFamilyIndex;
	protected final VkQueue vkQueue;

	public VulkanQueue(VulkanLogicalDevice vulkanLogicalDevice, int queueFamilyIndex, int queueIndex)
	{
		Logger.debug(
				"Creating queue for " + vulkanLogicalDevice + " queueFamilyIndex " + queueFamilyIndex + " queueIndex " +
						queueIndex);

		this.queueFamilyIndex = queueFamilyIndex;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pQueue = stack.mallocPointer(1);
			VK10.vkGetDeviceQueue(vulkanLogicalDevice.getVkDevice(), queueFamilyIndex, queueIndex, pQueue);
			long queue = pQueue.get(0);
			this.vkQueue = new VkQueue(queue, vulkanLogicalDevice.getVkDevice());
		}
	}

	public VkQueue getVkQueue()
	{
		return this.vkQueue;
	}

	public void waitIdle()
	{
		VK10.vkQueueWaitIdle(this.vkQueue);
	}

	public void submit(PointerBuffer vulkanCommandBuffers, LongBuffer waitVulkanSemapores,
					   IntBuffer waitVulkanDstStageMasks, LongBuffer signalVulkanSemaphores, VulkanFence vulkanFence)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSubmitInfo vkSubmitInfo = VkSubmitInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO)
					.pCommandBuffers(vulkanCommandBuffers).pSignalSemaphores(signalVulkanSemaphores);
			if (waitVulkanSemapores != null)
			{
				vkSubmitInfo.waitSemaphoreCount(waitVulkanSemapores.capacity()).pWaitSemaphores(waitVulkanSemapores)
						.pWaitDstStageMask(waitVulkanDstStageMasks);
			} else
			{
				vkSubmitInfo.waitSemaphoreCount(0);
			}
			long vulkanFenceHandle = vulkanFence != null ? vulkanFence.getVkFence() : VK10.VK_NULL_HANDLE;
			VulkanUtils.vkCheck(VK10.vkQueueSubmit(this.vkQueue, vkSubmitInfo, vulkanFenceHandle),
					"Failed to submit command to queue");
		}
	}

	public int getQueueFamilyIndex()
	{
		return this.queueFamilyIndex;
	}
}

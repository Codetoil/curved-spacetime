/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

public class VulkanModuleQueue
{

	protected final int queueFamilyIndex;
	protected final VkQueue vkQueue;

	public VulkanModuleQueue(VulkanModuleVulkanContext context, int queueFamilyIndex, int queueIndex)
	{
		Logger.debug(
				"Creating queue for " + context.getLogicalDevice() + " queueFamilyIndex " + queueFamilyIndex +
						" queueIndex " +
						queueIndex);

		this.queueFamilyIndex = queueFamilyIndex;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pQueue = stack.mallocPointer(1);
			VK13.vkGetDeviceQueue(context.getLogicalDevice().getVkDevice(), queueFamilyIndex, queueIndex, pQueue);
			long queue = pQueue.get(0);
			this.vkQueue = new VkQueue(queue, context.getLogicalDevice().getVkDevice());
		}
	}

	public VkQueue getVkQueue()
	{
		return this.vkQueue;
	}

	public void waitIdle()
	{
		VK13.vkQueueWaitIdle(this.vkQueue);
	}

	public void submit(VkCommandBufferSubmitInfo.Buffer vulkanCommandBuffers,
					   VkSemaphoreSubmitInfo.Buffer waitVulkanSemaphores,
					   VkSemaphoreSubmitInfo.Buffer signalVulkanSemaphores,
					   VulkanModuleFence vulkanModuleFence)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSubmitInfo2.Buffer vkSubmitInfo = VkSubmitInfo2
					.calloc(1, stack)
					.sType$Default()
					.pCommandBufferInfos(vulkanCommandBuffers)
					.pSignalSemaphoreInfos(signalVulkanSemaphores);
			if (waitVulkanSemaphores != null)
			{
				vkSubmitInfo.pWaitSemaphoreInfos(waitVulkanSemaphores);
			}
			long vulkanFenceHandle = vulkanModuleFence != null ? vulkanModuleFence.getVkFence() : VK13.VK_NULL_HANDLE;
			VulkanUtils.vkCheck(VK13.vkQueueSubmit2(this.vkQueue, vkSubmitInfo, vulkanFenceHandle),
					"Failed to submit command to queue");
		}
	}

	public int getQueueFamilyIndex()
	{
		return this.queueFamilyIndex;
	}
}

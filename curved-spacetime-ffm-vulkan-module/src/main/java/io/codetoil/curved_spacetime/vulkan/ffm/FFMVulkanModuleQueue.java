/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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
import vulkan.VkSubmitInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.util.logging.Logger;

public class FFMVulkanModuleQueue
{

	protected final int queueFamilyIndex;
	protected final MemorySegment vkQueue;
	protected final Logger logger;

	public FFMVulkanModuleQueue(FFMVulkanModuleLogicalDevice ffmVulkanModuleLogicalDevice, int queueFamilyIndex, int queueIndex,
	                            Logger logger)
	{
		this.logger = logger;
		this.logger.fine(
				"Creating queue for " + ffmVulkanModuleLogicalDevice + " queueFamilyIndex " + queueFamilyIndex +
						" queueIndex " +
						queueIndex);

		this.queueFamilyIndex = queueFamilyIndex;
		this.vkQueue = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkQueue);
		Vulkan.vkGetDeviceQueue(ffmVulkanModuleLogicalDevice.getVkDevice(), queueFamilyIndex, queueIndex, this.vkQueue);
	}

	public MemorySegment getVkQueue()
	{
		return this.vkQueue;
	}

	public void waitIdle()
	{
		Vulkan.vkQueueWaitIdle(this.vkQueue);
	}

	public void submit(MemorySegment vulkanCommandBuffers, MemorySegment waitVulkanSemaphores,
					   MemorySegment waitVulkanDstStageMasks, MemorySegment signalVulkanSemaphores,
					   FFMVulkanModuleFence ffmVulkanModuleFence)
	{
		MemorySegment vkSubmitInfo = VkSubmitInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkSubmitInfo.sType(vkSubmitInfo, Vulkan.VK_STRUCTURE_TYPE_SUBMIT_INFO());
		VkSubmitInfo.pCommandBuffers(vkSubmitInfo, vulkanCommandBuffers);
		VkSubmitInfo.pSignalSemaphores(vkSubmitInfo, signalVulkanSemaphores);
		if (waitVulkanSemaphores != null)
		{
			VkSubmitInfo.waitSemaphoreCount(vkSubmitInfo,
					(int) (waitVulkanSemaphores.byteSize() / Vulkan.VkSemaphore.byteSize()));
			VkSubmitInfo.pWaitSemaphores(vkSubmitInfo, waitVulkanSemaphores);
			VkSubmitInfo.pWaitDstStageMask(vkSubmitInfo, waitVulkanDstStageMasks);
		} else
		{
			VkSubmitInfo.waitSemaphoreCount(vkSubmitInfo, 0);
		}
		MemorySegment vulkanFenceHandle = ffmVulkanModuleFence != null ? ffmVulkanModuleFence.getVkFence() :
				Vulkan.VK_NULL_HANDLE();
		FFMVulkanUtils.vkCheck(Vulkan.vkQueueSubmit(this.vkQueue, 1, vkSubmitInfo, vulkanFenceHandle),
				"Failed to submit command to queue");
	}

	public int getQueueFamilyIndex()
	{
		return this.queueFamilyIndex;
	}
}

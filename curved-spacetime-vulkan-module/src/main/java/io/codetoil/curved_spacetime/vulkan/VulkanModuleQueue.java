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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.logging.Logger;

/**
 * A Vulkan queue, the channel through which recorded work reaches the device.
 * <p>
 * Queues are not created but retrieved: the logical device creates them all up front, and a queue
 * is identified by its family and its index within that family. Subclasses narrow this to a
 * particular capability, such as graphics or presentation.
 */
public class VulkanModuleQueue
{

	/**
	 * The family this queue belongs to, which determines what work it accepts.
	 */
	protected final int queueFamilyIndex;

	/**
	 * The underlying Vulkan queue.
	 */
	protected final VkQueue vkQueue;

	/**
	 * The logger this queue writes its diagnostics to.
	 */
	protected final Logger logger;

	/**
	 * Retrieves a queue from the given device.
	 *
	 * @param vulkanModuleLogicalDevice the device that owns the queue
	 * @param queueFamilyIndex          the family to take the queue from
	 * @param queueIndex                the index within that family
	 * @param logger                    the logger to write queue diagnostics to
	 */
	public VulkanModuleQueue(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueFamilyIndex, int queueIndex,
							 Logger logger)
	{
		this.logger = logger;
		this.logger.fine(
				"Creating queue for " + vulkanModuleLogicalDevice + " queueFamilyIndex " + queueFamilyIndex +
						" queueIndex " +
						queueIndex);

		this.queueFamilyIndex = queueFamilyIndex;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer pQueue = stack.mallocPointer(1);
			VK13.vkGetDeviceQueue(vulkanModuleLogicalDevice.getVkDevice(), queueFamilyIndex, queueIndex, pQueue);
			long queue = pQueue.get(0);
			this.vkQueue = new VkQueue(queue, vulkanModuleLogicalDevice.getVkDevice());
		}
	}

	/**
	 * Returns the underlying Vulkan queue.
	 *
	 * @return the {@code VkQueue}
	 */
	public VkQueue getVkQueue()
	{
		return this.vkQueue;
	}

	/**
	 * Blocks until every submission to this queue has completed.
	 */
	public void waitIdle()
	{
		VK13.vkQueueWaitIdle(this.vkQueue);
	}

	/**
	 * Submits recorded command buffers to this queue.
	 *
	 * @param vulkanCommandBuffers    the command buffers to execute
	 * @param waitVulkanSemaphores    semaphores to wait on before executing, or {@code null} to
	 *                                begin immediately
	 * @param waitVulkanDstStageMasks the pipeline stages at which each wait applies, positionally
	 *                                matching {@code waitVulkanSemaphores}
	 * @param signalVulkanSemaphores  semaphores to signal once execution completes
	 * @param vulkanModuleFence       a fence to signal on completion, or {@code null} for none
	 * @throws AssertionError if the submission is rejected
	 */
	public void submit(PointerBuffer vulkanCommandBuffers, LongBuffer waitVulkanSemaphores,
					   IntBuffer waitVulkanDstStageMasks, LongBuffer signalVulkanSemaphores,
					   VulkanModuleFence vulkanModuleFence)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSubmitInfo vkSubmitInfo = VkSubmitInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_SUBMIT_INFO)
					.pCommandBuffers(vulkanCommandBuffers).pSignalSemaphores(signalVulkanSemaphores);
			if (waitVulkanSemaphores != null)
			{
				vkSubmitInfo.waitSemaphoreCount(waitVulkanSemaphores.capacity()).pWaitSemaphores(waitVulkanSemaphores)
						.pWaitDstStageMask(waitVulkanDstStageMasks);
			} else
			{
				vkSubmitInfo.waitSemaphoreCount(0);
			}
			long vulkanFenceHandle = vulkanModuleFence != null ? vulkanModuleFence.getVkFence() : VK13.VK_NULL_HANDLE;
			VulkanUtils.vkCheck(VK13.vkQueueSubmit(this.vkQueue, vkSubmitInfo, vulkanFenceHandle),
					"Failed to submit command to queue");
		}
	}

	/**
	 * Returns the family this queue belongs to.
	 *
	 * @return the queue family index
	 */
	public int getQueueFamilyIndex()
	{
		return this.queueFamilyIndex;
	}
}

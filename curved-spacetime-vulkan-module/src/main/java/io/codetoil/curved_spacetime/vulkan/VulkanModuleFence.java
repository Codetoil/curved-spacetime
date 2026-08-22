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
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

/**
 * A Vulkan fence, used by the host to wait for work submitted to a queue to finish.
 * <p>
 * Fences synchronise the CPU against the GPU; use a {@link VulkanModuleSemaphore} to order GPU
 * work against other GPU work. A fence created signalled lets the first frame proceed without a
 * special case, since {@link #vulkanFenceWait()} then returns immediately.
 */
public class VulkanModuleFence
{
	private final VulkanModuleLogicalDevice logicalDevice;
	private final long vkFence;

	/**
	 * Creates a fence on the given device.
	 *
	 * @param logicalDevice the device to create the fence on
	 * @param signaled      whether the fence starts signalled, so the first wait returns at once
	 * @throws AssertionError if the fence cannot be created
	 */
	public VulkanModuleFence(VulkanModuleLogicalDevice logicalDevice, boolean signaled)
	{
		this.logicalDevice = logicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkFenceCreateInfo fenceCreateInfo =
					VkFenceCreateInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
							.flags(signaled ? VK13.VK_FENCE_CREATE_SIGNALED_BIT : 0);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK13.vkCreateFence(logicalDevice.getVkDevice(), fenceCreateInfo, null, lp),
					"Failed to create fence");
			this.vkFence = lp.get(0);
		}
	}

	/**
	 * Destroys the fence.
	 */
	public void cleanup()
	{
		VK13.vkDestroyFence(this.logicalDevice.getVkDevice(), this.vkFence, null);
	}

	/**
	 * Blocks until the fence is signalled.
	 * <p>
	 * Waits indefinitely; the caller is expected to have submitted work that will signal it.
	 */
	public void vulkanFenceWait()
	{
		VK13.vkWaitForFences(this.logicalDevice.getVkDevice(), this.vkFence, true, Long.MAX_VALUE);
	}

	/**
	 * Returns the underlying Vulkan handle.
	 *
	 * @return the {@code VkFence} handle
	 */
	public long getVkFence()
	{
		return this.vkFence;
	}

	/**
	 * Returns the fence to the unsignalled state, ready to be waited on again.
	 */
	public void reset()
	{
		VK13.vkResetFences(this.logicalDevice.getVkDevice(), this.vkFence);
	}
}

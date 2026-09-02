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

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleQueue;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.logging.Logger;

/**
 * A queue selected for graphics work.
 * <p>
 * Adds queue-family discovery to {@link VulkanModuleQueue}: rather than being told which family to
 * use, this finds one advertising the graphics capability.
 */
public class VulkanRenderModuleGraphicsQueue extends VulkanModuleQueue
{
	/**
	 * Retrieves a queue from an explicitly chosen family.
	 *
	 * @param vulkanModuleLogicalDevice the device that owns the queue
	 * @param queueFamilyIndex          the family to take the queue from
	 * @param queueIndex                the index within that family
	 * @param logger                    the logger to write queue diagnostics to
	 */
	public VulkanRenderModuleGraphicsQueue(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueFamilyIndex,
										   int queueIndex, Logger logger)
	{
		super(vulkanModuleLogicalDevice, queueFamilyIndex, queueIndex, logger);
	}

	/**
	 * Retrieves a queue from the first family that supports graphics.
	 *
	 * @param vulkanModuleLogicalDevice the device that owns the queue
	 * @param queueIndex                the index within the discovered family
	 * @param logger                    the logger to write queue diagnostics to
	 * @throws RuntimeException if the device advertises no graphics-capable queue family
	 */
	public VulkanRenderModuleGraphicsQueue(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueIndex,
										   Logger logger)
	{
		super(vulkanModuleLogicalDevice, getGraphicsQueueFamilyIndex(vulkanModuleLogicalDevice), queueIndex, logger);
	}

	private static int getGraphicsQueueFamilyIndex(VulkanModuleLogicalDevice vulkanModuleLogicalDevice)
	{
		int result = -1;
		VulkanModulePhysicalDevice vulkanModulePhysicalDevice = vulkanModuleLogicalDevice.getPhysicalDevice();
		VkQueueFamilyProperties.Buffer queuePropsBuff = vulkanModulePhysicalDevice.getVkQueueFamilyProps();
		int numQueuesFamilies = queuePropsBuff.capacity();
		for (int index = 0; index < numQueuesFamilies; index++)
		{
			VkQueueFamilyProperties props = queuePropsBuff.get(index);
			boolean graphicsQueue = (props.queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0;
			if (graphicsQueue)
			{
				result = index;
				break;
			}
		}

		if (result < 0)
		{
			throw new RuntimeException("Failed to get graphics Queue family index.");
		}
		return result;
	}

	/**
	 * A queue selected for presenting to a particular surface.
	 * <p>
	 * Presentation support is a property of a queue family <em>and</em> a surface, not of the
	 * device alone, so the family is discovered by asking about the surface. It may or may not be
	 * the same family as the graphics queue.
	 */
	public static class VulkanRenderPresentModuleGraphicsQueue extends VulkanRenderModuleGraphicsQueue
	{

		/**
		 * Retrieves a queue from the first family that can present to the given surface.
		 *
		 * @param logicalDevice the device that owns the queue
		 * @param surface       the surface presentation support is tested against
		 * @param queueIndex    the index within the discovered family
		 * @param logger        the logger to write queue diagnostics to
		 * @throws RuntimeException if no queue family can present to the surface
		 */
		public VulkanRenderPresentModuleGraphicsQueue(VulkanModuleLogicalDevice logicalDevice,
													  VulkanRenderModuleSurface surface, int queueIndex, Logger logger)
		{
			super(logicalDevice, getPresentQueueFamilyIndex(logicalDevice, surface), queueIndex, logger);
		}

		private static int getPresentQueueFamilyIndex(VulkanModuleLogicalDevice logicalDevice,
													  VulkanRenderModuleSurface surface)
		{
			int index = -1;
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				VulkanModulePhysicalDevice physicalDevice = logicalDevice.getPhysicalDevice();
				VkQueueFamilyProperties.Buffer queuePropsBuff = physicalDevice.getVkQueueFamilyProps();
				int numQueuesFamilies = queuePropsBuff.capacity();
				IntBuffer intBuffer = stack.mallocInt(1);
				for (int i = 0; i < numQueuesFamilies; i++)
				{
					KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice.getVkPhysicalDevice(), i,
							surface.getVkSurface(), intBuffer);
					boolean supportsPresentation = intBuffer.get(0) == VK13.VK_TRUE;
					if (supportsPresentation)
					{
						index = i;
						break;
					}
				}

				if (index < 0)
				{
					throw new RuntimeException("Failed to get Presentation Queue family index.");
				}
				return index;
			}
		}
	}
}

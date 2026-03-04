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
import vulkan.VkQueueFamilyProperties;
import vulkan.VkQueueFamilyProperties2;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.logging.Logger;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public class VulkanRenderModuleGraphicsQueue extends VulkanModuleQueue
{
	public VulkanRenderModuleGraphicsQueue(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueFamilyIndex,
										   int queueIndex, Logger logger)
	{
		super(vulkanModuleLogicalDevice, queueFamilyIndex, queueIndex, logger);
	}

	public VulkanRenderModuleGraphicsQueue(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, int queueIndex,
										   Logger logger)
	{
		super(vulkanModuleLogicalDevice, getGraphicsQueueFamilyIndex(vulkanModuleLogicalDevice), queueIndex, logger);
	}

	private static int getGraphicsQueueFamilyIndex(VulkanModuleLogicalDevice vulkanModuleLogicalDevice)
	{
		int result = -1;
		VulkanModulePhysicalDevice vulkanModulePhysicalDevice = vulkanModuleLogicalDevice.getPhysicalDevice();
		MemorySegment queuePropsArray = vulkanModulePhysicalDevice.getVkQueueFamilyProps2();
		int numQueuesFamilies = Math.toIntExact(queuePropsArray.byteSize() / VkQueueFamilyProperties2.sizeof());
		for (int index = 0; index < numQueuesFamilies; index++)
		{
			MemorySegment props2 = queuePropsArray.asSlice(index * VkQueueFamilyProperties2.sizeof(),
					VkQueueFamilyProperties2.layout());
			boolean graphicsQueue =
					(VkQueueFamilyProperties.queueFlags(VkQueueFamilyProperties2.queueFamilyProperties(props2)) &
							Vulkan.VK_QUEUE_GRAPHICS_BIT()) != 0;
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

	public static class VulkanRenderPresentModuleGraphicsQueue extends VulkanRenderModuleGraphicsQueue
	{

		public VulkanRenderPresentModuleGraphicsQueue(VulkanModuleLogicalDevice logicalDevice,
													  VulkanRenderModuleSurface surface, int queueIndex, Logger logger)
		{
			super(logicalDevice, getPresentQueueFamilyIndex(logicalDevice, surface), queueIndex, logger);
		}

		private static int getPresentQueueFamilyIndex(VulkanModuleLogicalDevice logicalDevice,
													  VulkanRenderModuleSurface surface)
		{
			int index = -1;
			VulkanModulePhysicalDevice physicalDevice = logicalDevice.getPhysicalDevice();
			MemorySegment queueProps2Array = physicalDevice.getVkQueueFamilyProps2();
			int numQueuesFamilies = Math.toIntExact(queueProps2Array.byteSize() / VkQueueFamilyProperties2.sizeof());
			MemorySegment supportsPresentationSegment = arena.allocateFrom(ValueLayout.ADDRESS,
					arena.allocate(ValueLayout.JAVA_INT));
			for (int i = 0; i < numQueuesFamilies; i++)
			{
				Vulkan.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice.getVkPhysicalDevice(), i,
						surface.getVkSurface(), supportsPresentationSegment);
				boolean supportsPresentation =
						supportsPresentationSegment.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0)
								== Vulkan.VK_TRUE();
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

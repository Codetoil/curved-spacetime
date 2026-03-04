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
import vulkan.*;


import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public class VulkanModuleLogicalDevice
{
	private static final MemorySegment VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME =
			arena.allocateFrom("VK_KHR_portability_subset");
	private final VulkanModulePhysicalDevice vulkanModulePhysicalDevice;
	private final MemorySegment vkDevice;

	private final Logger logger;

	public VulkanModuleLogicalDevice(VulkanModulePhysicalDevice vulkanModulePhysicalDevice, Logger logger)
	{
		this.logger = logger;
		this.logger.fine("Creating logical device");

		this.vulkanModulePhysicalDevice = vulkanModulePhysicalDevice;
		MemorySegment reqExtensions = this.createReqExtensions();
		// Enable all the queue families
		MemorySegment queuePropsArray = vulkanModulePhysicalDevice.getVkQueueFamilyProps2();
		long numQueueFamilies = queuePropsArray.byteSize() / VkQueueFamilyProperties.sizeof();
		MemorySegment queueCreateInfoArray =
				VkDeviceQueueCreateInfo.allocateArray(numQueueFamilies, arena);
		for (int index = 0; index < numQueueFamilies; index++)
		{
			MemorySegment queueProps = queuePropsArray.asSlice(index * VkQueueFamilyProperties.sizeof(),
					VkQueueFamilyProperties.sizeof());
			MemorySegment priorities = arena.allocate(ValueLayout.JAVA_FLOAT,
					VkDeviceQueueCreateInfo.queueCount(queueProps));
			MemorySegment queueCreateInfo = queueCreateInfoArray.asSlice(index * VkDeviceQueueCreateInfo.sizeof(),
					VkDeviceQueueCreateInfo.layout());
			VkDeviceQueueCreateInfo.sType(queueCreateInfo, Vulkan.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO());
			VkDeviceQueueCreateInfo.queueFamilyIndex(arena.allocateFrom(ValueLayout.JAVA_INT, index));
			VkDeviceQueueCreateInfo.pQueuePriorities(priorities);
		}

		MemorySegment deviceCreateInfo = VkDeviceCreateInfo.allocate(arena);
		VkDeviceCreateInfo.sType(deviceCreateInfo, Vulkan.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO());
		VkDeviceCreateInfo.ppEnabledExtensionNames(deviceCreateInfo, reqExtensions);
		VkDeviceCreateInfo.pQueueCreateInfos(queueCreateInfoArray);

		this.vkDevice = arena.allocate(Vulkan.VkPhysicalDevice);
		VulkanUtils.vkCheck(
				Vulkan.vkCreateDevice(vulkanModulePhysicalDevice.getVkPhysicalDevice(), deviceCreateInfo, null,
						this.vkDevice), "Failed to create device");
	}

	private MemorySegment createReqExtensions()
	{
		Set<MemorySegment> deviceExtensions = getDeviceExtensions();
		boolean usePortability =
				deviceExtensions.contains(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME)
						&& VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;

		var extensionList = new ArrayList<>(VulkanModulePhysicalDevice.REQUIRED_EXTENSIONS);
		if (usePortability)
		{
			extensionList.add(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME);
		}

		MemorySegment requiredExtensions = arena.allocate(VkExtensionProperties.layout(), extensionList.size());
		for (int index = 0; index < extensionList.size(); index++)
		{
			requiredExtensions.asSlice(index * VkExtensionProperties.sizeof(), VkExtensionProperties.layout())
							.copyFrom(extensionList.get(index));
		}
		VulkanUtils.reverseBytes(requiredExtensions);

		return requiredExtensions;
	}

	private Set<MemorySegment> getDeviceExtensions()
	{
		Set<MemorySegment> deviceExtensions = new HashSet<>();
		MemorySegment numExtensionsPtr = arena.allocateFrom(ValueLayout.ADDRESS, arena.allocate(ValueLayout.JAVA_INT));
		Vulkan.vkEnumerateDeviceExtensionProperties(this.vulkanModulePhysicalDevice.getVkPhysicalDevice(),
				null, numExtensionsPtr, null);
		int numExtensions = numExtensionsPtr.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		this.logger.fine("Device supports [" + numExtensions + "] extensions");

		MemorySegment propsArray = VkExtensionProperties.allocateArray(numExtensions, arena);
		Vulkan.vkEnumerateDeviceExtensionProperties(this.vulkanModulePhysicalDevice.getVkPhysicalDevice(),
				null,
				numExtensionsPtr, propsArray);
		for (int index = 0; index < numExtensions; index++)
		{
			MemorySegment props = propsArray.asSlice(index * VkExtensionProperties.sizeof(),
					VkExtensionProperties.layout());
			MemorySegment extensionName = VkExtensionProperties.extensionName(props);
			deviceExtensions.add(extensionName);
			this.logger.fine("Supported device extension [" + extensionName.getString(0) + "]");
		}
		return deviceExtensions;
	}

	public void cleanup()
	{
		this.logger.fine("Destroying Vulkan device");
		Vulkan.vkDestroyDevice(this.vkDevice, null);
	}

	public VulkanModulePhysicalDevice getPhysicalDevice()
	{
		return this.vulkanModulePhysicalDevice;
	}

	public MemorySegment getVkDevice()
	{
		return this.vkDevice;
	}

	public void waitIdle()
	{
		Vulkan.vkDeviceWaitIdle(this.vkDevice);
	}
}

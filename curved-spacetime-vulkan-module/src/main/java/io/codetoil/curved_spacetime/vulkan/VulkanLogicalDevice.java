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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class VulkanModuleLogicalDevice
{
	private final VulkanModulePhysicalDevice vulkanModulePhysicalDevice;
	private final VkDevice vkDevice;

	public VulkanModuleLogicalDevice(VulkanModulePhysicalDevice vulkanModulePhysicalDevice)
	{
		Logger.debug("Creating logical device");

		this.vulkanModulePhysicalDevice = vulkanModulePhysicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			PointerBuffer reqExtensions = this.createReqExtensions(stack);
			// Enable all the queue families
			VkQueueFamilyProperties.Buffer queuePropsBuff = vulkanModulePhysicalDevice.getVkQueueFamilyProps();
			int numQueueFamilies = queuePropsBuff.capacity();
			VkDeviceQueueCreateInfo.Buffer queueCreationInfoBuf =
					VkDeviceQueueCreateInfo.calloc(numQueueFamilies, stack);
			for (int index = 0; index < numQueueFamilies; index++)
			{
				FloatBuffer priorities = stack.callocFloat(queuePropsBuff.get(index).queueCount());
				queueCreationInfoBuf.get(index)
						.sType$Default()
						.queueFamilyIndex(index)
						.pQueuePriorities(priorities);
			}

			VkDeviceCreateInfo deviceCreateInfo =
					VkDeviceCreateInfo.calloc(stack)
							.sType$Default()
							.ppEnabledExtensionNames(reqExtensions)
							.pQueueCreateInfos(queueCreationInfoBuf);

			PointerBuffer pp = stack.mallocPointer(1);
			VulkanUtils.vkCheck(
					VK13.vkCreateDevice(vulkanModulePhysicalDevice.getVkPhysicalDevice(), deviceCreateInfo, null, pp),
					"Failed to create device");
			this.vkDevice = new VkDevice(pp.get(0), vulkanModulePhysicalDevice.getVkPhysicalDevice(), deviceCreateInfo);
		}
	}

	private PointerBuffer createReqExtensions(MemoryStack stack)
	{
		Set<String> deviceExtensions = getDeviceExtensions();
		boolean usePortability =
				deviceExtensions.contains(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME)
						&& VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;

		var extensionList = new ArrayList<ByteBuffer>();
		for (String extension : VulkanModulePhysicalDevice.REQUIRED_EXTENSIONS)
		{
			extensionList.add(stack.ASCII(extension));
		}
		if (usePortability)
		{
			extensionList.add(stack.ASCII(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
		}

		PointerBuffer requiredExtensions = stack.mallocPointer(extensionList.size());
		extensionList.forEach(requiredExtensions::put);
		requiredExtensions.flip();

		return requiredExtensions;
	}

	private Set<String> getDeviceExtensions()
	{
		Set<String> deviceExtensions = new HashSet<>();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer numExtensionsBuf = stack.callocInt(1);
			VK13.vkEnumerateDeviceExtensionProperties(this.vulkanModulePhysicalDevice.getVkPhysicalDevice(),
					(String) null,
					numExtensionsBuf, null);
			int numExtensions = numExtensionsBuf.get(0);
			Logger.debug("Device supports [{}] extensions", numExtensions);

			VkExtensionProperties.Buffer propsBuff = VkExtensionProperties.calloc(numExtensions, stack);
			VK13.vkEnumerateDeviceExtensionProperties(this.vulkanModulePhysicalDevice.getVkPhysicalDevice(),
					(String) null,
					numExtensionsBuf, propsBuff);
			for (int index = 0; index < numExtensions; index++)
			{
				VkExtensionProperties props = propsBuff.get(index);
				String extensionName = props.extensionNameString();
				deviceExtensions.add(extensionName);
				Logger.debug("Supported device extension [{}]", extensionName);
			}
		}
		return deviceExtensions;
	}

	public void cleanup()
	{
		Logger.debug("Destroying Vulkan device");
		VK13.vkDestroyDevice(this.vkDevice, null);
	}

	public VulkanModulePhysicalDevice getPhysicalDevice()
	{
		return this.vulkanModulePhysicalDevice;
	}

	public VkDevice getVkDevice()
	{
		return this.vkDevice;
	}

	public void waitIdle()
	{
		VK13.vkDeviceWaitIdle(this.vkDevice);
	}
}

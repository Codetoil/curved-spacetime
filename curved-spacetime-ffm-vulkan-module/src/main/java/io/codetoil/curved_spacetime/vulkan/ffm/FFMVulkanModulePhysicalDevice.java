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
import vulkan.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class FFMVulkanModulePhysicalDevice
{
	protected static final Set<MemorySegment> REQUIRED_EXTENSIONS;

	static
	{
		REQUIRED_EXTENSIONS = new HashSet<>();
		REQUIRED_EXTENSIONS.add(Vulkan.VK_KHR_SWAPCHAIN_EXTENSION_NAME());
	}

	private final MemorySegment vkDeviceExtensions;
	private final MemorySegment vkMemoryProperties2;
	private final MemorySegment vkPhysicalDevice;
	private final MemorySegment vkPhysicalDeviceFeatures2;
	private final MemorySegment vkPhysicalDeviceProperties2;
	private final MemorySegment vkQueueFamilyProps2;

	private final Logger logger;

	private FFMVulkanModulePhysicalDevice(MemorySegment vkPhysicalDevice, Logger logger)
	{
		this.logger = logger;
		this.vkPhysicalDevice = vkPhysicalDevice;

		// Get device properties
		this.vkPhysicalDeviceProperties2 =
				VkPhysicalDeviceProperties2.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkPhysicalDeviceProperties2.sType(this.vkPhysicalDeviceProperties2,
				Vulkan.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2());
		Vulkan.vkGetPhysicalDeviceProperties2(vkPhysicalDevice, this.vkPhysicalDeviceProperties2);

		// Get device extensions
		MemorySegment numberExtensionsPtr =
				MainModuleEngine.getInstance().nativeAllocator.allocateFrom(ValueLayout.ADDRESS,
						MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT));
		FFMVulkanUtils.vkCheck(
				Vulkan.vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, null, numberExtensionsPtr,
						null),
				"Failed to get number of device extension properties");
		int numberExtensions = numberExtensionsPtr.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		this.vkDeviceExtensions =
				VkExtensionProperties.allocateArray(numberExtensions, MainModuleEngine.getInstance().nativeAllocator);
		FFMVulkanUtils.vkCheck(Vulkan.vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, null,
				numberExtensionsPtr, this.vkDeviceExtensions), "Failed to get extension properties");

		// Get Queue family properties
		MemorySegment numberQueueFamiliesPtr =
				MainModuleEngine.getInstance().nativeAllocator.allocateFrom(ValueLayout.ADDRESS,
						MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT));
		Vulkan.vkGetPhysicalDeviceQueueFamilyProperties2(vkPhysicalDevice, numberQueueFamiliesPtr, null);
		int numberQueueFamilies = numberQueueFamiliesPtr.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		this.vkQueueFamilyProps2 = VkQueueFamilyProperties.allocateArray(numberQueueFamilies,
				MainModuleEngine.getInstance().nativeAllocator);
		Vulkan.vkGetPhysicalDeviceQueueFamilyProperties2(vkPhysicalDevice, numberQueueFamiliesPtr,
				this.vkQueueFamilyProps2);

		this.vkPhysicalDeviceFeatures2 =
				VkPhysicalDeviceFeatures2.allocate(MainModuleEngine.getInstance().nativeAllocator);
		Vulkan.vkGetPhysicalDeviceFeatures2(vkPhysicalDevice, this.vkPhysicalDeviceFeatures2);

		// Get Memory information and properties
		this.vkMemoryProperties2 =
				VkPhysicalDeviceMemoryProperties2.allocate(MainModuleEngine.getInstance().nativeAllocator);
		Vulkan.vkGetPhysicalDeviceMemoryProperties2(vkPhysicalDevice, this.vkMemoryProperties2);
	}

	public static FFMVulkanModulePhysicalDevice createPhysicalDevice(FFMVulkanModuleVulkanInstance instance,
	                                                                 FFMVulkanModuleEntrypoint ffmVulkanModuleEntrypoint,
	                                                                 Logger logger)
	{
		logger.fine(() -> "Selecting physical devices");
		// Get available devices
		MemorySegment pPhysicalDevices = getPhysicalDevices(instance, logger);

		int numPhysicalDevices = Math.toIntExact(pPhysicalDevices.byteSize() / Vulkan.VkPhysicalDevice.byteSize());

		if (numPhysicalDevices <= 0)
		{
			throw new RuntimeException("No physical devices found");
		}

		//Populate available devices
		List<FFMVulkanModulePhysicalDevice> physDevices = new ArrayList<>();
		FFMVulkanModulePhysicalDevice selectedFFMVulkanModulePhysicalDevice = null;

		for (int i = 0; i < numPhysicalDevices; i++)
		{
			MemorySegment vkPhysicalDevice =
					pPhysicalDevices.asSlice(i * Vulkan.VkPhysicalDevice.byteSize(), Vulkan.VkPhysicalDevice);
			var physDevice = new FFMVulkanModulePhysicalDevice(vkPhysicalDevice, logger);

			String deviceName = physDevice.getDeviceName();
			if (!physDevice.hasGraphicsQueueFamily())
			{
				logger.fine(() -> "Device [" + deviceName + "] does not support graphics queue family");
				physDevice.cleanup();
				continue;
			}

			if (!physDevice.supportsExtensions(REQUIRED_EXTENSIONS))
			{
				logger.fine("Device [" + deviceName + "] does not support required extensions");
				physDevice.cleanup();
				continue;
			}

			String preferredDeviceName = ((FFMVulkanModuleConfig) ffmVulkanModuleEntrypoint.getConfig())
					.getPreferredDeviceName();
			if (preferredDeviceName != null && preferredDeviceName.equals(deviceName))
			{
				selectedFFMVulkanModulePhysicalDevice = physDevice;
				break;
			}
			if (VkPhysicalDeviceProperties.deviceType(
					VkPhysicalDeviceProperties2.properties(physDevice.vkPhysicalDeviceProperties2))
					== Vulkan.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU())
			{
				physDevices.addFirst(physDevice);
			} else
			{
				physDevices.add(physDevice);
			}
		}

		// No preferred device, or it does not meet requirements, just pick the first one
		if (selectedFFMVulkanModulePhysicalDevice == null && !physDevices.isEmpty())
			selectedFFMVulkanModulePhysicalDevice = physDevices.removeFirst();

		// Clean up non-selected devices
		physDevices.forEach(FFMVulkanModulePhysicalDevice::cleanup);

		if (selectedFFMVulkanModulePhysicalDevice == null)
		{
			throw new RuntimeException("No suitable physical devices found");
		}

		logger.fine("Selected device: [" + selectedFFMVulkanModulePhysicalDevice.getDeviceName() + "]");

		return selectedFFMVulkanModulePhysicalDevice;
	}

	protected static MemorySegment getPhysicalDevices(FFMVulkanModuleVulkanInstance instance, Logger logger)
	{
		MemorySegment pPhysicalDevices;
		// Get number of physical devices
		MemorySegment numberDevicesPtr =
				MainModuleEngine.getInstance().nativeAllocator.allocateFrom(ValueLayout.ADDRESS,
						MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT));
		FFMVulkanUtils.vkCheck(
				Vulkan.vkEnumeratePhysicalDevices(instance.getVkInstance(), numberDevicesPtr, null),
				"Failed to get number of physical devices");
		int numDevices = numberDevicesPtr.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		logger.fine("Detected " + numDevices + " physical device(s)");

		// Populate physical devices list pointer
		pPhysicalDevices = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkPhysicalDevice, numDevices);
		FFMVulkanUtils.vkCheck(Vulkan.vkEnumeratePhysicalDevices(instance.getVkInstance(), numberDevicesPtr,
				pPhysicalDevices), "Failed to get physical devices");
		return pPhysicalDevices;
	}

	public String getDeviceName()
	{
		return VkPhysicalDeviceProperties
				.deviceName(VkPhysicalDeviceProperties2.properties(this.vkPhysicalDeviceProperties2))
				.getString(0);
	}

	private boolean hasGraphicsQueueFamily()
	{
		boolean result = false;
		for (int i = 0; i < this.vkQueueFamilyProps2.byteSize() / VkQueueFamilyProperties2.sizeof(); i++)
		{
			if (result) break;
			MemorySegment queueFamilyProps2 =
					this.vkQueueFamilyProps2.asSlice(i * VkQueueFamilyProperties2.sizeof(),
							VkQueueFamilyProperties2.layout());
			if ((VkQueueFamilyProperties.queueFlags(VkQueueFamilyProperties2.queueFamilyProperties(queueFamilyProps2))
					& Vulkan.VK_QUEUE_GRAPHICS_BIT()) != 0)
			{
				result = true;
			}
		}

		return result;
	}

	public void cleanup()
	{
		this.logger.fine(
				"Destroying physical device [" + getDeviceName() + "]");
		this.vkMemoryProperties2.unload();
		this.vkPhysicalDeviceFeatures2.unload();
		this.vkQueueFamilyProps2.unload();
		this.vkDeviceExtensions.unload();
		this.vkPhysicalDeviceProperties2.unload();
	}

	public boolean supportsExtensions(Set<MemorySegment> extensions)
	{
		var copyExtensions = new HashSet<>(extensions);
		vkDeviceExtensions.spliterator(VkExtensionProperties.layout()).forEachRemaining((extProps) ->
				copyExtensions.remove(VkExtensionProperties.extensionName(extProps)));

		boolean result = copyExtensions.isEmpty();
		if (!result)
		{
			logger.fine("At least [" + copyExtensions.iterator().next() +
					"] extension is not supported by device [" + getDeviceName() + "]");
		}
		return result;
	}

	public MemorySegment getVkMemoryProperties2()
	{
		return this.vkMemoryProperties2;
	}

	public MemorySegment getVkPhysicalDevice()
	{
		return this.vkPhysicalDevice;
	}

	public MemorySegment getVkPhysicalDeviceFeatures2()
	{
		return this.vkPhysicalDeviceFeatures2;
	}

	public MemorySegment getVkPhysicalDeviceProperties2()
	{
		return this.vkPhysicalDeviceProperties2;
	}

	public MemorySegment getVkQueueFamilyProps2()
	{
		return this.vkQueueFamilyProps2;
	}
}

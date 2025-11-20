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

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VulkanPhysicalDevice
{
	protected static final Set<String> REQUIRED_EXTENSIONS;

	static
	{
		REQUIRED_EXTENSIONS = new HashSet<>();
		REQUIRED_EXTENSIONS.add(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME);
	}

	private final VkExtensionProperties.Buffer vkDeviceExtensions;
	private final VkPhysicalDeviceMemoryProperties vkMemoryProperties;
	private final VkPhysicalDevice vkPhysicalDevice;
	private final VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures;
	private final VkPhysicalDeviceProperties2 vkPhysicalDeviceProperties;
	private final VkQueueFamilyProperties.Buffer vkQueueFamilyProps;

	private VulkanPhysicalDevice(VkPhysicalDevice vkPhysicalDevice)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.vkPhysicalDevice = vkPhysicalDevice;

			IntBuffer intBuffer = stack.mallocInt(1);

			// Get device properties
			this.vkPhysicalDeviceProperties = VkPhysicalDeviceProperties2.calloc().sType$Default();
			VK13.vkGetPhysicalDeviceProperties2(vkPhysicalDevice, this.vkPhysicalDeviceProperties);

			// Get device extensions
			VulkanUtils.vkCheck(
					VK13.vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String) null, intBuffer, null),
					"Failed to get number of device extension properties");
			this.vkDeviceExtensions = VkExtensionProperties.calloc(intBuffer.get(0));
			VulkanUtils.vkCheck(VK13.vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String) null, intBuffer,
					this.vkDeviceExtensions), "Failed to get extension properties");

			// Get Queue family properties
			VK13.vkGetPhysicalDeviceQueueFamilyProperties(vkPhysicalDevice, intBuffer, null);
			this.vkQueueFamilyProps = VkQueueFamilyProperties.calloc(intBuffer.get(0));
			VK13.vkGetPhysicalDeviceQueueFamilyProperties(vkPhysicalDevice, intBuffer, this.vkQueueFamilyProps);

			this.vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
			VK13.vkGetPhysicalDeviceFeatures(vkPhysicalDevice, this.vkPhysicalDeviceFeatures);

			// Get Memory information and properties
			this.vkMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
			VK13.vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, this.vkMemoryProperties);
		}
	}

	public static VulkanPhysicalDevice createPhysicalDevice(VulkanInstance instance,
															VulkanModuleEntrypoint vulkanModuleEntrypoint)
	{
		Logger.debug("Selecting physical devices");
		VulkanPhysicalDevice selectedVulkanPhysicalDevice = null;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			// Get available devices
			PointerBuffer pPhysicalDevices = getPhysicalDevices(instance, stack);
			int numDevices = pPhysicalDevices.capacity();
			if (numDevices <= 0)
			{
				throw new RuntimeException("No physical devices found");
			}

			//Populate available devices
			List<VulkanPhysicalDevice> physDevices = new ArrayList<>();
			for (int i = 0; i < numDevices; i++)
			{
				var vkPhysicalDevice = new VkPhysicalDevice(pPhysicalDevices.get(i), instance.getVkInstance());
				var physDevice = new VulkanPhysicalDevice(vkPhysicalDevice);

				String deviceName = physDevice.getDeviceName();
				if (!physDevice.hasGraphicsQueueFamily())
				{
					Logger.debug("Device [{}] does not support graphics queue family", deviceName);
					physDevice.cleanup();
					continue;
				}

				if (!physDevice.supportsExtensions(REQUIRED_EXTENSIONS))
				{
					Logger.debug("Device [{}] does not support required extensions", deviceName);
					physDevice.cleanup();
					continue;
				}

				String preferredDeviceName = ((VulkanModuleConfig) vulkanModuleEntrypoint.getConfig())
						.getPreferredDeviceName();
				if (preferredDeviceName != null && preferredDeviceName.equals(deviceName))
				{
					selectedVulkanPhysicalDevice = physDevice;
					break;
				}
				if (physDevice.vkPhysicalDeviceProperties.properties().deviceType()
						== VK13.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU)
				{
					physDevices.addFirst(physDevice);
				} else
				{
					physDevices.add(physDevice);
				}
			}
			// No preferred device, or it does not meet requirements, just pick the first one
			selectedVulkanPhysicalDevice =
					selectedVulkanPhysicalDevice == null &&
							!physDevices.isEmpty() ? physDevices.removeFirst() : selectedVulkanPhysicalDevice;

			// Clean up non-selected devices
			physDevices.forEach(VulkanPhysicalDevice::cleanup);

			if (selectedVulkanPhysicalDevice == null)
			{
				throw new RuntimeException("No suitable physical devices found");
			}

			Logger.debug("Selected device: [{}]", selectedVulkanPhysicalDevice.getDeviceName());

			return selectedVulkanPhysicalDevice;
		}
	}

	protected static PointerBuffer getPhysicalDevices(VulkanInstance instance, MemoryStack stack)
	{
		PointerBuffer pPhysicalDevices;
		// Get number of physical devices
		IntBuffer intBuffer = stack.mallocInt(1);
		VulkanUtils.vkCheck(VK13.vkEnumeratePhysicalDevices(instance.getVkInstance(), intBuffer, null),
				"Failed to get number of physical devices");
		int numDevices = intBuffer.get(0);
		Logger.debug("Detected {} physical device(s)", numDevices);

		// Populate physical devices list pointer
		pPhysicalDevices = stack.mallocPointer(numDevices);
		VulkanUtils.vkCheck(VK13.vkEnumeratePhysicalDevices(instance.getVkInstance(), intBuffer, pPhysicalDevices),
				"Failed to get physical devices");
		return pPhysicalDevices;
	}

	public String getDeviceName()
	{
		return this.vkPhysicalDeviceProperties.properties().deviceNameString();
	}

	private boolean hasGraphicsQueueFamily()
	{
		boolean result = false;
		int numQueueFamilies = this.vkQueueFamilyProps != null ? this.vkQueueFamilyProps.capacity() : 0;
		for (int i = 0; i < numQueueFamilies; i++)
		{
			VkQueueFamilyProperties familyProps = this.vkQueueFamilyProps.get(i);
			if ((familyProps.queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0)
			{
				result = true;
				break;
			}
		}
		return result;
	}

	public void cleanup()
	{
		Logger.debug("Destroying physical device [{}]",
				this.vkPhysicalDeviceProperties.properties().deviceNameString());
		this.vkMemoryProperties.free();
		this.vkPhysicalDeviceFeatures.free();
		this.vkQueueFamilyProps.free();
		this.vkDeviceExtensions.free();
		this.vkPhysicalDeviceProperties.free();
	}

	public boolean supportsExtensions(Set<String> extensions)
	{
		var copyExtensions = new HashSet<>(extensions);
		int numExtensions = vkDeviceExtensions != null ? vkDeviceExtensions.capacity() : 0;
		for (int i = 0; i < numExtensions; i++)
		{
			String extensionName = vkDeviceExtensions.get(i).extensionNameString();
			copyExtensions.remove(extensionName);
		}

		boolean result = copyExtensions.isEmpty();
		if (!result)
		{
			Logger.debug("At least [{}] extension is not supported by device [{}]",
					copyExtensions.iterator().next(),
					getDeviceName());
		}
		return result;
	}

	public VkPhysicalDeviceMemoryProperties getVkMemoryProperties()
	{
		return this.vkMemoryProperties;
	}

	public VkPhysicalDevice getVkPhysicalDevice()
	{
		return this.vkPhysicalDevice;
	}

	public VkPhysicalDeviceFeatures getVkPhysicalDeviceFeatures()
	{
		return this.vkPhysicalDeviceFeatures;
	}

	public VkPhysicalDeviceProperties2 getVkPhysicalDeviceProperties()
	{
		return this.vkPhysicalDeviceProperties;
	}

	public VkQueueFamilyProperties.Buffer getVkQueueFamilyProps()
	{
		return this.vkQueueFamilyProps;
	}
}

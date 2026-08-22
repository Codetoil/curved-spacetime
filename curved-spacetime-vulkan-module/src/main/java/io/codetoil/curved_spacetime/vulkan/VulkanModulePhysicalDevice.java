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
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A Vulkan physical device — a GPU the driver has reported — together with everything queried
 * about it.
 * <p>
 * Instances are created by {@link #createPhysicalDevice}, which enumerates every device, discards
 * those that cannot present graphics or lack a required extension, and picks one. Discrete GPUs
 * are preferred over integrated ones, and a device named in configuration wins outright.
 * <p>
 * The queried structures are allocated off-heap and are owned by this object, so every device
 * that is enumerated must eventually be passed to {@link #cleanup()} — including the ones that
 * lose the selection.
 */
public class VulkanModulePhysicalDevice
{
	/**
	 * Extensions a device must support to be considered usable, currently just the swap chain.
	 */
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

	private final Logger logger;

	private VulkanModulePhysicalDevice(VkPhysicalDevice vkPhysicalDevice, Logger logger)
	{
		this.logger = logger;
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

	/**
	 * Enumerates the available devices and selects one to render with.
	 * <p>
	 * A device named by {@code preferredDeviceName} in configuration is taken if it is present and
	 * otherwise suitable; failing that, a discrete GPU is preferred over an integrated one. Every
	 * device not selected is cleaned up before returning.
	 *
	 * @param instance               the instance to enumerate devices from
	 * @param vulkanModuleEntrypoint the entrypoint supplying the preferred device name
	 * @param logger                 the logger to write selection diagnostics to
	 * @return the selected physical device, owned by the caller
	 * @throws RuntimeException if no device is present, or none meets the requirements
	 */
	public static VulkanModulePhysicalDevice createPhysicalDevice(VulkanModuleVulkanInstance instance,
																  VulkanModuleEntrypoint vulkanModuleEntrypoint,
																  Logger logger)
	{
		logger.fine(() -> "Selecting physical devices");
		VulkanModulePhysicalDevice selectedVulkanModulePhysicalDevice = null;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			// Get available devices
			PointerBuffer pPhysicalDevices = getPhysicalDevices(instance, stack, logger);
			int numDevices = pPhysicalDevices.capacity();
			if (numDevices <= 0)
			{
				throw new RuntimeException("No physical devices found");
			}

			//Populate available devices
			List<VulkanModulePhysicalDevice> physDevices = new ArrayList<>();
			for (int i = 0; i < numDevices; i++)
			{
				var vkPhysicalDevice = new VkPhysicalDevice(pPhysicalDevices.get(i), instance.getVkInstance());
				var physDevice = new VulkanModulePhysicalDevice(vkPhysicalDevice, logger);

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

				String preferredDeviceName = ((VulkanModuleConfig) vulkanModuleEntrypoint.getConfig())
						.getPreferredDeviceName();
				if (preferredDeviceName != null && preferredDeviceName.equals(deviceName))
				{
					selectedVulkanModulePhysicalDevice = physDevice;
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
			selectedVulkanModulePhysicalDevice =
					selectedVulkanModulePhysicalDevice == null &&
							!physDevices.isEmpty() ? physDevices.removeFirst() : selectedVulkanModulePhysicalDevice;

			// Clean up non-selected devices
			physDevices.forEach(VulkanModulePhysicalDevice::cleanup);

			if (selectedVulkanModulePhysicalDevice == null)
			{
				throw new RuntimeException("No suitable physical devices found");
			}

			logger.fine("Selected device: [" + selectedVulkanModulePhysicalDevice.getDeviceName() + "]");

			return selectedVulkanModulePhysicalDevice;
		}
	}

	/**
	 * Enumerates the handles of every physical device the instance can see.
	 *
	 * @param instance the instance to enumerate from
	 * @param stack    the stack to allocate the result buffer on
	 * @param logger   the logger to write the device count to
	 * @return a buffer of {@code VkPhysicalDevice} handles, valid for the stack frame
	 * @throws AssertionError if enumeration fails
	 */
	protected static PointerBuffer getPhysicalDevices(VulkanModuleVulkanInstance instance, MemoryStack stack,
													  Logger logger)
	{
		PointerBuffer pPhysicalDevices;
		// Get number of physical devices
		IntBuffer intBuffer = stack.mallocInt(1);
		VulkanUtils.vkCheck(VK13.vkEnumeratePhysicalDevices(instance.getVkInstance(), intBuffer, null),
				"Failed to get number of physical devices");
		int numDevices = intBuffer.get(0);
		logger.fine("Detected " + numDevices + " physical device(s)");

		// Populate physical devices list pointer
		pPhysicalDevices = stack.mallocPointer(numDevices);
		VulkanUtils.vkCheck(VK13.vkEnumeratePhysicalDevices(instance.getVkInstance(), intBuffer, pPhysicalDevices),
				"Failed to get physical devices");
		return pPhysicalDevices;
	}

	/**
	 * Returns the device's reported name.
	 *
	 * @return the device name as the driver reports it
	 */
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

	/**
	 * Frees the off-heap structures queried about this device.
	 * <p>
	 * Must be called for every enumerated device, including those rejected during selection.
	 */
	public void cleanup()
	{
		this.logger.fine(
				"Destroying physical device [" + this.vkPhysicalDeviceProperties.properties().deviceNameString() + "]");
		this.vkMemoryProperties.free();
		this.vkPhysicalDeviceFeatures.free();
		this.vkQueueFamilyProps.free();
		this.vkDeviceExtensions.free();
		this.vkPhysicalDeviceProperties.free();
	}

	/**
	 * Returns whether this device supports every one of the given extensions.
	 *
	 * @param extensions the extension names required
	 * @return {@code true} if all of them are supported
	 */
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
			logger.fine("At least [" + copyExtensions.iterator().next() +
					"] extension is not supported by device [" + getDeviceName() + "]");
		}
		return result;
	}

	/**
	 * Returns the device's memory heaps and types.
	 *
	 * @return the memory properties, owned by this object
	 */
	public VkPhysicalDeviceMemoryProperties getVkMemoryProperties()
	{
		return this.vkMemoryProperties;
	}

	/**
	 * Returns the underlying Vulkan physical device.
	 *
	 * @return the {@code VkPhysicalDevice}
	 */
	public VkPhysicalDevice getVkPhysicalDevice()
	{
		return this.vkPhysicalDevice;
	}

	/**
	 * Returns the optional features this device supports.
	 *
	 * @return the device features, owned by this object
	 */
	public VkPhysicalDeviceFeatures getVkPhysicalDeviceFeatures()
	{
		return this.vkPhysicalDeviceFeatures;
	}

	/**
	 * Returns the device's properties, including its name, type, and limits.
	 *
	 * @return the device properties, owned by this object
	 */
	public VkPhysicalDeviceProperties2 getVkPhysicalDeviceProperties()
	{
		return this.vkPhysicalDeviceProperties;
	}

	/**
	 * Returns the queue families this device offers.
	 * <p>
	 * Callers scan this to find a family with the capability they need, such as graphics or
	 * presentation.
	 *
	 * @return the queue family properties, owned by this object
	 */
	public VkQueueFamilyProperties.Buffer getVkQueueFamilyProps()
	{
		return this.vkQueueFamilyProps;
	}
}

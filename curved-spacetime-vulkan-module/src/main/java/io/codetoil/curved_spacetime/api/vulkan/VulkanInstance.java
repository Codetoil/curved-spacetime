/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
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

package io.codetoil.curved_spacetime.api.vulkan;

import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleConfig;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class VulkanInstance
{
	public static final int MESSAGE_SEVERITY_BITMASK = EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT |
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
	public static final int MESSAGE_TYPE_BITMASK = EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

	public final VulkanModuleConfig vulkanModuleConfig;
	protected final VulkanPhysicalDevice vulkanPhysicalDevice;
	protected final VulkanLogicalDevice vulkanLogicalDevice;
	private final VkInstance vkInstance;
	private VkDebugUtilsMessengerCreateInfoEXT debugUtils;
	private long vkDebugHandle;

	public VulkanInstance(Supplier<PointerBuffer> windowExtensionsGetter)
	{
		try
		{
			this.vulkanModuleConfig = new VulkanModuleConfig().load();
			if (this.vulkanModuleConfig.isDirty()) this.vulkanModuleConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Vulkan Config", ex);
		}

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			ByteBuffer appShortName = stack.UTF8("CurvedSpacetime");
			VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO)
					.pApplicationName(appShortName).applicationVersion(0).pEngineName(appShortName).engineVersion(0)
					.apiVersion(VK10.VK_API_VERSION_1_0);

			// Validation layers
			boolean supportsValidation = this.vulkanModuleConfig.validation();
			List<String> validationLayers = List.of();
			int numValidationLayers = 0;

			if (this.vulkanModuleConfig.validation())
			{
				validationLayers = getSupportedValidationLayers();
				numValidationLayers = validationLayers.size();
				if (numValidationLayers == 0)
				{
					supportsValidation = false;
					Logger.warn("Request validation but no supported validation layers found. " +
							"Falling back to no validation");
				}
			}
			Logger.debug("Validation: {}", supportsValidation);

			// Set required layers
			PointerBuffer requiredLayers = null;
			if (supportsValidation)
			{
				requiredLayers = stack.mallocPointer(numValidationLayers);
				for (int i = 0; i < numValidationLayers; i++)
				{
					Logger.debug("Using validation layer [{}]", validationLayers.get(i));
					requiredLayers.put(i, stack.ASCII(validationLayers.get(i)));
				}
			}

			Set<String> instanceExtensions = getInstanceExtensions();

			PointerBuffer windowExtensions = windowExtensionsGetter.get();
			if (windowExtensions == null)
			{
				throw new RuntimeException("Failed to find the Window extensions");
			}

			PointerBuffer requiredExtensions;

			boolean usePortability = instanceExtensions.contains(
					KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME) &&
					VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;
			if (supportsValidation)
			{
				ByteBuffer vkDebugUtilsExtension = stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
				int numExtensions =
						usePortability ? windowExtensions.remaining() + 2 : windowExtensions.remaining() + 1;
				requiredExtensions = stack.mallocPointer(numExtensions);
				requiredExtensions.put(windowExtensions).put(vkDebugUtilsExtension);
				if (usePortability)
				{
					requiredExtensions.put(
							stack.UTF8(KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME));
				}
			} else
			{
				int numExtensions = usePortability ? windowExtensions.remaining() + 1 : windowExtensions.remaining();
				requiredExtensions = stack.mallocPointer(numExtensions);
				requiredExtensions.put(windowExtensions);
				if (usePortability)
				{
					requiredExtensions.put(stack.UTF8(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
				}
			}
			requiredExtensions.flip();

			long extension = MemoryUtil.NULL;
			if (supportsValidation)
			{
				this.debugUtils = createDebugCallback();
				extension = this.debugUtils.address();
			}

			// Create instance info
			VkInstanceCreateInfo instanceInfo =
					VkInstanceCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
							.pNext(extension).pApplicationInfo(appInfo).ppEnabledLayerNames(requiredLayers)
							.ppEnabledExtensionNames(requiredExtensions);
			if (usePortability)
			{
				instanceInfo.flags(KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
			}

			PointerBuffer pInstance = stack.mallocPointer(1);
			VulkanUtils.vkCheck(VK10.vkCreateInstance(instanceInfo, null, pInstance), "Error creating instance");
			this.vkInstance = new VkInstance(pInstance.get(0), instanceInfo);
			this.vkDebugHandle = VK10.VK_NULL_HANDLE;
			if (supportsValidation)
			{
				LongBuffer longBuff = stack.mallocLong(1);
				VulkanUtils.vkCheck(
						EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(this.vkInstance, this.debugUtils, null, longBuff),
						"Error creating debug utils");
				this.vkDebugHandle = longBuff.get(0);
			}
		}

		this.vulkanPhysicalDevice =
				VulkanPhysicalDevice.createPhysicalDevice(this, this.vulkanModuleConfig.getPreferredDeviceName());
		this.vulkanLogicalDevice = new VulkanLogicalDevice(this.vulkanPhysicalDevice);
	}

	private List<String> getSupportedValidationLayers()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			// Validation Layers
			IntBuffer numLayersArray = stack.callocInt(1);
			VK10.vkEnumerateInstanceLayerProperties(numLayersArray, null);
			int numLayers = numLayersArray.get(0);
			Logger.debug("Instance supports [{}] layers", numLayers);
			VkLayerProperties.Buffer propsBuffer = VkLayerProperties.calloc(numLayers, stack);
			VK10.vkEnumerateInstanceLayerProperties(numLayersArray, propsBuffer);
			List<String> supportedLayers = new ArrayList<>();
			for (int index = 0; index < numLayers; index++)
			{
				VkLayerProperties props = propsBuffer.get(index);
				String layerName = props.layerNameString();
				supportedLayers.add(layerName);
				Logger.debug("Supported Layer [{}]", layerName);
			}
			List<String> layersToUse = new ArrayList<>();

			// Main validation layer
			if (supportedLayers.contains("VK_LAYER_KHRONOS_validation"))
			{
				layersToUse.add("VK_LAYER_KHRONOS_validation");
				return layersToUse;
			}

			// Fallback 1
			if (supportedLayers.contains("VK_LAYER_LUNARG_standard_validation"))
			{
				layersToUse.add("VK_LAYER_LUNARG_standard_validation");
				return layersToUse;
			}

			// Fallback 2 (set)
			List<String> requestedLayers = new ArrayList<>();
			requestedLayers.add("VK_LAYER_GOOGLE_threading");
			requestedLayers.add("VK_LAYER_LUNARG_parameter_validation");
			requestedLayers.add("VK_LAYER_LUNARG_object_tracker");
			requestedLayers.add("VK_LAYER_LUNARG_core_validation");
			requestedLayers.add("VK_LAYER_GOOGLE_unique_objects");

			return requestedLayers.stream().filter(supportedLayers::contains).toList();
		}
	}

	private Set<String> getInstanceExtensions()
	{
		Set<String> instanceExtensions = new HashSet<>();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer numExtensionsBuf = stack.callocInt(1);
			VK10.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, null);
			int numExtensions = numExtensionsBuf.get(0);
			Logger.debug("Instance supports [{}] extensions", numExtensions);

			VkExtensionProperties.Buffer instanceExtensionProps = VkExtensionProperties.calloc(numExtensions, stack);
			VK10.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, instanceExtensionProps);
			for (int index = 0; index < numExtensions; index++)
			{
				VkExtensionProperties props = instanceExtensionProps.get(index);
				String extensionName = props.extensionNameString();
				instanceExtensions.add(extensionName);
				Logger.debug("Supported instance extension [{}]", extensionName);
			}
			return instanceExtensions;
		}
	}

	private static VkDebugUtilsMessengerCreateInfoEXT createDebugCallback()
	{
		return VkDebugUtilsMessengerCreateInfoEXT.calloc()
				.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
				.messageSeverity(VulkanInstance.MESSAGE_SEVERITY_BITMASK)
				.messageType(VulkanInstance.MESSAGE_TYPE_BITMASK)
				.pfnUserCallback((messageSeverity, messageTypes, callbackDataAddress, userData) -> {
					try (VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(
							callbackDataAddress))
					{
						if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0)
						{
							Logger.info("VkDebugUtilsCallback, {}", callbackData.pMessageString());
						} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) !=
								0)
						{
							Logger.warn("VkDebugUtilsCallback, {}", callbackData.pMessageString());
						} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0)
						{
							Logger.error("VkDebugUtilsCallback, {}", callbackData.pMessageString());
						} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT) !=
								0)
						{
							Logger.debug("VkDebugUtilsCallback, {}", callbackData.pMessageString());
						}
					}
					return VK10.VK_FALSE;
				});
	}

	public void cleanup()
	{
		this.vulkanLogicalDevice.waitIdle();
		this.vulkanLogicalDevice.cleanup();
		this.vulkanPhysicalDevice.cleanup();
		Logger.debug("Destroying Vulkan Instance");
		if (this.vkDebugHandle != VK10.VK_NULL_HANDLE)
		{
			EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(this.vkInstance, this.vkDebugHandle, null);
		}
		if (this.debugUtils != null)
		{
			this.debugUtils.pfnUserCallback().free();
			this.debugUtils.free();
		}
		VK10.vkDestroyInstance(this.vkInstance, null);
	}

	public VkInstance getVkInstance()
	{
		return this.vkInstance;
	}

	public VulkanLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanLogicalDevice;
	}

	public VulkanPhysicalDevice getVulkanPhysicalDevice()
	{
		return this.vulkanPhysicalDevice;
	}
}

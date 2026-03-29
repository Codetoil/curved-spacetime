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

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import vulkan.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class VulkanModuleVulkanInstance
{
	public static final int MESSAGE_SEVERITY_BITMASK = Vulkan.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT() |
			Vulkan.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT();
	public static final int MESSAGE_TYPE_BITMASK = Vulkan.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT() |
			Vulkan.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT() |
			Vulkan.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT();
	public static final String VALIDATION_DEFAULT = "VK_LAYER_KHRONOS_validation";
	public static final String VALIDATION_FALLBACK1 = "VK_LAYER_LUNARG_standard_validation";
	public static final Set<String> VALIDATION_FALLBACK2 = Set.of(
			"VK_LAYER_GOOGLE_threading",
			"VK_LAYER_LUNARG_parameter_validation",
			"VK_LAYER_LUNARG_object_tracker",
			"VK_LAYER_LUNARG_core_validation",
			"VK_LAYER_GOOGLE_unique_objects");
	public static final String PORTABILITY_EXTENSION =
			Vulkan.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME().getString(0);
	public static final String DBG_CALLBACK_PREF = "VkDebugUtilsCallback, ";
	protected final Logger logger;
	protected MemorySegment vkInstance;
	protected MemorySegment debugUtils;
	protected MemorySegment vkDebugHandle;

	public VulkanModuleVulkanInstance(VulkanModuleEntrypoint vulkanModuleEntrypoint, Logger logger)
	{
		this.logger = logger;
		MemorySegment appShortName = MainModuleEngine.getInstance().nativeAllocator.allocateFrom("CurvedSpacetime");
		MemorySegment appInfo = VkApplicationInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkApplicationInfo.sType(appInfo, Vulkan.VK_STRUCTURE_TYPE_APPLICATION_INFO());
		VkApplicationInfo.pApplicationName(appInfo, appShortName);
		VkApplicationInfo.applicationVersion(appInfo, 0);
		VkApplicationInfo.pEngineName(appInfo, appShortName);
		VkApplicationInfo.engineVersion(appInfo, 0);
		VkApplicationInfo.apiVersion(appInfo, Vulkan.VK_API_VERSION_1_3());

		// Validation layers
		boolean validation = ((VulkanModuleConfig) vulkanModuleEntrypoint.getConfig()).validation();
		List<String> validationLayers;
		int numValidationLayers = 0;

		if (validation)
		{
			validationLayers = getSupportedValidationLayers();
			numValidationLayers = validationLayers.size();
			if (numValidationLayers == 0)
			{
				validation = false;
				this.logger.warning("Request validation but no supported validation layers found. " +
						"Falling back to no validation");
			}
		} else
		{
			validationLayers = List.of();
		}
		this.logger.fine("Validation: " + validation);

		// Set required layers
		MemorySegment requiredLayers = null;
		if (validation)
		{
			requiredLayers =
					MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.ADDRESS, numValidationLayers);
			for (int i = 0; i < numValidationLayers; i++)
			{
				this.logger.fine("Using validation layer [" + validationLayers.get(i) + "]");
				requiredLayers.setAtIndex(ValueLayout.ADDRESS, i,
						MainModuleEngine.getInstance().nativeAllocator.allocateFrom(validationLayers.get(i)));
			}
		}

		Set<String> instanceExtensions = getInstanceExtensions();

		boolean usePortability = instanceExtensions.contains(PORTABILITY_EXTENSION) &&
				VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;

		List<MemorySegment> additionalExtensions = new ArrayList<>();
		if (validation)
		{

			additionalExtensions.add(Vulkan.VK_EXT_DEBUG_UTILS_EXTENSION_NAME());
		}
		if (usePortability)
		{
			additionalExtensions.add(
					MainModuleEngine.getInstance().nativeAllocator.allocateFrom(PORTABILITY_EXTENSION));
		}
		int numAdditionalExtensions = additionalExtensions.size();

		MemorySegment requiredExtensions =
				MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.ADDRESS, numAdditionalExtensions);
		for (int i = 0; i < numAdditionalExtensions; i++)
		{
			requiredExtensions.setAtIndex(ValueLayout.ADDRESS, i, additionalExtensions.get(i));
		}
		VulkanUtils.reverseBytes(requiredExtensions);

		MemorySegment extension = null;
		if (validation)
		{
			this.debugUtils = createDebugCallback(this.logger);
			extension = this.debugUtils;
		}

		// Create instance info
		assert requiredLayers != null;
		MemorySegment instanceInfo = VkInstanceCreateInfo.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkInstanceCreateInfo.sType(instanceInfo, Vulkan.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO());
		VkInstanceCreateInfo.pNext(instanceInfo, extension);
		VkInstanceCreateInfo.pApplicationInfo(instanceInfo, appInfo);
		VkInstanceCreateInfo.ppEnabledLayerNames(instanceInfo, requiredLayers);
		VkInstanceCreateInfo.ppEnabledExtensionNames(instanceInfo, requiredExtensions);
		if (usePortability)
		{
			VkInstanceCreateInfo.flags(MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT,
					Vulkan.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR()));
		}

		MemorySegment pInstance = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkInstance);
		VulkanUtils.vkCheck(Vulkan.vkCreateInstance(instanceInfo, null, pInstance),
				"Error creating instance");
		this.vkDebugHandle = Vulkan.VK_NULL_HANDLE();
		if (validation)
		{
			this.vkDebugHandle =
					MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkDebugUtilsMessengerEXT);
			VulkanUtils.vkCheck(
					Vulkan.vkCreateDebugUtilsMessengerEXT(this.vkInstance, this.debugUtils, null,
							this.vkDebugHandle),
					"Error creating debug utils");
		}
	}

	private List<String> getSupportedValidationLayers()
	{
		// Validation Layers
		MemorySegment numLayersSegment =
				MainModuleEngine.getInstance().nativeAllocator.allocateFrom(ValueLayout.ADDRESS,
						MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT));
		Vulkan.vkEnumerateInstanceLayerProperties(numLayersSegment, null);
		int numLayers = numLayersSegment.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		this.logger.fine("Instance supports [" + numLayers + "] layers");
		MemorySegment propsArray =
				VkLayerProperties.allocateArray(numLayers, MainModuleEngine.getInstance().nativeAllocator);
		Vulkan.vkEnumerateInstanceLayerProperties(numLayersSegment, propsArray);
		List<String> supportedLayers = new ArrayList<>();
		for (int i = 0; i < numLayers; i++)
		{
			MemorySegment layer = propsArray.asSlice(i * VkLayerProperties.sizeof(), VkLayerProperties.layout());
			String layerName = VkLayerProperties.layerName(layer).getString(0);
			supportedLayers.add(layerName);
			this.logger.fine("Supported Layer [" + layerName + "]");
		}
		List<String> layersToUse = new ArrayList<>();

		// Main validation layer
		if (supportedLayers.contains(VALIDATION_DEFAULT))
		{
			layersToUse.add(VALIDATION_DEFAULT);
			return layersToUse;
		}

		// Fallback 1
		if (supportedLayers.contains(VALIDATION_FALLBACK1))
		{
			layersToUse.add(VALIDATION_FALLBACK1);
			return layersToUse;
		}

		// Fallback 2 (set)
		List<String> requestedLayers = new ArrayList<>(VALIDATION_FALLBACK2);

		return requestedLayers.stream().filter(supportedLayers::contains).toList();
	}

	private Set<String> getInstanceExtensions()
	{
		Set<String> instanceExtensions = new HashSet<>();
		MemorySegment numExtensionsPointer =
				MainModuleEngine.getInstance().nativeAllocator.allocateFrom(ValueLayout.ADDRESS,
						MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT));
		Vulkan.vkEnumerateInstanceExtensionProperties(null, numExtensionsPointer, null);
		int numExtensions = numExtensionsPointer.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		this.logger.fine("Instance supports [" + numExtensions + "] extensions");

		MemorySegment instanceExtensionProps =
				VkExtensionProperties.allocateArray(numExtensions, MainModuleEngine.getInstance().nativeAllocator);
		Vulkan.vkEnumerateInstanceExtensionProperties(null, numExtensionsPointer, instanceExtensionProps);
		for (int i = 0; i < numExtensions; i++)
		{
			MemorySegment prop = instanceExtensionProps.asSlice(i * VkExtensionProperties.sizeof(),
					VkExtensionProperties.layout());
			String extensionName = VkExtensionProperties.extensionName(prop).getString(0);
			instanceExtensions.add(extensionName);
			this.logger.fine("Supported instance extension [" + extensionName + "]");
		}
		return instanceExtensions;
	}

	private static MemorySegment createDebugCallback(Logger logger)
	{
		MemorySegment result =
				VkDebugUtilsMessengerCreateInfoEXT.allocate(MainModuleEngine.getInstance().nativeAllocator);
		VkDebugUtilsMessengerCreateInfoEXT.sType(result,
				Vulkan.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT());
		VkDebugUtilsMessengerCreateInfoEXT.messageSeverity(result, VulkanModuleVulkanInstance.MESSAGE_SEVERITY_BITMASK);
		VkDebugUtilsMessengerCreateInfoEXT.messageType(result, VulkanModuleVulkanInstance.MESSAGE_TYPE_BITMASK);
		VkDebugUtilsMessengerCreateInfoEXT.pfnUserCallback(result, PFN_vkDebugUtilsMessengerCallbackEXT.allocate(
				(messageSeverity, _, callbackDataAddress, _) -> {
					String callbackDataMessageString =
							VkDebugUtilsMessengerCallbackDataEXT.pMessage(callbackDataAddress).getString(0);
					if ((messageSeverity & Vulkan.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT()) != 0)
					{
						logger.info(DBG_CALLBACK_PREF + callbackDataMessageString);
					} else if ((messageSeverity & Vulkan.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT()) !=
							0)
					{
						logger.warning(DBG_CALLBACK_PREF + callbackDataMessageString);
					} else if ((messageSeverity & Vulkan.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT()) != 0)
					{
						logger.severe(DBG_CALLBACK_PREF + callbackDataMessageString);
					} else if ((messageSeverity & Vulkan.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT()) !=
							0)
					{
						logger.fine(DBG_CALLBACK_PREF + callbackDataMessageString);
					}
					return Vulkan.VK_FALSE();
				}, MainModuleEngine.getInstance().nativeAllocator));
		return result;
	}

	public void cleanup()
	{
		this.logger.fine("Destroying Vulkan Instance");
		if (this.vkDebugHandle != Vulkan.VK_NULL_HANDLE())
		{
			Vulkan.vkDestroyDebugUtilsMessengerEXT(this.vkInstance, this.vkDebugHandle, null);
		}
		if (this.debugUtils != null)
		{
			VkDebugUtilsMessengerCreateInfoEXT.pfnUserCallback(this.debugUtils).unload();
			this.debugUtils.unload();
		}
		Vulkan.vkDestroyInstance(this.vkInstance, null);
	}

	public MemorySegment getVkInstance()
	{
		return this.vkInstance;
	}
}

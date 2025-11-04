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
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

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
	public static final String VALIDATION_DEFAULT = "VK_LAYER_KHRONOS_validation";
	public static final String VALIDATION_FALLBACK1 = "VK_LAYER_LUNARG_standard_validation";
	public static final Set<String> VALIDATION_FALLBACK2 = Set.of(
			"VK_LAYER_GOOGLE_threading",
			"VK_LAYER_LUNARG_parameter_validation",
			"VK_LAYER_LUNARG_object_tracker",
			"VK_LAYER_LUNARG_core_validation",
			"VK_LAYER_GOOGLE_unique_objects");
	public static final String PORTABILITY_EXTENSION =
			KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME;
	public static final String DBG_CALLBACK_PREF = "VkDebugUtilsCallback, {}";

	protected VkInstance vkInstance;
	protected VkDebugUtilsMessengerCreateInfoEXT debugUtils;
	protected long vkDebugHandle;

	public VulkanInstance(VulkanModuleEntrypoint vulkanModuleEntrypoint, Supplier<PointerBuffer> windowExtensionsGetter)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			ByteBuffer appShortName = stack.UTF8("CurvedSpacetime");
			VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
					.sType$Default()
					.pApplicationName(appShortName)
					.applicationVersion(0)
					.pEngineName(appShortName)
					.engineVersion(0)
					.apiVersion(VK13.VK_API_VERSION_1_3);

			// Validation layers
			boolean validation = ((VulkanModuleConfig) vulkanModuleEntrypoint.getConfig()).validation();
			List<String> validationLayers = List.of();
			int numValidationLayers = 0;

			if (validation)
			{
				validationLayers = getSupportedValidationLayers();
				numValidationLayers = validationLayers.size();
				if (numValidationLayers == 0)
				{
					validation = false;
					Logger.warn("Request validation but no supported validation layers found. " +
							"Falling back to no validation");
				}
			}
			Logger.debug("Validation: {}", validation);

			// Set required layers
			PointerBuffer requiredLayers = null;
			if (validation)
			{
				requiredLayers = stack.mallocPointer(numValidationLayers);
				for (int i = 0; i < numValidationLayers; i++)
				{
					Logger.debug("Using validation layer [{}]", validationLayers.get(i));
					requiredLayers.put(i, stack.ASCII(validationLayers.get(i)));
				}
			}

			Set<String> instanceExtensions = getInstanceExtensions();

			boolean usePortability = instanceExtensions.contains(PORTABILITY_EXTENSION) &&
					VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;

			PointerBuffer windowExtensions = windowExtensionsGetter.get();
			if (windowExtensions == null)
			{
				throw new RuntimeException("Failed to find the Window extensions");
			}

			List<ByteBuffer> additionalExtensions = new ArrayList<>();
			if (validation)
			{
				additionalExtensions.add(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
			}
			if (usePortability)
			{
				additionalExtensions.add(stack.UTF8(PORTABILITY_EXTENSION));
			}
			int numAdditionalExtensions = additionalExtensions.size();

			PointerBuffer requiredExtensions = stack.mallocPointer(windowExtensions.remaining() +
					numAdditionalExtensions);
			requiredExtensions.put(windowExtensions);
			for (int i = 0; i < numAdditionalExtensions; i++)
			{
				requiredExtensions.put(additionalExtensions.get(i));
			}
			requiredExtensions.flip();

			long extension = MemoryUtil.NULL;
			if (validation)
			{
				this.debugUtils = createDebugCallback();
				extension = this.debugUtils.address();
			}

			// Create instance info
			VkInstanceCreateInfo instanceInfo =
					VkInstanceCreateInfo.calloc(stack)
							.sType$Default()
							.pNext(extension)
							.pApplicationInfo(appInfo)
							.ppEnabledLayerNames(requiredLayers)
							.ppEnabledExtensionNames(requiredExtensions);
			if (usePortability)
			{
				instanceInfo.flags(KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
			}

			PointerBuffer pInstance = stack.mallocPointer(1);
			VulkanUtils.vkCheck(VK13.vkCreateInstance(instanceInfo, null, pInstance), "Error creating instance");
			this.vkInstance = new VkInstance(pInstance.get(0), instanceInfo);
			this.vkDebugHandle = VK13.VK_NULL_HANDLE;
			if (validation)
			{
				LongBuffer longBuff = stack.mallocLong(1);
				VulkanUtils.vkCheck(
						EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(this.vkInstance, this.debugUtils, null, longBuff),
						"Error creating debug utils");
				this.vkDebugHandle = longBuff.get(0);
			}
		}
	}

	private List<String> getSupportedValidationLayers()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			// Validation Layers
			IntBuffer numLayersArray = stack.callocInt(1);
			VK13.vkEnumerateInstanceLayerProperties(numLayersArray, null);
			int numLayers = numLayersArray.get(0);
			Logger.debug("Instance supports [{}] layers", numLayers);
			VkLayerProperties.Buffer propsBuffer = VkLayerProperties.calloc(numLayers, stack);
			VK13.vkEnumerateInstanceLayerProperties(numLayersArray, propsBuffer);
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
	}

	private Set<String> getInstanceExtensions()
	{
		Set<String> instanceExtensions = new HashSet<>();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer numExtensionsBuf = stack.callocInt(1);
			VK13.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, null);
			int numExtensions = numExtensionsBuf.get(0);
			Logger.debug("Instance supports [{}] extensions", numExtensions);

			VkExtensionProperties.Buffer instanceExtensionProps = VkExtensionProperties.calloc(numExtensions, stack);
			VK13.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, instanceExtensionProps);
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
				.sType$Default()
				.messageSeverity(VulkanInstance.MESSAGE_SEVERITY_BITMASK)
				.messageType(VulkanInstance.MESSAGE_TYPE_BITMASK)
				.pfnUserCallback((messageSeverity, messageTypes, callbackDataAddress, userData) -> {
					try (VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(
							callbackDataAddress))
					{
						if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0)
						{
							Logger.info(DBG_CALLBACK_PREF, callbackData.pMessageString());
						} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) !=
								0)
						{
							Logger.warn(DBG_CALLBACK_PREF, callbackData.pMessageString());
						} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0)
						{
							Logger.error(DBG_CALLBACK_PREF, callbackData.pMessageString());
						} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT) !=
								0)
						{
							Logger.debug(DBG_CALLBACK_PREF, callbackData.pMessageString());
						}
					}
					return VK13.VK_FALSE;
				});
	}

	public void cleanup()
	{
		Logger.debug("Destroying Vulkan Instance");
		if (this.vkDebugHandle != VK13.VK_NULL_HANDLE)
		{
			EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(this.vkInstance, this.vkDebugHandle, null);
		}
		if (this.debugUtils != null)
		{
			this.debugUtils.pfnUserCallback().free();
			this.debugUtils.free();
		}
		VK13.vkDestroyInstance(this.vkInstance, null);
	}

	public VkInstance getVkInstance()
	{
		return this.vkInstance;
	}
}

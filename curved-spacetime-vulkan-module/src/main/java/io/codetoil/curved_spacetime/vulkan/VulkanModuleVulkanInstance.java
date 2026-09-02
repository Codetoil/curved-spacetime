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
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The Vulkan instance: the application's connection to the Vulkan loader, and the object every
 * physical device is enumerated from.
 * <p>
 * When validation is enabled in configuration, this also installs a debug messenger that routes
 * the validation layers' output into the module's logger, so driver complaints appear alongside
 * the program's own diagnostics. Validation degrades gracefully: the preferred Khronos layer is
 * used if present, then a LunarG fallback, then a set of individual Google and LunarG layers, and
 * if none are available validation is switched off with a warning rather than failing.
 */
public class VulkanModuleVulkanInstance
{
	/**
	 * Which severities the debug messenger reports: errors and warnings only.
	 */
	public static final int MESSAGE_SEVERITY_BITMASK = EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT |
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;

	/**
	 * Which message kinds the debug messenger reports: general, validation, and performance.
	 */
	public static final int MESSAGE_TYPE_BITMASK = EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
			EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

	/**
	 * The preferred validation layer.
	 */
	public static final String VALIDATION_DEFAULT = "VK_LAYER_KHRONOS_validation";

	/**
	 * The validation layer to try when {@link #VALIDATION_DEFAULT} is unavailable.
	 */
	public static final String VALIDATION_FALLBACK1 = "VK_LAYER_LUNARG_standard_validation";

	/**
	 * The individual layers to fall back on when neither combined validation layer is available.
	 */
	public static final Set<String> VALIDATION_FALLBACK2 = Set.of(
			"VK_LAYER_GOOGLE_threading",
			"VK_LAYER_LUNARG_parameter_validation",
			"VK_LAYER_LUNARG_object_tracker",
			"VK_LAYER_LUNARG_core_validation",
			"VK_LAYER_GOOGLE_unique_objects");
	/**
	 * The extension that lets non-conformant implementations, such as MoltenVK, be enumerated.
	 */
	public static final String PORTABILITY_EXTENSION =
			KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME;

	/**
	 * The prefix every message forwarded from the validation layers carries.
	 */
	public static final String DBG_CALLBACK_PREF = "VkDebugUtilsCallback, ";

	/**
	 * The logger this instance, and the validation layers, write to.
	 */
	protected final Logger logger;

	/**
	 * The underlying Vulkan instance.
	 */
	protected VkInstance vkInstance;

	/**
	 * The debug messenger's creation info, retained so it can be freed, or {@code null} when
	 * validation is disabled.
	 */
	protected VkDebugUtilsMessengerCreateInfoEXT debugUtils;

	/**
	 * The debug messenger handle, or {@code VK_NULL_HANDLE} when validation is disabled.
	 */
	protected long vkDebugHandle;

	/**
	 * Creates the Vulkan instance, enabling validation if configuration asks for it and the layers
	 * are available.
	 *
	 * @param vulkanModuleEntrypoint the entrypoint supplying the Vulkan module's configuration
	 * @param logger                 the logger to write instance and validation output to
	 * @throws AssertionError if the instance or the debug messenger cannot be created
	 */
	public VulkanModuleVulkanInstance(VulkanModuleEntrypoint vulkanModuleEntrypoint, Logger logger)
	{
		this.logger = logger;
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
			PointerBuffer requiredLayers = null;
			if (validation)
			{
				requiredLayers = stack.mallocPointer(numValidationLayers);
				for (int i = 0; i < numValidationLayers; i++)
				{
					this.logger.fine("Using validation layer [" + validationLayers.get(i) + "]");
					requiredLayers.put(i, stack.ASCII(validationLayers.get(i)));
				}
			}

			Set<String> instanceExtensions = getInstanceExtensions();

			boolean usePortability = instanceExtensions.contains(PORTABILITY_EXTENSION) &&
					VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;

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

			PointerBuffer requiredExtensions = stack.mallocPointer(numAdditionalExtensions);
			for (int i = 0; i < numAdditionalExtensions; i++)
			{
				requiredExtensions.put(additionalExtensions.get(i));
			}
			requiredExtensions.flip();

			long extension = MemoryUtil.NULL;
			if (validation)
			{
				this.debugUtils = createDebugCallback(this.logger);
				extension = this.debugUtils.address();
			}

			// Create instance info
			assert requiredLayers != null;
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
			this.logger.fine("Instance supports [" + numLayers + "] layers");
			VkLayerProperties.Buffer propsBuffer = VkLayerProperties.calloc(numLayers, stack);
			VK13.vkEnumerateInstanceLayerProperties(numLayersArray, propsBuffer);
			List<String> supportedLayers = new ArrayList<>();
			for (int index = 0; index < numLayers; index++)
			{
				VkLayerProperties props = propsBuffer.get(index);
				String layerName = props.layerNameString();
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
	}

	private Set<String> getInstanceExtensions()
	{
		Set<String> instanceExtensions = new HashSet<>();
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer numExtensionsBuf = stack.callocInt(1);
			VK13.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, null);
			int numExtensions = numExtensionsBuf.get(0);
			this.logger.fine("Instance supports [" + numExtensions + "] extensions");

			VkExtensionProperties.Buffer instanceExtensionProps = VkExtensionProperties.calloc(numExtensions, stack);
			VK13.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, instanceExtensionProps);
			for (int index = 0; index < numExtensions; index++)
			{
				VkExtensionProperties props = instanceExtensionProps.get(index);
				String extensionName = props.extensionNameString();
				instanceExtensions.add(extensionName);
				this.logger.fine("Supported instance extension [" + extensionName + "]");
			}
			return instanceExtensions;
		}
	}

	private static VkDebugUtilsMessengerCreateInfoEXT createDebugCallback(Logger logger)
	{
		return VkDebugUtilsMessengerCreateInfoEXT.calloc()
				.sType$Default()
				.messageSeverity(VulkanModuleVulkanInstance.MESSAGE_SEVERITY_BITMASK)
				.messageType(VulkanModuleVulkanInstance.MESSAGE_TYPE_BITMASK)
				.pfnUserCallback((messageSeverity, messageTypes, callbackDataAddress, userData) -> {
					VkDebugUtilsMessengerCallbackDataEXT callbackData =
							VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataAddress);
					if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0)
					{
						logger.info(DBG_CALLBACK_PREF + callbackData.pMessageString());
					} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) !=
							0)
					{
						logger.warning(DBG_CALLBACK_PREF + callbackData.pMessageString());
					} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0)
					{
						logger.severe(DBG_CALLBACK_PREF + callbackData.pMessageString());
					} else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT) !=
							0)
					{
						logger.fine(DBG_CALLBACK_PREF + callbackData.pMessageString());
					}
					return VK13.VK_FALSE;
				});
	}

	/**
	 * Destroys the debug messenger, if any, and then the instance.
	 */
	public void cleanup()
	{
		this.logger.fine("Destroying Vulkan Instance");
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

	/**
	 * Returns the underlying Vulkan instance.
	 *
	 * @return the {@code VkInstance}
	 */
	public VkInstance getVkInstance()
	{
		return this.vkInstance;
	}
}

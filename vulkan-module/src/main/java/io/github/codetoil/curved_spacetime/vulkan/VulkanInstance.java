package io.github.codetoil.curved_spacetime.vulkan;

import io.github.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
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

public class VulkanInstance {
    public static final int MESSAGE_SEVERITY_BITMASK = EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT |
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
    public static final int MESSAGE_TYPE_BITMASK = EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

    private final VkInstance vkInstance;

    private VkDebugUtilsMessengerCreateInfoEXT debugUtils;
    private long vkDebugHandle;

    public VulkanInstance(boolean validate, Supplier<PointerBuffer> windowExtensionsGetter)
    {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer appShortName = stack.UTF8("CurvedSpacetime");
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(appShortName)
                    .applicationVersion(0)
                    .pEngineName(appShortName)
                    .engineVersion(0)
                    .apiVersion(VK13.VK_API_VERSION_1_3);

            // Validation layers
            List<String> validationLayers = getSupportedValidationLayers();
            int numValidationLayers = validationLayers.size();
            boolean supportsValidation = validate;
            if (validate && numValidationLayers == 0) {
                supportsValidation = false;
                Logger.warn("Request validation but no supported validation layers found. " +
                        "Falling back to no validation");
            }
            Logger.debug("Validation: {}", supportsValidation);

            // Set required layers
            PointerBuffer requiredLayers = null;
            if (supportsValidation) {
                requiredLayers = stack.mallocPointer(numValidationLayers);
                for (int i = 0; i < numValidationLayers; i++) {
                    Logger.debug("Using validation layer [{}]", validationLayers.get(i));
                    requiredLayers.put(i, stack.ASCII(validationLayers.get(i)));
                }
            }

            Set<String> instanceExtensions = getInstanceExtensions();

            PointerBuffer windowExtensions = windowExtensionsGetter.get();
            if (windowExtensions == null) {
                throw new RuntimeException("Failed to find the Window extensions");
            }

            PointerBuffer requiredExtensions;

            boolean usePortability = instanceExtensions
                    .contains(KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME) &&
                    VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;
            if (supportsValidation) {
                ByteBuffer vkDebugUtilsExtension = stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
                int numExtensions = usePortability ? windowExtensions.remaining() + 2 : windowExtensions.remaining() + 1;
                requiredExtensions = stack.mallocPointer(numExtensions);
                requiredExtensions.put(windowExtensions).put(vkDebugUtilsExtension);
                if (usePortability) {
                    requiredExtensions.put(stack
                            .UTF8(KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME));
                }
            } else {
                int numExtensions = usePortability ? windowExtensions.remaining() + 1 : windowExtensions.remaining();
                requiredExtensions = stack.mallocPointer(numExtensions);
                requiredExtensions.put(windowExtensions);
                if (usePortability) {
                    requiredExtensions.put(stack.UTF8(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
                }
            }
            requiredExtensions.flip();

            long extension = MemoryUtil.NULL;
            if (supportsValidation) {
                debugUtils = createDebugCallback();
                extension = debugUtils.address();
            }

            // Create instance info
            VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK13.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(extension)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(requiredLayers)
                    .ppEnabledExtensionNames(requiredExtensions);
            if (usePortability) {
                instanceInfo.flags(KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
            }

            PointerBuffer pInstance = stack.mallocPointer(1);
            VulkanUtils.vkCheck(VK13.vkCreateInstance(instanceInfo, null, pInstance),
                    "Error creating instance");
            vkInstance = new VkInstance(pInstance.get(0), instanceInfo);
            vkDebugHandle = VK13.VK_NULL_HANDLE;
            if (supportsValidation) {
                LongBuffer longBuff = stack.mallocLong(1);
                VulkanUtils.vkCheck(EXTDebugUtils
                        .vkCreateDebugUtilsMessengerEXT(vkInstance, debugUtils, null, longBuff),
                        "Error creating debug utils");
                vkDebugHandle = longBuff.get(0);
            }
        }
    }

    private List<String> getSupportedValidationLayers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Validation Layers
            IntBuffer numLayersArray = stack.callocInt(1);
            VK13.vkEnumerateInstanceLayerProperties(numLayersArray, null);
            int numLayers = numLayersArray.get(0);
            Logger.debug("Instance supports [{}] layers", numLayers);
            VkLayerProperties.Buffer propsBuffer = VkLayerProperties.calloc(numLayers, stack);
            VK13.vkEnumerateInstanceLayerProperties(numLayersArray, propsBuffer);
            List<String> supportedLayers = new ArrayList<>();
            for (int index = 0; index < numLayers; index++) {
                VkLayerProperties props = propsBuffer.get(index);
                String layerName = props.layerNameString();
                supportedLayers.add(layerName);
                Logger.debug("Supported Layer [{}]", layerName);
            }
            List<String> layersToUse = new ArrayList<>();

            // Main validation layer
            if (supportedLayers.contains("VK_LAYER_KHRONOS_validation")) {
                layersToUse.add("VK_LAYER_KHRONOS_validation");
                return layersToUse;
            }

            // Fallback 1
            if (supportedLayers.contains("VK_LAYER_LUNARG_standard_validation")) {
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

    private Set<String> getInstanceExtensions() {
        Set<String> instanceExtensions = new HashSet<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtensionsBuf = stack.callocInt(1);
            VK13.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, null);
            int numExtensions = numExtensionsBuf.get(0);
            Logger.debug("Instance supports [{}] extensions", numExtensions);

            VkExtensionProperties.Buffer instanceExtensionProps = VkExtensionProperties.calloc(numExtensions, stack);
            VK13.vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, instanceExtensionProps);
            for (int index = 0; index < numExtensions; index++) {
                VkExtensionProperties props = instanceExtensionProps.get(index);
                String extensionName = props.extensionNameString();
                instanceExtensions.add(extensionName);
                Logger.debug("Supported instance extension [{}]", extensionName);
            }
            return instanceExtensions;
        }
    }

    private static VkDebugUtilsMessengerCreateInfoEXT createDebugCallback() {
        VkDebugUtilsMessengerCreateInfoEXT result = VkDebugUtilsMessengerCreateInfoEXT.calloc()
                .sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(MESSAGE_SEVERITY_BITMASK)
                .messageType(MESSAGE_TYPE_BITMASK)
                .pfnUserCallback((messageSeverity, messageTypes, callbackDataAddress, userData) -> {
                    VkDebugUtilsMessengerCallbackDataEXT callbackData =
                            VkDebugUtilsMessengerCallbackDataEXT.create(callbackDataAddress);
                    if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0) {
                        Logger.info("VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    } else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
                        Logger.warn("VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    } else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
                        Logger.error("VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    } else if ((messageSeverity & EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT) != 0) {
                        Logger.debug("VkDebugUtilsCallback, {}", callbackData.pMessageString());
                    }
                    return VK13.VK_FALSE;
                });
        return result;
    }

    public void cleanup() {
        Logger.debug("Destroying Vulkan Instance");
        if (vkDebugHandle != VK13.VK_NULL_HANDLE) {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(vkInstance, vkDebugHandle, null);
        }
        if (debugUtils != null) {
            debugUtils.pfnUserCallback().free();
            debugUtils.free();
        }
        VK13.vkDestroyInstance(vkInstance, null);
    }

    public VkInstance getVkInstance() {
        return vkInstance;
    }
}

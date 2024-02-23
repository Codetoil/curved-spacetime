/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2024 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2023 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<br>
 */

package io.github.codetoil.curved_spacetime.vulkan;

import io.github.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class VulkanPhysicalDevice {
    private final VkExtensionProperties.Buffer vkDeviceExtensions;
    private final VkPhysicalDeviceMemoryProperties vkMemoryProperties;
    private final VkPhysicalDevice vkPhysicalDevice;
    private final VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures;
    private final VkPhysicalDeviceProperties vkPhysicalDeviceProperties;
    private final VkQueueFamilyProperties.Buffer vkQueueFamilyProps;

    private VulkanPhysicalDevice(VkPhysicalDevice vkPhysicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.vkPhysicalDevice = vkPhysicalDevice;

            IntBuffer intBuffer = stack.mallocInt(1);

            // Get device properties
            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc();
            VK13.vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);

            // Get device extensions
            VulkanUtils.vkCheck(VK13.vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String) null, intBuffer,
                            null), "Failed to get number of device extension properties");
            vkDeviceExtensions = VkExtensionProperties.calloc(intBuffer.get(0));
            VulkanUtils.vkCheck(VK13.vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String) null, intBuffer,
                            vkDeviceExtensions), "Failed to get extension properties");

            // Get Queue family properties
            VK13.vkGetPhysicalDeviceQueueFamilyProperties(vkPhysicalDevice, intBuffer, null);
            vkQueueFamilyProps = VkQueueFamilyProperties.calloc(intBuffer.get(0));
            VK13.vkGetPhysicalDeviceQueueFamilyProperties(vkPhysicalDevice, intBuffer, vkQueueFamilyProps);

            vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc();
            VK13.vkGetPhysicalDeviceFeatures(vkPhysicalDevice, vkPhysicalDeviceFeatures);

            // Get Memory information and properties
            vkMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
            VK13.vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, vkMemoryProperties);
        }
    }

    public static VulkanPhysicalDevice createPhysicalDevice(VulkanInstance instance, String preferredDeviceName) {
        Logger.debug("Selecting physical devices");
        VulkanPhysicalDevice selectedVulkanPhysicalDevice = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Get available devices
            PointerBuffer pPhysicalDevices = getPhysicalDevices(instance, stack);
            int numDevices = pPhysicalDevices.capacity();
            if (numDevices <= 0) {
                throw new RuntimeException("No physical devices found");
            }

            //Populate available devices
            List<VulkanPhysicalDevice> devices = new ArrayList<>();
            for (int index = 0; index < numDevices; index++) {
                VkPhysicalDevice vkPhysicalDevice = new VkPhysicalDevice(pPhysicalDevices.get(index),
                        instance.getVkInstance());
                VulkanPhysicalDevice vulkanPhysicalDevice = new VulkanPhysicalDevice(vkPhysicalDevice);;

                String deviceName = vulkanPhysicalDevice.getDeviceName();
                if (vulkanPhysicalDevice.hasGraphicsQueueFamily() && vulkanPhysicalDevice.hasKHRSwapChainExtension()) {
                    Logger.debug("Device [{}] supports required extensions", deviceName);
                    if (preferredDeviceName != null && preferredDeviceName.equals(deviceName)) {
                        selectedVulkanPhysicalDevice = vulkanPhysicalDevice;
                        break;
                    }
                    devices.add(vulkanPhysicalDevice);
                } else {
                    Logger.debug("Device [{}] does not support required extensions", deviceName);
                    vulkanPhysicalDevice.cleanup();
                }

                // No preferred device or it does not meet requirements, just pick the first one
                selectedVulkanPhysicalDevice = selectedVulkanPhysicalDevice == null && !devices.isEmpty()
                        ? devices.remove(0) : selectedVulkanPhysicalDevice;

                // Clean up non-selected devices
                for (VulkanPhysicalDevice vulkanPhysicalDevice1 : devices) {
                    vulkanPhysicalDevice1.cleanup();
                }

                if (selectedVulkanPhysicalDevice == null) {
                    throw new RuntimeException("No suitable physical devices found");
                }
                Logger.debug("Selected device: [{}]", selectedVulkanPhysicalDevice.getDeviceName());
            }

            return selectedVulkanPhysicalDevice;
        }
    }

    public void cleanup() {
        Logger.debug("Destroying physical device [{}]", vkPhysicalDeviceProperties.deviceNameString());
        vkMemoryProperties.free();
        vkPhysicalDeviceFeatures.free();
        vkQueueFamilyProps.free();
        vkDeviceExtensions.free();
        vkPhysicalDeviceProperties.free();
    }

    protected static PointerBuffer getPhysicalDevices(VulkanInstance instance, MemoryStack stack) {
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
    public String getDeviceName() {
        return vkPhysicalDeviceProperties.deviceNameString();
    }

    public VkPhysicalDeviceMemoryProperties getVkMemoryProperties() {
        return vkMemoryProperties;
    }

    public VkPhysicalDevice getVkPhysicalDevice() {
        return vkPhysicalDevice;
    }

    public VkPhysicalDeviceFeatures getVkPhysicalDeviceFeatures() {
        return vkPhysicalDeviceFeatures;
    }

    public VkPhysicalDeviceProperties getVkPhysicalDeviceProperties() {
        return vkPhysicalDeviceProperties;
    }

    public VkQueueFamilyProperties.Buffer getVkQueueFamilyProps() {
        return vkQueueFamilyProps;
    }

    private boolean hasKHRSwapChainExtension() {
        boolean result = false;
        int numExtensions = vkDeviceExtensions != null ? vkDeviceExtensions.capacity() : 0;
        for (int i = 0; i < numExtensions; i++) {
            String extensionName = vkDeviceExtensions.get(i).extensionNameString();
            if (KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME.equals(extensionName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean hasGraphicsQueueFamily() {
        boolean result = false;
        int numQueueFamilies = vkQueueFamilyProps != null ? vkQueueFamilyProps.capacity() : 0;
        for (int i = 0; i < numQueueFamilies; i++) {
            VkQueueFamilyProperties familyProps = vkQueueFamilyProps.get(i);
            if ((familyProps.queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0) {
                result = true;
                break;
            }
        }
        return result;
    }
}

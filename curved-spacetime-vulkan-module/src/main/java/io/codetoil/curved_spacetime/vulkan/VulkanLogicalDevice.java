/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

public class VulkanLogicalDevice {
    private final VulkanPhysicalDevice vulkanPhysicalDevice;
    private final VkDevice vkDevice;

    public VulkanLogicalDevice(VulkanPhysicalDevice vulkanPhysicalDevice) {
        Logger.debug("Creating logical device");

        this.vulkanPhysicalDevice = vulkanPhysicalDevice;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Define required extensions
            Set<String> deviceExtensions = getDeviceExtensions();
            boolean usePortability = deviceExtensions.contains(KHRPortabilitySubset.
                    VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME) && VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;
            int numExtensions = usePortability ? 2 : 1;
            PointerBuffer requiredExtensions = stack.mallocPointer(numExtensions);
            requiredExtensions.put(stack.ASCII(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
            if (usePortability) {
                requiredExtensions.put(stack.ASCII(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
            }
            requiredExtensions.flip();

            // Set up required features
            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);

            // Enable all the queue families
            VkQueueFamilyProperties.Buffer queuePropsBuff = vulkanPhysicalDevice.getVkQueueFamilyProps();
            int numQueueFamilies = queuePropsBuff.capacity();
            VkDeviceQueueCreateInfo.Buffer queueCreationInfoBuf = VkDeviceQueueCreateInfo.calloc(numQueueFamilies,
                    stack);
            for (int index = 0; index < numQueueFamilies; index++) {
                FloatBuffer priorities = stack.callocFloat(queuePropsBuff.get(index).queueCount());
                queueCreationInfoBuf.get(index)
                        .sType(VK14.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(index)
                        .pQueuePriorities(priorities);
            }

            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .ppEnabledExtensionNames(requiredExtensions)
                    .pEnabledFeatures(features)
                    .pQueueCreateInfos(queueCreationInfoBuf);

            PointerBuffer pp = stack.mallocPointer(1);
            VulkanUtils.vkCheck(VK14.vkCreateDevice(vulkanPhysicalDevice.getVkPhysicalDevice(), deviceCreateInfo,
                    null, pp), "Failed to create device");
            this.vkDevice = new VkDevice(pp.get(0), vulkanPhysicalDevice.getVkPhysicalDevice(), deviceCreateInfo);
        }
    }

    private Set<String> getDeviceExtensions() {
        Set<String> deviceExtensions = new HashSet<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtensionsBuf = stack.callocInt(1);
            VK14.vkEnumerateDeviceExtensionProperties(vulkanPhysicalDevice.getVkPhysicalDevice(), (String) null,
                    numExtensionsBuf, null);
            int numExtensions = numExtensionsBuf.get(0);
            Logger.debug("Device supports [{}] extensions", numExtensions);

            VkExtensionProperties.Buffer propsBuff = VkExtensionProperties.calloc(numExtensions, stack);
            VK14.vkEnumerateDeviceExtensionProperties(this.vulkanPhysicalDevice.getVkPhysicalDevice(), (String) null,
                    numExtensionsBuf, propsBuff);
            for (int index = 0; index < numExtensions; index++) {
                VkExtensionProperties props = propsBuff.get(index);
                String extensionName = props.extensionNameString();
                deviceExtensions.add(extensionName);
                Logger.debug("Supported device extension [{}]", extensionName);
            }
        }
        return deviceExtensions;
    }

    public void cleanup() {
        Logger.debug("Destroying Vulkan device");
        VK14.vkDestroyDevice(this.vkDevice, null);
    }

    public VulkanPhysicalDevice getPhysicalDevice() {
        return this.vulkanPhysicalDevice;
    }

    public VkDevice getVkDevice() {
        return this.vkDevice;
    }

    public void waitIdle() {
        VK14.vkDeviceWaitIdle(this.vkDevice);
    }
}

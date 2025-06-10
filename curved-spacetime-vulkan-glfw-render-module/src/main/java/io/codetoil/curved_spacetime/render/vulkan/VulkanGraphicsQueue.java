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
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanQueue;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;

public class VulkanGraphicsQueue extends VulkanQueue {

    public VulkanGraphicsQueue(VulkanLogicalDevice vulkanLogicalDevice, int graphicsQueueFamilyIndex, int queueIndex) {
        super(vulkanLogicalDevice, graphicsQueueFamilyIndex, queueIndex);
    }

    public VulkanGraphicsQueue(VulkanLogicalDevice vulkanLogicalDevice, int queueIndex) {
        this(vulkanLogicalDevice, VulkanGraphicsQueue.getGraphicsQueueFamilyIndex(vulkanLogicalDevice), queueIndex);
    }

    private static int getGraphicsQueueFamilyIndex(VulkanLogicalDevice vulkanLogicalDevice) {
        int result = -1;
        VulkanPhysicalDevice vulkanPhysicalDevice = vulkanLogicalDevice.getPhysicalDevice();
        VkQueueFamilyProperties.Buffer queuePropsBuff = vulkanPhysicalDevice.getVkQueueFamilyProps();
        int numQueuesFamilies = queuePropsBuff.capacity();
        for (int index = 0; index < numQueuesFamilies; index++) {
            VkQueueFamilyProperties props = queuePropsBuff.get(index);
            boolean graphicsQueue = (props.queueFlags() & VK14.VK_QUEUE_GRAPHICS_BIT) != 0;
            if (graphicsQueue) {
                result = index;
                break;
            }
        }

        if (result < 0) {
            throw new RuntimeException("Failed to get graphics Queue family index.");
        }
        return result;
    }

    public static class VulkanGraphicsPresentQueue extends VulkanGraphicsQueue {

        public VulkanGraphicsPresentQueue(VulkanLogicalDevice logicalDevice, VulkanSurface surface,
                                          int queueIndex) {
            super(logicalDevice, getPresentQueueFamilyIndex(logicalDevice, surface), queueIndex);
        }

        private static int getPresentQueueFamilyIndex(VulkanLogicalDevice logicalDevice, VulkanSurface surface) {
            int index = -1;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VulkanPhysicalDevice physicalDevice = logicalDevice.getPhysicalDevice();
                VkQueueFamilyProperties.Buffer queuePropsBuff = physicalDevice.getVkQueueFamilyProps();
                int numQueuesFamilies = queuePropsBuff.capacity();
                IntBuffer intBuffer = stack.mallocInt(1);
                for (int i = 0; i < numQueuesFamilies; i++) {
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice.getVkPhysicalDevice(),
                            i, surface.getVkSurface(), intBuffer);
                    boolean supportsPresentation = intBuffer.get(0) == VK14.VK_TRUE;
                    if (supportsPresentation) {
                        index = i;
                        break;
                    }
                }

                if (index < 0) {
                    throw new RuntimeException("Failed to get Presentation Queue family index.");
                }
                return index;
            }
        }
    }
}

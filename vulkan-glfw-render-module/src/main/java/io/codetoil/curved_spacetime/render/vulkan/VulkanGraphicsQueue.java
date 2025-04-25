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

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanQueue;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class VulkanGraphicsQueue extends VulkanQueue {

    public VulkanGraphicsQueue(VulkanLogicalDevice vulkanLogicalDevice, int queueIndex) {
        super(vulkanLogicalDevice, VulkanGraphicsQueue.getGraphicsQueueFamilyIndex(vulkanLogicalDevice), queueIndex);
    }

    private static int getGraphicsQueueFamilyIndex(VulkanLogicalDevice vulkanLogicalDevice) {
        int result = -1;
        VulkanPhysicalDevice vulkanPhysicalDevice = vulkanLogicalDevice.getPhysicalDevice();
        VkQueueFamilyProperties.Buffer queuePropsBuff = vulkanPhysicalDevice.getVkQueueFamilyProps();
        int numQueuesFamilies = queuePropsBuff.capacity();
        for (int index = 0; index < numQueuesFamilies; index++) {
            VkQueueFamilyProperties props = queuePropsBuff.get(index);
            boolean graphicsQueue = (props.queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0;
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
}

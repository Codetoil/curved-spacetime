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

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkQueue;
import org.tinylog.Logger;

public class VulkanQueue {

    private final VkQueue vkQueue;

    public VulkanQueue(VulkanLogicalDevice vulkanLogicalDevice, int queueFamilyIndex, int queueIndex) {
        Logger.debug("Creating queue");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            VK13.vkGetDeviceQueue(vulkanLogicalDevice.getVkDevice(), queueFamilyIndex, queueIndex, pQueue);
            long queue = pQueue.get(0);
            this.vkQueue = new VkQueue(queue, vulkanLogicalDevice.getVkDevice());
        }
    }

    public VkQueue getVkQueue() {
        return this.vkQueue;
    }

    public void waitIdle() {
        VK13.vkQueueWaitIdle(this.vkQueue);
    }
}

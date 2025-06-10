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

import io.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.tinylog.Logger;

import java.nio.LongBuffer;

public class VulkanSurface {

    private final VulkanPhysicalDevice vulkanPhysicalDevice;
    private final long vkSurface;

    public VulkanSurface(VulkanPhysicalDevice vulkanPhysicalDevice, long windowHandle) {
        Logger.debug("Creating vulkan surface");
        this.vulkanPhysicalDevice = vulkanPhysicalDevice;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(this.vulkanPhysicalDevice.getVkPhysicalDevice().getInstance(),
                    windowHandle, null, pSurface);
            this.vkSurface = pSurface.get(0);
        }
    }

    public void cleanup() {
        Logger.debug("Destroying Vulkan surface");
        KHRSurface.vkDestroySurfaceKHR(vulkanPhysicalDevice.getVkPhysicalDevice().getInstance(), this.vkSurface,
                null);
    }

    public long getVkSurface() {
        return this.vkSurface;
    }
}

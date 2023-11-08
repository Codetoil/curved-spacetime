package io.github.codetoil.curved_spacetime.render.vulkan;

import io.github.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
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
            GLFWVulkan.glfwCreateWindowSurface(this.vulkanPhysicalDevice.getVkPhysicalDevice().getInstance(), windowHandle,
                    null, pSurface);
            this.vkSurface = pSurface.get(0);
        }
    }

    public void cleanup() {
        Logger.debug("Destroying Vulkan surface");
        KHRSurface.vkDestroySurfaceKHR(vulkanPhysicalDevice.getVkPhysicalDevice().getInstance(), vkSurface, null);
    }

    public long getVkSurface() {
        return this.vkSurface;
    }
}

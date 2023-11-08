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

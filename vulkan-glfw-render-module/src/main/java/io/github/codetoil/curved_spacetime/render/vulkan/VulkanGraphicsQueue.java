package io.github.codetoil.curved_spacetime.render.vulkan;

import io.github.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import io.github.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
import io.github.codetoil.curved_spacetime.vulkan.VulkanQueue;
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

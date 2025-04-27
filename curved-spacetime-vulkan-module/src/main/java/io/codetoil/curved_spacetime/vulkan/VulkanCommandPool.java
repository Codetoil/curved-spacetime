package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.tinylog.Logger;

import java.nio.LongBuffer;

public class VulkanCommandPool {
    private final VulkanLogicalDevice vulkanLogicalDevice;
    private final long vkCommandPool;

    public VulkanCommandPool(VulkanLogicalDevice vulkanLogicalDevice, int queueFamilyIndex) {
        Logger.debug("Creating Vulkan CommandPool for " + vulkanLogicalDevice);

        this.vulkanLogicalDevice = vulkanLogicalDevice;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo cmdPoolInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK14.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(queueFamilyIndex);

            LongBuffer lp = stack.mallocLong(1);
            VulkanUtils.vkCheck(VK14.vkCreateCommandPool(vulkanLogicalDevice.getVkDevice(), cmdPoolInfo,
                            null, lp), "failed to create command pool");

            this.vkCommandPool = lp.get(0);
        }
    }

    public void cleanup() {
        VK14.vkDestroyCommandPool(this.vulkanLogicalDevice.getVkDevice(), this.vkCommandPool, null);
    }

    public VulkanLogicalDevice getVulkanLogicalDevice() {
        return this.vulkanLogicalDevice;
    }

    public long getVkCommandPool() {
        return this.vkCommandPool;
    }
}

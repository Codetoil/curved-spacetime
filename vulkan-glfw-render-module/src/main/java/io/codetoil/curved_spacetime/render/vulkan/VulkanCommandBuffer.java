package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

public class VulkanCommandBuffer {
    private final VulkanCommandPool commandPool;
    private final boolean oneTimeSubmit;
    private final VkCommandBuffer vkCommandBuffer;
    private boolean primary;

    public VulkanCommandBuffer(VulkanCommandPool commandPool, boolean primary, boolean oneTimeSubmit) {
        Logger.trace("Creating command buffer");
        this.commandPool = commandPool;
        this.primary = primary;
        this.oneTimeSubmit = oneTimeSubmit;
        VkDevice vkDevice = commandPool.getVulkanLogicalDevice().getVkDevice();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool.getVkCommandPool())
                    .level(primary ? VK14.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK14.VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                    .commandBufferCount(1);
            PointerBuffer pb = stack.mallocPointer(1);
            VulkanUtils.vkCheck(VK14.vkAllocateCommandBuffers(vkDevice, cmdBufAllocateInfo, pb),
                    "Failed to allocate render command buffer");

            this.vkCommandBuffer = new VkCommandBuffer(pb.get(0), vkDevice);
        }
    }

    public void beginRecording() {
        this.beginRecording(null);
    }

    public void beginRecording(VkCommandBufferInheritanceInfo inheritanceInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType(VK14.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            if(this.oneTimeSubmit) {
                cmdBufInfo.flags(VK14.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            }
            if (!this.primary) {
                if (inheritanceInfo == null) {
                    throw new RuntimeException("Secondary buffers must declare inheritance info");
                }
                VkCommandBufferInheritanceInfo vkInheritanceInfo = VkCommandBufferInheritanceInfo.calloc(stack)
                        .sType(VK14.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                        .renderPass(inheritanceInfo.renderPass())
                        .subpass(inheritanceInfo.subpass())
                        .framebuffer(inheritanceInfo.framebuffer());
                cmdBufInfo.pInheritanceInfo(vkInheritanceInfo);
                cmdBufInfo.flags(VK14.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            }
            VulkanUtils.vkCheck(VK14.vkBeginCommandBuffer(this.vkCommandBuffer, cmdBufInfo),
                    "Failed to create command buffer");
        }
    }

    public void cleanup() {
        Logger.trace("Destroying command buffer");
        VK14.vkFreeCommandBuffers(this.commandPool.getVulkanLogicalDevice().getVkDevice(), this.commandPool.getVkCommandPool(),
                this.vkCommandBuffer);
    }

    public void endRecording() {
        VulkanUtils.vkCheck(VK14.vkEndCommandBuffer(this.vkCommandBuffer), "Failed to end command buffer");
    }

    public VkCommandBuffer getVkCommandBuffer() {
        return this.vkCommandBuffer;
    }

    public void reset() {
        VK14.vkResetCommandBuffer(this.vkCommandBuffer, VK14.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
    }
}

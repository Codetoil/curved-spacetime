package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.nio.LongBuffer;

public class VulkanFrameBuffer
{
	private final VulkanLogicalDevice logicalDevice;
	private final long vkFrameBuffer;

	public VulkanFrameBuffer(VulkanLogicalDevice logicalDevice, int width, int height,
							 LongBuffer pAttachments, long renderPass)
	{
		this.logicalDevice = logicalDevice;

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack)
					.sType(VK14.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
					.pAttachments(pAttachments)
					.width(width)
					.height(height)
					.layers(1)
					.renderPass(renderPass);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK14.vkCreateFramebuffer(logicalDevice.getVkDevice(), framebufferCreateInfo,
					null, lp), "Failed to create FrameBuffer");
			this.vkFrameBuffer = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK14.vkDestroyFramebuffer(this.logicalDevice.getVkDevice(), this.vkFrameBuffer, null);
	}

	public long getVkFrameBuffer()
	{
		return this.vkFrameBuffer;
	}
}

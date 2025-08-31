package io.codetoil.curved_spacetime.api.vulkan;

import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

public class VulkanFence
{
	private final VulkanLogicalDevice logicalDevice;
	private final long vkFence;

	public VulkanFence(VulkanLogicalDevice logicalDevice, boolean signaled)
	{
		this.logicalDevice = logicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkFenceCreateInfo fenceCreateInfo =
					VkFenceCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
							.flags(signaled ? VK10.VK_FENCE_CREATE_SIGNALED_BIT : 0);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK10.vkCreateFence(logicalDevice.getVkDevice(), fenceCreateInfo, null, lp),
					"Failed to create fence");
			this.vkFence = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK10.vkDestroyFence(this.logicalDevice.getVkDevice(), this.vkFence, null);
	}

	public void vulkanFenceWait()
	{
		VK10.vkWaitForFences(this.logicalDevice.getVkDevice(), this.vkFence, true, Long.MAX_VALUE);
	}

	public long getVkFence()
	{
		return this.vkFence;
	}

	public void reset()
	{
		VK10.vkResetFences(this.logicalDevice.getVkDevice(), this.vkFence);
	}
}

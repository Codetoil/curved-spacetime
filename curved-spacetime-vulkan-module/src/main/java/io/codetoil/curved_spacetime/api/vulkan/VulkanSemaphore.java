package io.codetoil.curved_spacetime.api.vulkan;

import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;

public class VulkanSemaphore
{
	private final VulkanLogicalDevice logicalDevice;
	private final long vkSemaphore;

	public VulkanSemaphore(VulkanLogicalDevice logicalDevice)
	{
		this.logicalDevice = logicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkSemaphoreCreateInfo semaphoreCreateInfo =
					VkSemaphoreCreateInfo.calloc(stack).sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK10.vkCreateSemaphore(logicalDevice.getVkDevice(), semaphoreCreateInfo, null, lp),
					"Failed to create semaphore");
			this.vkSemaphore = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK10.vkDestroySemaphore(this.logicalDevice.getVkDevice(), this.vkSemaphore, null);
	}

	public long getVkSemaphore()
	{
		return this.vkSemaphore;
	}
}

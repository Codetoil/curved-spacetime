/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br> it under the terms of the GNU General
 * Public License as published by <br> the Free Software Foundation, either version 3 of the License, or <br> (at your
 * option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br> but WITHOUT ANY WARRANTY; without even the
 * implied warranty of<br> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br> GNU General Public License
 * for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br> along with this program.  If not, see <a
 * href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.nio.LongBuffer;

/**
 * A framebuffer: the concrete set of image views a render pass draws into.
 * <p>
 * A render pass describes attachments abstractly; a framebuffer binds those slots to actual
 * images. One is needed per swap chain image, since each frame targets a different image.
 */
public class VulkanRenderModuleFrameBuffer
{
	private final VulkanModuleLogicalDevice logicalDevice;
	private final long vkFrameBuffer;

	/**
	 * Creates a framebuffer binding the given attachments for a render pass.
	 *
	 * @param logicalDevice the device to create the framebuffer on
	 * @param width         the framebuffer's width in pixels
	 * @param height        the framebuffer's height in pixels
	 * @param pAttachments  the image views filling the render pass's attachment slots, in order
	 * @param renderPass    the render pass this framebuffer is compatible with
	 * @throws AssertionError if the framebuffer cannot be created
	 */
	public VulkanRenderModuleFrameBuffer(VulkanModuleLogicalDevice logicalDevice, int width, int height,
										 LongBuffer pAttachments,
										 long renderPass)
	{
		this.logicalDevice = logicalDevice;

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkFramebufferCreateInfo framebufferCreateInfo =
					VkFramebufferCreateInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
							.pAttachments(pAttachments).width(width).height(height).layers(1).renderPass(renderPass);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(VK13.vkCreateFramebuffer(logicalDevice.getVkDevice(), framebufferCreateInfo, null, lp),
					"Failed to create FrameBuffer");
			this.vkFrameBuffer = lp.get(0);
		}
	}

	/**
	 * Destroys the framebuffer, leaving its attachments untouched.
	 */
	public void cleanup()
	{
		VK13.vkDestroyFramebuffer(this.logicalDevice.getVkDevice(), this.vkFrameBuffer, null);
	}

	/**
	 * Returns the underlying Vulkan handle.
	 *
	 * @return the {@code VkFramebuffer} handle
	 */
	public long getVkFrameBuffer()
	{
		return this.vkFrameBuffer;
	}
}

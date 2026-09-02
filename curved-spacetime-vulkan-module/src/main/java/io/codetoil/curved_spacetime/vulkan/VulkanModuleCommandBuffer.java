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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.logging.Logger;

/**
 * A Vulkan command buffer allocated from a {@link VulkanModuleCommandPool}.
 * <p>
 * Commands are recorded between {@link #beginRecording()} and {@link #endRecording()}, then
 * submitted through a {@link VulkanModuleQueue}. A primary buffer can be submitted directly; a
 * secondary one can only be executed from within a primary buffer, and must declare what it
 * inherits when recording begins.
 */
public class VulkanModuleCommandBuffer
{
	private final VulkanModuleCommandPool commandPool;
	private final boolean oneTimeSubmit;
	private final VkCommandBuffer vkCommandBuffer;
	private final boolean primary;
	private final Logger logger;

	/**
	 * Allocates a command buffer from the given pool.
	 *
	 * @param commandPool   the pool to allocate from, which fixes the queue family
	 * @param primary       {@code true} for a primary buffer, {@code false} for a secondary one
	 * @param oneTimeSubmit whether the buffer is recorded fresh before each submission
	 * @param logger        the logger to write buffer diagnostics to
	 * @throws AssertionError if the buffer cannot be allocated
	 */
	public VulkanModuleCommandBuffer(VulkanModuleCommandPool commandPool, boolean primary, boolean oneTimeSubmit,
									 Logger logger)
	{
		this.logger = logger;
		this.logger.finer("Creating command buffer");
		this.commandPool = commandPool;
		this.primary = primary;
		this.oneTimeSubmit = oneTimeSubmit;
		VkDevice vkDevice = commandPool.getVulkanLogicalDevice().getVkDevice();

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferAllocateInfo cmdBufAllocateInfo =
					VkCommandBufferAllocateInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
							.commandPool(commandPool.getVkCommandPool())
							.level(primary ? VK13.VK_COMMAND_BUFFER_LEVEL_PRIMARY :
									VK13.VK_COMMAND_BUFFER_LEVEL_SECONDARY).commandBufferCount(1);
			PointerBuffer pb = stack.mallocPointer(1);
			VulkanUtils.vkCheck(VK13.vkAllocateCommandBuffers(vkDevice, cmdBufAllocateInfo, pb),
					"Failed to allocate render command buffer");

			this.vkCommandBuffer = new VkCommandBuffer(pb.get(0), vkDevice);
		}
	}

	/**
	 * Begins recording into a primary buffer.
	 *
	 * @throws RuntimeException if this is a secondary buffer, which needs inheritance information
	 */
	public void beginRecording()
	{
		this.beginRecording(null);
	}

	/**
	 * Begins recording, supplying what a secondary buffer inherits from its caller.
	 *
	 * @param inheritanceInfo the render pass, subpass, and framebuffer a secondary buffer
	 *                        inherits, or {@code null} for a primary buffer
	 * @throws RuntimeException if this is a secondary buffer and {@code inheritanceInfo} is
	 *                          {@code null}
	 */
	public void beginRecording(VkCommandBufferInheritanceInfo inheritanceInfo)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo cmdBufInfo =
					VkCommandBufferBeginInfo.calloc(stack).sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

			if (this.oneTimeSubmit)
			{
				cmdBufInfo.flags(VK13.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
			}
			if (!this.primary)
			{
				if (inheritanceInfo == null)
				{
					throw new RuntimeException("Secondary buffers must declare inheritance info");
				}
				VkCommandBufferInheritanceInfo vkInheritanceInfo = VkCommandBufferInheritanceInfo.calloc(stack)
						.sType(VK13.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
						.renderPass(inheritanceInfo.renderPass()).subpass(inheritanceInfo.subpass())
						.framebuffer(inheritanceInfo.framebuffer());
				cmdBufInfo.pInheritanceInfo(vkInheritanceInfo);
				cmdBufInfo.flags(VK13.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
			}
			VulkanUtils.vkCheck(VK13.vkBeginCommandBuffer(this.vkCommandBuffer, cmdBufInfo),
					"Failed to create command buffer");
		}
	}

	/**
	 * Returns this buffer to its pool.
	 */
	public void cleanup()
	{
		this.logger.finer("Destroying command buffer");
		VK13.vkFreeCommandBuffers(this.commandPool.getVulkanLogicalDevice().getVkDevice(),
				this.commandPool.getVkCommandPool(), this.vkCommandBuffer);
	}

	/**
	 * Finishes recording, leaving the buffer ready to submit.
	 *
	 * @throws AssertionError if the recorded commands are invalid
	 */
	public void endRecording()
	{
		VulkanUtils.vkCheck(VK13.vkEndCommandBuffer(this.vkCommandBuffer), "Failed to end command buffer");
	}

	/**
	 * Returns the underlying Vulkan command buffer.
	 *
	 * @return the {@code VkCommandBuffer}
	 */
	public VkCommandBuffer getVkCommandBuffer()
	{
		return this.vkCommandBuffer;
	}

	/**
	 * Discards anything recorded, releasing the buffer's resources back to the pool.
	 */
	public void reset()
	{
		VK13.vkResetCommandBuffer(this.vkCommandBuffer, VK13.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
	}
}

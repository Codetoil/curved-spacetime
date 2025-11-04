/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.api.render.vulkan;

import io.codetoil.curved_spacetime.api.vulkan.VulkanLogicalDevice;
import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;

public class VulkanImageView
{
	private final int aspectMask;
	private final int mipLevels;

	private final VulkanLogicalDevice vulkanLogicalDevice;
	private final long vkImageView;
	private final long vkImage;

	public VulkanImageView(VulkanLogicalDevice vulkanLogicalDevice, long vkImage,
						   VulkanImageViewData vulkanImageViewData)
	{
		this.vulkanLogicalDevice = vulkanLogicalDevice;
		this.aspectMask = vulkanImageViewData.aspectMask;
		this.mipLevels = vulkanImageViewData.mipLevels;
		this.vkImage = vkImage;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			LongBuffer lp = stack.mallocLong(1);
			VkImageViewCreateInfo viewCreateInfo =
					VkImageViewCreateInfo
							.calloc(stack)
							.sType$Default()
							.image(vkImage)
							.viewType(vulkanImageViewData.viewType)
							.format(vulkanImageViewData.format)
							.subresourceRange(
									it ->
											it
													.aspectMask(this.aspectMask)
													.baseMipLevel(0)
													.levelCount(this.mipLevels)
													.baseMipLevel(vulkanImageViewData.baseArrayLayer)
													.layerCount(vulkanImageViewData.layerCount));
			VulkanUtils.vkCheck(VK13.vkCreateImageView(vulkanLogicalDevice.getVkDevice(), viewCreateInfo, null, lp),
					"Failed to create image view");
			this.vkImageView = lp.get(0);
		}
	}

	public void cleanup()
	{
		VK13.vkDestroyImageView(this.vulkanLogicalDevice.getVkDevice(), this.vkImageView, null);
	}

	public int getAspectMask()
	{
		return aspectMask;
	}

	public int getMipLevels()
	{
		return mipLevels;
	}

	public long getVkImageView()
	{
		return this.vkImageView;
	}

	public long getVkImage()
	{
		return vkImage;
	}

	public static class VulkanImageViewData
	{
		private int baseArrayLayer;
		private int mipLevels;
		private int aspectMask;
		private int format;
		private int layerCount;
		private int viewType;

		public VulkanImageViewData()
		{
			this.baseArrayLayer = 0;
			this.layerCount = 1;
			this.mipLevels = 1;
			this.viewType = VK13.VK_IMAGE_VIEW_TYPE_2D;
		}

		public VulkanImageView.VulkanImageViewData aspectMask(int aspectMask)
		{
			this.aspectMask = aspectMask;
			return this;
		}

		public VulkanImageView.VulkanImageViewData baseArrayLayer(int baseArrayLayer)
		{
			this.baseArrayLayer = baseArrayLayer;
			return this;
		}

		public VulkanImageView.VulkanImageViewData format(int format)
		{
			this.format = format;
			return this;
		}

		public VulkanImageView.VulkanImageViewData layerCount(int layerCount)
		{
			this.layerCount = layerCount;
			return this;
		}

		public VulkanImageView.VulkanImageViewData mipLevels(int mipLevels)
		{
			this.mipLevels = mipLevels;
			return this;
		}

		public VulkanImageView.VulkanImageViewData viewType(int viewType)
		{
			this.viewType = viewType;
			return this;
		}

	}
}

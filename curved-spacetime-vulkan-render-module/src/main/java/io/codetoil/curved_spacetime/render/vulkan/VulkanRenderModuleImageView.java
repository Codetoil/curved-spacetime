/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;

/**
 * A view onto a Vulkan image, describing how that image is to be interpreted.
 * <p>
 * Images cannot be used directly by a render pass; a view selects which part of an image is in
 * play — its format, which aspect such as colour or depth, and which mip levels and array layers —
 * and it is the view that gets attached to a framebuffer.
 */
public class VulkanRenderModuleImageView
{
	private final int aspectMask;
	private final int mipLevels;

	private final VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	private final long vkImageView;
	private final long vkImage;

	/**
	 * Creates a view onto the given image.
	 *
	 * @param vulkanModuleLogicalDevice the device that owns the image
	 * @param vkImage                   the image to view
	 * @param vulkanImageViewData       how the image should be interpreted
	 * @throws AssertionError if the view cannot be created
	 */
	public VulkanRenderModuleImageView(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, long vkImage,
									   VulkanImageViewData vulkanImageViewData)
	{
		this.vulkanModuleLogicalDevice = vulkanModuleLogicalDevice;
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
			VulkanUtils.vkCheck(
					VK13.vkCreateImageView(vulkanModuleLogicalDevice.getVkDevice(), viewCreateInfo, null, lp),
					"Failed to create image view");
			this.vkImageView = lp.get(0);
		}
	}

	/**
	 * Destroys the view, leaving the image it referenced untouched.
	 */
	public void cleanup()
	{
		VK13.vkDestroyImageView(this.vulkanModuleLogicalDevice.getVkDevice(), this.vkImageView, null);
	}

	/**
	 * Returns which aspect of the image this view exposes.
	 *
	 * @return the Vulkan aspect mask, such as colour or depth
	 */
	public int getAspectMask()
	{
		return aspectMask;
	}

	/**
	 * Returns how many mip levels this view covers.
	 *
	 * @return the mip level count
	 */
	public int getMipLevels()
	{
		return mipLevels;
	}

	/**
	 * Returns the underlying Vulkan handle.
	 *
	 * @return the {@code VkImageView} handle
	 */
	public long getVkImageView()
	{
		return this.vkImageView;
	}

	/**
	 * Returns the image this view was created onto.
	 *
	 * @return the {@code VkImage} handle
	 */
	public long getVkImage()
	{
		return vkImage;
	}

	/**
	 * How an image should be interpreted by a view, built up fluently.
	 * <p>
	 * Defaults describe the common case: a single-layer, single-mip two-dimensional image. Only
	 * {@code format} and {@code aspectMask} normally need setting.
	 */
	public static class VulkanImageViewData
	{
		private int baseArrayLayer;
		private int mipLevels;
		private int aspectMask;
		private int format;
		private int layerCount;
		private int viewType;

		/**
		 * Creates view data describing a single-layer, single-mip two-dimensional image.
		 */
		public VulkanImageViewData()
		{
			this.baseArrayLayer = 0;
			this.layerCount = 1;
			this.mipLevels = 1;
			this.viewType = VK13.VK_IMAGE_VIEW_TYPE_2D;
		}

		/**
		 * Sets which aspect of the image the view exposes.
		 *
		 * @param aspectMask the Vulkan aspect mask, such as colour or depth
		 * @return this object, so calls can be chained
		 */
		public VulkanRenderModuleImageView.VulkanImageViewData aspectMask(int aspectMask)
		{
			this.aspectMask = aspectMask;
			return this;
		}

		/**
		 * Sets the first array layer the view starts at.
		 *
		 * @param baseArrayLayer the index of the first array layer
		 * @return this object, so calls can be chained
		 */
		public VulkanRenderModuleImageView.VulkanImageViewData baseArrayLayer(int baseArrayLayer)
		{
			this.baseArrayLayer = baseArrayLayer;
			return this;
		}

		/**
		 * Sets the pixel format the image is interpreted in.
		 *
		 * @param format the Vulkan image format
		 * @return this object, so calls can be chained
		 */
		public VulkanRenderModuleImageView.VulkanImageViewData format(int format)
		{
			this.format = format;
			return this;
		}

		/**
		 * Sets how many array layers the view covers.
		 *
		 * @param layerCount the number of array layers
		 * @return this object, so calls can be chained
		 */
		public VulkanRenderModuleImageView.VulkanImageViewData layerCount(int layerCount)
		{
			this.layerCount = layerCount;
			return this;
		}

		/**
		 * Sets how many mip levels the view covers.
		 *
		 * @param mipLevels the number of mip levels
		 * @return this object, so calls can be chained
		 */
		public VulkanRenderModuleImageView.VulkanImageViewData mipLevels(int mipLevels)
		{
			this.mipLevels = mipLevels;
			return this;
		}

		/**
		 * Sets the dimensionality of the view.
		 *
		 * @param viewType the Vulkan image view type, such as 2D or cube
		 * @return this object, so calls can be chained
		 */
		public VulkanRenderModuleImageView.VulkanImageViewData viewType(int viewType)
		{
			this.viewType = viewType;
			return this;
		}

	}
}

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
import vulkan.VkImageSubresourceRange;
import vulkan.VkImageViewCreateInfo;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public class VulkanRenderModuleImageView
{
	private final int aspectMask;
	private final int mipLevels;

	private final VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	private final MemorySegment vkImageView;
	private final MemorySegment vkImage;

	public VulkanRenderModuleImageView(VulkanModuleLogicalDevice vulkanModuleLogicalDevice, MemorySegment vkImage,
									   VulkanImageViewData vulkanImageViewData)
	{
		this.vulkanModuleLogicalDevice = vulkanModuleLogicalDevice;
		this.aspectMask = vulkanImageViewData.aspectMask;
		this.mipLevels = vulkanImageViewData.mipLevels;
		this.vkImage = vkImage;
		MemorySegment viewCreateInfo = VkImageViewCreateInfo.allocate(arena);
		VkImageViewCreateInfo.sType(viewCreateInfo, Vulkan.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO());
		VkImageViewCreateInfo.image(viewCreateInfo, vkImage);
		VkImageViewCreateInfo.viewType(viewCreateInfo, vulkanImageViewData.viewType);
		VkImageViewCreateInfo.format(viewCreateInfo, vulkanImageViewData.format);
		MemorySegment subresourceRange = VkImageSubresourceRange.allocate(arena);
		VkImageSubresourceRange.aspectMask(subresourceRange, this.aspectMask);
		VkImageSubresourceRange.baseMipLevel(subresourceRange, 0);
		VkImageSubresourceRange.levelCount(subresourceRange, this.mipLevels);
		VkImageSubresourceRange.baseMipLevel(subresourceRange, vulkanImageViewData.baseArrayLayer);
		VkImageSubresourceRange.layerCount(subresourceRange, vulkanImageViewData.layerCount);
		VkImageViewCreateInfo.subresourceRange(viewCreateInfo, subresourceRange);

		this.vkImageView = arena.allocate(Vulkan.VkImageView);
		VulkanUtils.vkCheck(
				Vulkan.vkCreateImageView(vulkanModuleLogicalDevice.getVkDevice(), viewCreateInfo, null,
						this.vkImageView), "Failed to create image view");
	}

	public static VulkanRenderModuleImageView[] createImageViews(VulkanModuleLogicalDevice vulkanModuleLogicalDevice,
														   MemorySegment swapChain, int format)
	{
		VulkanRenderModuleImageView[] result;

		MemorySegment numImagesSegment = arena.allocateFrom(ValueLayout.ADDRESS, arena.allocate(ValueLayout.JAVA_INT));
		VulkanUtils.vkCheck(Vulkan.vkGetSwapchainImagesKHR(vulkanModuleLogicalDevice.getVkDevice(), swapChain,
				numImagesSegment, null), "Failed to get number of surface images");
		int numImages = numImagesSegment.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);

		MemorySegment swapChainImages = arena.allocate(Vulkan.VkImage, numImages);
		VulkanUtils.vkCheck(Vulkan.vkGetSwapchainImagesKHR(vulkanModuleLogicalDevice.getVkDevice(), swapChain,
						numImagesSegment, swapChainImages), "Failed to get surface images");

		result = new VulkanRenderModuleImageView[numImages];
		VulkanRenderModuleImageView.VulkanImageViewData imageViewData =
				new VulkanRenderModuleImageView.VulkanImageViewData().format(format)
						.aspectMask(Vulkan.VK_IMAGE_ASPECT_COLOR_BIT());
		for (int index = 0; index < numImages; index++)
		{
			result[index] =
					new VulkanRenderModuleImageView(vulkanModuleLogicalDevice, swapChainImages, imageViewData);
		}

		return result;

	}

	public void cleanup()
	{
		Vulkan.vkDestroyImageView(this.vulkanModuleLogicalDevice.getVkDevice(), this.vkImageView, null);
	}

	public int getAspectMask()
	{
		return aspectMask;
	}

	public int getMipLevels()
	{
		return mipLevels;
	}

	public MemorySegment getVkImageView()
	{
		return this.vkImageView;
	}

	public MemorySegment getVkImage()
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
			this.viewType = Vulkan.VK_IMAGE_VIEW_TYPE_2D();
		}

		public VulkanRenderModuleImageView.VulkanImageViewData aspectMask(int aspectMask)
		{
			this.aspectMask = aspectMask;
			return this;
		}

		public VulkanRenderModuleImageView.VulkanImageViewData baseArrayLayer(int baseArrayLayer)
		{
			this.baseArrayLayer = baseArrayLayer;
			return this;
		}

		public VulkanRenderModuleImageView.VulkanImageViewData format(int format)
		{
			this.format = format;
			return this;
		}

		public VulkanRenderModuleImageView.VulkanImageViewData layerCount(int layerCount)
		{
			this.layerCount = layerCount;
			return this;
		}

		public VulkanRenderModuleImageView.VulkanImageViewData mipLevels(int mipLevels)
		{
			this.mipLevels = mipLevels;
			return this;
		}

		public VulkanRenderModuleImageView.VulkanImageViewData viewType(int viewType)
		{
			this.viewType = viewType;
			return this;
		}

	}
}

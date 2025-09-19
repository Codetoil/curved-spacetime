/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

import io.codetoil.curved_spacetime.api.Window;
import io.codetoil.curved_spacetime.api.vulkan.VulkanLogicalDevice;
import io.codetoil.curved_spacetime.api.vulkan.VulkanPhysicalDevice;
import io.codetoil.curved_spacetime.api.vulkan.VulkanSemaphore;
import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VulkanSwapChain
{

	protected final VulkanLogicalDevice vulkanLogicalDevice;
	protected final VulkanImageView[] vulkanImageViews;
	protected final VulkanSwapChain.VulkanSurfaceFormat vulkanSurfaceFormat;
	protected final VkExtent2D vulkanSwapChainExtent;
	protected final long vkSwapChain;
	protected final SynchronizationVulkanSemaphores[] synchronizationVulkanSemaphoresList;
	protected int currentFrame;

	public VulkanSwapChain(VulkanLogicalDevice vulkanLogicalDevice, VulkanSurface surface, Window window,
						   int requestedImages, boolean vsync,
						   VulkanGraphicsQueue.VulkanGraphicsPresentQueue vulkanPresentationQueue,
						   VulkanGraphicsQueue[] vulkanConcurrentQueues)
	{
		Logger.debug("Creating Vulkan SwapChain");
		this.vulkanLogicalDevice = vulkanLogicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{

			VulkanPhysicalDevice vulkanPhysicalDevice = vulkanLogicalDevice.getPhysicalDevice();

			// Get surface capabilities
			VkSurfaceCapabilitiesKHR surfCapabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vulkanPhysicalDevice.getVkPhysicalDevice(),
							surface.getVkSurface(), surfCapabilities), "Failed to get surface capabilities");

			int numImages = calcNumImages(surfCapabilities, requestedImages);
			this.synchronizationVulkanSemaphoresList = new SynchronizationVulkanSemaphores[numImages];
			Arrays.setAll(this.synchronizationVulkanSemaphoresList,
					i -> new SynchronizationVulkanSemaphores(this.vulkanLogicalDevice));
			this.currentFrame = 0;

			this.vulkanSurfaceFormat = calcSurfaceFormat(vulkanPhysicalDevice, surface);

			this.vulkanSwapChainExtent = calcSwapChainExtent(window, surfCapabilities);

			VkSwapchainCreateInfoKHR vkSwapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
					.sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR).surface(surface.getVkSurface())
					.minImageCount(numImages).imageFormat(this.vulkanSurfaceFormat.imageFormat())
					.imageColorSpace(this.vulkanSurfaceFormat.colorSpace()).imageExtent(this.vulkanSwapChainExtent)
					.imageArrayLayers(1).imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
					.preTransform(surfCapabilities.currentTransform())
					.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR).clipped(true);
			int numQueues = vulkanConcurrentQueues != null ? vulkanConcurrentQueues.length : 0;
			List<Integer> indices = new ArrayList<>();
			for (int i = 0; i < numQueues; i++)
			{
				VulkanGraphicsQueue vulkanGraphicsQueue = vulkanConcurrentQueues[i];
				if (vulkanGraphicsQueue.getQueueFamilyIndex() != vulkanPresentationQueue.getQueueFamilyIndex())
				{
					indices.add(vulkanGraphicsQueue.getQueueFamilyIndex());
				}
			}
			if (!indices.isEmpty())
			{
				IntBuffer intBuffer = stack.mallocInt(indices.size() + 1);
				indices.forEach(intBuffer::put);
				intBuffer.put(vulkanPresentationQueue.getQueueFamilyIndex()).flip();
				vkSwapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT)
						.queueFamilyIndexCount(intBuffer.capacity()).pQueueFamilyIndices(intBuffer);
			} else
			{
				vkSwapchainCreateInfo.imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
			}
			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(
					KHRSwapchain.vkCreateSwapchainKHR(vulkanLogicalDevice.getVkDevice(), vkSwapchainCreateInfo, null,
							lp), "Failed to create swap chain");
			this.vkSwapChain = lp.get(0);

			this.vulkanImageViews = createImageViews(stack, vulkanLogicalDevice, this.vkSwapChain,
					this.vulkanSurfaceFormat.imageFormat);
		}
	}

	public void cleanup()
	{
		Logger.debug("Destroying Vulkan SwapChain");
		Arrays.asList(synchronizationVulkanSemaphoresList).forEach(SynchronizationVulkanSemaphores::cleanup);
		this.vulkanSwapChainExtent.free();
		Arrays.asList(this.vulkanImageViews).forEach(VulkanImageView::cleanup);
		KHRSwapchain.vkDestroySwapchainKHR(this.vulkanLogicalDevice.getVkDevice(), this.vkSwapChain, null);
	}

	private int calcNumImages(VkSurfaceCapabilitiesKHR surfCapabilities, int requestedImages)
	{
		int minImages = surfCapabilities.minImageCount();
		int maxImages = surfCapabilities.maxImageCount();
		int result = minImages;
		if (maxImages != 0)
		{
			result = Math.min(requestedImages, maxImages);
		}
		result = Math.max(result, minImages);
		Logger.debug(
				"Requested [{}] images, got [{}] images. Surface capabilities, maxImages: [{}], " + "minImages: [{}]",
				requestedImages, result, maxImages, minImages);
		return result;
	}

	private VulkanSwapChain.VulkanSurfaceFormat calcSurfaceFormat(VulkanPhysicalDevice vulkanPhysicalDevice,
																  VulkanSurface vulkanSurface)
	{
		int imageFormat;
		int colorSpace;

		try (MemoryStack stack = MemoryStack.stackPush())
		{

			IntBuffer ip = stack.mallocInt(1);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vulkanPhysicalDevice.getVkPhysicalDevice(),
							vulkanSurface.getVkSurface(), ip, null), "Failed to get the number of surface formats");
			int numFormats = ip.get(0);
			if (numFormats <= 0)
			{
				throw new RuntimeException("No surface formats retrieved");
			}

			VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vulkanPhysicalDevice.getVkPhysicalDevice(),
							vulkanSurface.getVkSurface(), ip, surfaceFormats), "Failed to get surface formats");

			imageFormat = surfaceFormats.get(0).format();
			colorSpace = surfaceFormats.get(0).colorSpace();
			for (int index = 0; index < numFormats; index++)
			{
				VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(index);
				if (surfaceFormatKHR.format() == VK10.VK_FORMAT_B8G8R8_SRGB &&
						surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				{
					imageFormat = surfaceFormatKHR.format();
					colorSpace = surfaceFormatKHR.colorSpace();
					break;
				}
			}
		}
		return new VulkanSwapChain.VulkanSurfaceFormat(imageFormat, colorSpace);
	}

	private VkExtent2D calcSwapChainExtent(Window window, VkSurfaceCapabilitiesKHR surfCapabilities)
	{
		VkExtent2D result = VkExtent2D.calloc();
		if (surfCapabilities.currentExtent().width() == 0xFFFFFFFF)
		{
			// Surface size undefined. Set to the window size if within bounds
			int width = Math.min(window.getWidth(), surfCapabilities.maxImageCount());
			width = Math.max(width, surfCapabilities.minImageExtent().width());

			int height = Math.min(window.getHeight(), surfCapabilities.maxImageExtent().height());
			height = Math.max(height, surfCapabilities.minImageExtent().height());

			result.set(width, height);
		} else
		{
			result.set(surfCapabilities.currentExtent());
		}
		return result;
	}

	private VulkanImageView[] createImageViews(MemoryStack stack, VulkanLogicalDevice vulkanLogicalDevice,
											   long swapChain, int format)
	{
		VulkanImageView[] result;

		IntBuffer ip = stack.mallocInt(1);
		VulkanUtils.vkCheck(
				KHRSwapchain.vkGetSwapchainImagesKHR(vulkanLogicalDevice.getVkDevice(), swapChain, ip, null),
				"Failed to get number of surface images");
		int numImages = ip.get(0);

		LongBuffer swapChainImages = stack.mallocLong(numImages);
		VulkanUtils.vkCheck(
				KHRSwapchain.vkGetSwapchainImagesKHR(vulkanLogicalDevice.getVkDevice(), swapChain, ip, swapChainImages),
				"Failed to get surface images");

		result = new VulkanImageView[numImages];
		VulkanImageView.VulkanImageViewData imageViewData =
				new VulkanImageView.VulkanImageViewData().format(format).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
		for (int index = 0; index < numImages; index++)
		{
			result[index] = new VulkanImageView(vulkanLogicalDevice, swapChainImages.get(0), imageViewData);
		}

		return result;

	}

	public record VulkanSurfaceFormat(int imageFormat, int colorSpace)
	{
	}

	public VulkanSurfaceFormat getVulkanSurfaceFormat()
	{
		return this.vulkanSurfaceFormat;
	}

	public VulkanLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanLogicalDevice;
	}

	public int getCurrentFrame()
	{
		return this.currentFrame;
	}

	public SynchronizationVulkanSemaphores[] getSyncVulkanSemaphoreList()
	{
		return this.synchronizationVulkanSemaphoresList;
	}

	public VkExtent2D getVulkanSwapChainExtent()
	{
		return this.vulkanSwapChainExtent;
	}

	public VulkanImageView[] getVulkanImageViews()
	{
		return this.vulkanImageViews;
	}

	public int acquireNextImage()
	{
		int imageIndex;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer ip = stack.mallocInt(1);
			int err = KHRSwapchain.vkAcquireNextImageKHR(this.vulkanLogicalDevice.getVkDevice(), this.vkSwapChain, ~0L,
					this.synchronizationVulkanSemaphoresList[currentFrame].imageAcquisitionVulkanSemaphore()
							.getVkSemaphore(), MemoryUtil.NULL, ip);
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				return -1;
			} else if (err != VK10.VK_SUCCESS &&
					err != KHRSwapchain.VK_SUBOPTIMAL_KHR) // If false, not optimal but swapchain can still be used.
			{
				throw new RuntimeException("Failed to acquire image: " + err);
			}
			imageIndex = ip.get(0);
		}

		return imageIndex;
	}

	public boolean presentImage(VulkanGraphicsQueue vulkanGraphicsQueue, int imageIndex)
	{
		boolean resize = false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPresentInfoKHR vkPresentInfo =
					VkPresentInfoKHR.calloc(stack).sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
							.pWaitSemaphores(stack.longs(
									synchronizationVulkanSemaphoresList[currentFrame].renderCompleteVulkanSemaphore()
											.getVkSemaphore())).swapchainCount(1).pSwapchains(stack.longs(vkSwapChain))
							.pImageIndices(stack.ints(imageIndex));

			int err = KHRSwapchain.vkQueuePresentKHR(vulkanGraphicsQueue.getVkQueue(), vkPresentInfo);
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				resize = true;
			} else if (err != VK10.VK_SUCCESS && err != KHRSwapchain.VK_SUBOPTIMAL_KHR)
			{
				throw new RuntimeException("Failed to present KHR: " + err);
			}
			currentFrame = (currentFrame + 1) % vulkanImageViews.length;
			return resize;
		}
	}

	public record SynchronizationVulkanSemaphores(VulkanSemaphore imageAcquisitionVulkanSemaphore,
												  VulkanSemaphore renderCompleteVulkanSemaphore)
	{
		public SynchronizationVulkanSemaphores(VulkanLogicalDevice vulkanLogicalDevice)
		{
			this(new VulkanSemaphore(vulkanLogicalDevice), new VulkanSemaphore(vulkanLogicalDevice));
		}

		public void cleanup()
		{
			this.imageAcquisitionVulkanSemaphore.cleanup();
			this.renderCompleteVulkanSemaphore.cleanup();
		}
	}
}

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

import io.codetoil.curved_spacetime.render.RenderModuleWindow;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleGraphicsModuleQueue.VulkanRenderModuleGraphicsPresentModuleQueue;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleSemaphore;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public class VulkanRenderModuleSwapChain
{

	protected final VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	protected final VulkanRenderModuleImageView[] vulkanRenderModuleImageViews;
	protected final VulkanRenderModuleSwapChain.VulkanSurfaceFormat vulkanSurfaceFormat;
	protected final VkExtent2D vulkanSwapChainExtent;
	protected final long vkSwapChain;
	protected int currentFrame;

	public VulkanRenderModuleSwapChain(VulkanModuleLogicalDevice vulkanModuleLogicalDevice,
									   VulkanRenderModuleSurface surface, RenderModuleWindow window,
									   int requestedImages, boolean vsync
	)
	{
		Logger.debug("Creating Vulkan SwapChain");
		this.vulkanModuleLogicalDevice = vulkanModuleLogicalDevice;
		try (MemoryStack stack = MemoryStack.stackPush())
		{

			VulkanModulePhysicalDevice vulkanModulePhysicalDevice = vulkanModuleLogicalDevice.getPhysicalDevice();

			// Get surface capabilities
			VkSurfaceCapabilitiesKHR surfaceCaps = surface.getSurfaceCaps();

			int requiredImages = calcNumImages(surfaceCaps, requestedImages);

			this.vulkanSwapChainExtent = calcSwapChainExtent(window, surfaceCaps);

			this.vulkanSurfaceFormat = calcSurfaceFormat(vulkanModulePhysicalDevice, surface);

			VkSwapchainCreateInfoKHR vkSwapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
					.sType$Default()
					.surface(surface.getVkSurface())
					.minImageCount(requiredImages)
					.imageFormat(this.vulkanSurfaceFormat.imageFormat())
					.imageColorSpace(this.vulkanSurfaceFormat.colorSpace())
					.imageExtent(this.vulkanSwapChainExtent)
					.imageArrayLayers(1)
					.imageUsage(VK13.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
					.preTransform(surfaceCaps.currentTransform())
					.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
					.clipped(true);
			vkSwapchainCreateInfo.presentMode(
					vsync ? KHRSurface.VK_PRESENT_MODE_FIFO_KHR : KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);

			LongBuffer lp = stack.mallocLong(1);
			VulkanUtils.vkCheck(
					KHRSwapchain.vkCreateSwapchainKHR(vulkanModuleLogicalDevice.getVkDevice(), vkSwapchainCreateInfo,
							null,
							lp), "Failed to create swap chain");
			this.vkSwapChain = lp.get(0);

			this.vulkanRenderModuleImageViews = createImageViews(stack, vulkanModuleLogicalDevice, this.vkSwapChain,
					this.vulkanSurfaceFormat.imageFormat);
		}
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
				"Requested [{}] images, got [{}] images. Surface capabilities, maxImages: [{}], minImages: [{}]",
				requestedImages, result, maxImages, minImages);
		return result;
	}

	private VkExtent2D calcSwapChainExtent(RenderModuleWindow window, VkSurfaceCapabilitiesKHR surfCapabilities)
	{
		VkExtent2D result = VkExtent2D.calloc();
		if (surfCapabilities.currentExtent().width() == 0xFFFFFFFF)
		{
			// Surface size undefined. Set to the window size if within bounds
			int width = Math.min(window.getWidth(), surfCapabilities.maxImageExtent().width());
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

	private VulkanRenderModuleSwapChain.VulkanSurfaceFormat calcSurfaceFormat(
			VulkanModulePhysicalDevice vulkanModulePhysicalDevice,
			VulkanRenderModuleSurface vulkanRenderModuleSurface)
	{
		int imageFormat;
		int colorSpace;

		try (MemoryStack stack = MemoryStack.stackPush())
		{

			IntBuffer ip = stack.mallocInt(1);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vulkanModulePhysicalDevice.getVkPhysicalDevice(),
							vulkanRenderModuleSurface.getVkSurface(), ip, null),
					"Failed to get the number of surface formats");
			int numFormats = ip.get(0);
			if (numFormats <= 0)
			{
				throw new RuntimeException("No surface formats retrieved");
			}

			VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(vulkanModulePhysicalDevice.getVkPhysicalDevice(),
							vulkanRenderModuleSurface.getVkSurface(), ip, surfaceFormats),
					"Failed to get surface formats");

			imageFormat = surfaceFormats.get(0).format();
			colorSpace = surfaceFormats.get(0).colorSpace();
			for (int index = 0; index < numFormats; index++)
			{
				VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(index);
				if (surfaceFormatKHR.format() == VK13.VK_FORMAT_B8G8R8_SRGB &&
						surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				{
					imageFormat = surfaceFormatKHR.format();
					colorSpace = surfaceFormatKHR.colorSpace();
					break;
				}
			}
		}
		return new VulkanRenderModuleSwapChain.VulkanSurfaceFormat(imageFormat, colorSpace);
	}

	private VulkanRenderModuleImageView[] createImageViews(MemoryStack stack,
														   VulkanModuleLogicalDevice vulkanModuleLogicalDevice,
														   long swapChain, int format)
	{
		VulkanRenderModuleImageView[] result;

		IntBuffer ip = stack.mallocInt(1);
		VulkanUtils.vkCheck(
				KHRSwapchain.vkGetSwapchainImagesKHR(vulkanModuleLogicalDevice.getVkDevice(), swapChain, ip, null),
				"Failed to get number of surface images");
		int numImages = ip.get(0);

		LongBuffer swapChainImages = stack.mallocLong(numImages);
		VulkanUtils.vkCheck(
				KHRSwapchain.vkGetSwapchainImagesKHR(vulkanModuleLogicalDevice.getVkDevice(), swapChain, ip,
						swapChainImages),
				"Failed to get surface images");

		result = new VulkanRenderModuleImageView[numImages];
		VulkanRenderModuleImageView.VulkanImageViewData imageViewData =
				new VulkanRenderModuleImageView.VulkanImageViewData().format(format)
						.aspectMask(VK13.VK_IMAGE_ASPECT_COLOR_BIT);
		for (int index = 0; index < numImages; index++)
		{
			result[index] =
					new VulkanRenderModuleImageView(vulkanModuleLogicalDevice, swapChainImages.get(0), imageViewData);
		}

		return result;

	}

	public void cleanup()
	{
		Logger.debug("Destroying Vulkan SwapChain");
		this.vulkanSwapChainExtent.free();
		Arrays.asList(this.vulkanRenderModuleImageViews).forEach(VulkanRenderModuleImageView::cleanup);
		KHRSwapchain.vkDestroySwapchainKHR(this.vulkanModuleLogicalDevice.getVkDevice(), this.vkSwapChain, null);
	}

	public VulkanSurfaceFormat getVulkanSurfaceFormat()
	{
		return this.vulkanSurfaceFormat;
	}

	public VulkanModuleLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanModuleLogicalDevice;
	}

	public int getCurrentFrame()
	{
		return this.currentFrame;
	}

	public VkExtent2D getVulkanSwapChainExtent()
	{
		return this.vulkanSwapChainExtent;
	}

	public VulkanRenderModuleImageView[] getVulkanImageViews()
	{
		return this.vulkanRenderModuleImageViews;
	}

	public boolean presentImage(VulkanRenderModuleGraphicsPresentModuleQueue vulkanGraphicsPresentQueue,
								VulkanModuleSemaphore renderCompleteSemaphore, int imageIndex)
	{
		boolean resize = false;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkPresentInfoKHR present = VkPresentInfoKHR.calloc(stack)
					.sType$Default()
					.pWaitSemaphores(stack.longs(renderCompleteSemaphore.getVkSemaphore()))
					.swapchainCount(1)
					.pSwapchains(stack.longs(vkSwapChain))
					.pImageIndices(stack.ints(imageIndex));

			int err = KHRSwapchain.vkQueuePresentKHR(vulkanGraphicsPresentQueue.getVkQueue(), present);
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				resize = true;
			} else if (err == KHRSwapchain.VK_SUBOPTIMAL_KHR)
			{
				// Not optimal but swap chain can still be used
			} else if (err != VK13.VK_SUCCESS)
			{
				throw new RuntimeException("Failed to present KHR: " + err);
			}
		}
		return resize;
	}

	public int acquireNextImage(VulkanModuleLogicalDevice logicalDevice,
								VulkanModuleSemaphore imageAcquisitionSemaphore)
	{
		int imageIndex;
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer ip = stack.mallocInt(1);
			int err = KHRSwapchain.vkAcquireNextImageKHR(logicalDevice.getVkDevice(), vkSwapChain, ~0L,
					imageAcquisitionSemaphore.getVkSemaphore(), MemoryUtil.NULL, ip);
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				return -1;
			} else if (err == KHRSwapchain.VK_SUBOPTIMAL_KHR)
			{
				// Not optimal but swapchain can still be used
			} else if (err != VK13.VK_SUCCESS)
			{
				throw new RuntimeException("Failed to acquire image: " + err);
			}
			imageIndex = ip.get(0);
		}

		return imageIndex;
	}

	public record VulkanSurfaceFormat(int imageFormat, int colorSpace)
	{
	}
}

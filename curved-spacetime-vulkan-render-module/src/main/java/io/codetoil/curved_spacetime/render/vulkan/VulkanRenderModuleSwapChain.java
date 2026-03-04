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
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSurface.VulkanRenderSurfaceFormat;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleSemaphore;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import vulkan.*;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.logging.Logger;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public class VulkanRenderModuleSwapChain
{

	protected final VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	protected final VulkanRenderModuleImageView[] vulkanRenderModuleImageViews;
	protected final VulkanRenderSurfaceFormat vulkanRenderSurfaceFormat;
	protected final MemorySegment vulkanSwapChainExtent;
	protected final MemorySegment vkSwapChain;
	protected final Logger logger;
	//protected final SynchronizationVulkanSemaphores[] synchronizationVulkanSemaphoresList;
	protected int currentFrame;

	public VulkanRenderModuleSwapChain(VulkanModuleLogicalDevice vulkanModuleLogicalDevice,
									   VulkanRenderModuleSurface surface, RenderModuleWindow renderModuleWindow,
									   int requestedImages, boolean vsync,
									   Logger logger//,
									   // VulkanGraphicsQueue.VulkanGraphicsPresentQueue vulkanPresentationQueue,
									   // VulkanGraphicsQueue[] vulkanConcurrentQueues
	)
	{
		this.logger = logger;
		this.logger.fine("Creating Vulkan SwapChain");
		this.vulkanModuleLogicalDevice = vulkanModuleLogicalDevice;

		VulkanModulePhysicalDevice vulkanModulePhysicalDevice = vulkanModuleLogicalDevice.getPhysicalDevice();

		// Get surface capabilities
		MemorySegment surfaceCaps = surface.getSurfaceCaps();

		int requiredImages = calcNumImages(surfaceCaps, requestedImages);

		/*this.synchronizationVulkanSemaphoresList = new SynchronizationVulkanSemaphores[requiredImages];
		Arrays.setAll(this.synchronizationVulkanSemaphoresList,
				i -> new SynchronizationVulkanSemaphores(this.vulkanLogicalDevice));
		this.currentFrame = 0;*/

		this.vulkanSwapChainExtent = calcSwapChainExtent(renderModuleWindow, surfaceCaps);

		this.vulkanRenderSurfaceFormat = surface.calcSurfaceFormat();

		MemorySegment vkSwapchainCreateInfo = VkSwapchainCreateInfoKHR.allocate(arena);
		VkSwapchainCreateInfoKHR.sType(vkSwapchainCreateInfo, Vulkan.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR());
		VkSwapchainCreateInfoKHR.surface(vkSwapchainCreateInfo, surface.getVkSurface());
		VkSwapchainCreateInfoKHR.minImageCount(vkSwapchainCreateInfo, requiredImages);
		VkSwapchainCreateInfoKHR.imageFormat(vkSwapchainCreateInfo, this.vulkanRenderSurfaceFormat.imageFormat());
		VkSwapchainCreateInfoKHR.imageColorSpace(vkSwapchainCreateInfo, this.vulkanRenderSurfaceFormat.colorSpace());
		VkSwapchainCreateInfoKHR.imageExtent(vkSwapchainCreateInfo, this.vulkanSwapChainExtent);
		VkSwapchainCreateInfoKHR.imageArrayLayers(vkSwapchainCreateInfo, 1);
		VkSwapchainCreateInfoKHR.imageUsage(vkSwapchainCreateInfo, Vulkan.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT());
		VkSwapchainCreateInfoKHR.preTransform(vkSwapchainCreateInfo,
				VkSurfaceCapabilitiesKHR.currentTransform(surfaceCaps));
		VkSwapchainCreateInfoKHR.compositeAlpha(vkSwapchainCreateInfo, Vulkan.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR());
		VkSwapchainCreateInfoKHR.clipped(vkSwapchainCreateInfo, Vulkan.VK_TRUE());
		VkSwapchainCreateInfoKHR.presentMode(vkSwapchainCreateInfo,
				vsync ? Vulkan.VK_PRESENT_MODE_FIFO_KHR() : Vulkan.VK_PRESENT_MODE_IMMEDIATE_KHR());

		/*int numQueues = vulkanConcurrentQueues != null ? vulkanConcurrentQueues.length : 0;
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
			vkSwapchainCreateInfo.imageSharingMode(VK13.VK_SHARING_MODE_CONCURRENT)
					.queueFamilyIndexCount(intBuffer.capacity()).pQueueFamilyIndices(intBuffer);
		} else
		{
			vkSwapchainCreateInfo.imageSharingMode(VK13.VK_SHARING_MODE_EXCLUSIVE);
		}*/

		this.vkSwapChain = arena.allocate(Vulkan.VkSwapchainKHR);
		VulkanUtils.vkCheck(
				Vulkan.vkCreateSwapchainKHR(vulkanModuleLogicalDevice.getVkDevice(), vkSwapchainCreateInfo,
						null,
						this.vkSwapChain), "Failed to create swap chain");


		this.vulkanRenderModuleImageViews =
				VulkanRenderModuleImageView.createImageViews(vulkanModuleLogicalDevice, this.vkSwapChain,
						this.vulkanRenderSurfaceFormat.imageFormat());
	}

	private int calcNumImages(MemorySegment surfCapabilities, int requestedImages)
	{
		int minImages = VkSurfaceCapabilitiesKHR.minImageCount(surfCapabilities);
		int maxImages = VkSurfaceCapabilitiesKHR.maxImageCount(surfCapabilities);
		int result = minImages;
		if (maxImages != 0)
		{
			result = Math.min(requestedImages, maxImages);
		}
		result = Math.max(result, minImages);
		this.logger.fine(
				"Requested [" + requestedImages + "] images, got [" + result +
						"] images. Surface capabilities, maxImages: [" + maxImages + "], minImages: [" + minImages +
						"]");
		return result;
	}

	private MemorySegment calcSwapChainExtent(RenderModuleWindow renderModuleWindow,
										   MemorySegment surfCapabilities)
	{
		MemorySegment result = VkExtent2D.allocate(arena);
		if (VkExtent2D.width(VkSurfaceCapabilitiesKHR.currentExtent(surfCapabilities)) == 0xFFFFFFFF)
		{
			// Surface size undefined. Set to the window size if within bounds
			int width = Math.min(renderModuleWindow.getWidth(),
					VkExtent2D.width(VkSurfaceCapabilitiesKHR.maxImageExtent(surfCapabilities)));
			width = Math.max(width, VkExtent2D.width(VkSurfaceCapabilitiesKHR.minImageExtent(surfCapabilities)));

			int height = Math.min(renderModuleWindow.getHeight(),
					VkExtent2D.height(VkSurfaceCapabilitiesKHR.maxImageExtent(surfCapabilities)));
			height = Math.max(height, VkExtent2D.height(VkSurfaceCapabilitiesKHR.minImageExtent(surfCapabilities)));

			VkExtent2D.width(result, width);
			VkExtent2D.height(result, height);
		} else
		{
			result.copyFrom(VkSurfaceCapabilitiesKHR.currentExtent(surfCapabilities));
		}
		return result;
	}



	public void cleanup()
	{
		this.logger.fine("Destroying Vulkan SwapChain");
		//Arrays.asList(synchronizationVulkanSemaphoresList).forEach(SynchronizationVulkanSemaphores::cleanup);
		this.vulkanSwapChainExtent.unload();
		Arrays.asList(this.vulkanRenderModuleImageViews).forEach(VulkanRenderModuleImageView::cleanup);
		Vulkan.vkDestroySwapchainKHR(this.vulkanModuleLogicalDevice.getVkDevice(), this.vkSwapChain, null);
	}

	public VulkanRenderSurfaceFormat getVulkanSurfaceFormat()
	{
		return this.vulkanRenderSurfaceFormat;
	}

	public VulkanModuleLogicalDevice getVulkanLogicalDevice()
	{
		return this.vulkanModuleLogicalDevice;
	}

	public int getCurrentFrame()
	{
		return this.currentFrame;
	}

	/*public SynchronizationVulkanSemaphores[] getSyncVulkanSemaphoreList()
	{
		return this.synchronizationVulkanSemaphoresList;
	}*/

	public MemorySegment getVulkanSwapChainExtent()
	{
		return this.vulkanSwapChainExtent;
	}

	public VulkanRenderModuleImageView[] getVulkanImageViews()
	{
		return this.vulkanRenderModuleImageViews;
	}

	/*public int acquireNextImage()
	{
		int imageIndex;
			IntBuffer ip = stack.mallocInt(1);
			int err = KHRSwapchain.vkAcquireNextImageKHR(this.vulkanLogicalDevice.getVkDevice(), this.vkSwapChain, ~0L,
					this.synchronizationVulkanSemaphoresList[currentFrame].imageAcquisitionVulkanSemaphore()
							.getVkSemaphore(), MemoryUtil.NULL, ip);
			if (err == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR)
			{
				return -1;
			} else if (err != VK13.VK_SUCCESS &&
					err != KHRSwapchain.VK_SUBOPTIMAL_KHR) // If false, not optimal but swapchain can still be used.
			{
				throw new RuntimeException("Failed to acquire image: " + err);
			}
			imageIndex = ip.get(0);

		return imageIndex;
	}

	public boolean presentImage(VulkanGraphicsQueue vulkanGraphicsQueue, int imageIndex)
	{
		boolean resize = false;
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
			} else if (err != VK13.VK_SUCCESS && err != KHRSwapchain.VK_SUBOPTIMAL_KHR)
			{
				throw new RuntimeException("Failed to present KHR: " + err);
			}
			currentFrame = (currentFrame + 1) % vulkanImageViews.length;
			return resize;
	}*/

	public record SynchronizationVulkanSemaphores(VulkanModuleSemaphore imageAcquisitionVulkanModuleSemaphore,
												  VulkanModuleSemaphore renderCompleteVulkanModuleSemaphore)
	{
		public SynchronizationVulkanSemaphores(VulkanModuleLogicalDevice vulkanModuleLogicalDevice)
		{
			this(new VulkanModuleSemaphore(vulkanModuleLogicalDevice),
					new VulkanModuleSemaphore(vulkanModuleLogicalDevice));
		}

		public void cleanup()
		{
			this.imageAcquisitionVulkanModuleSemaphore.cleanup();
			this.renderCompleteVulkanModuleSemaphore.cleanup();
		}
	}
}

/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2024 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2023 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<br>
 */

package io.github.codetoil.curved_spacetime.render.vulkan;

import io.github.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import io.github.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
import io.github.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public class VulkanSwapChain {

    private final VulkanLogicalDevice vulkanLogicalDevice;
    private final VulkanImageView[] imageViews;
    private final VulkanSwapChain.VulkanSurfaceFormat vulkanSurfaceFormat;
    private final VkExtent2D swapChainExtent;
    private final long vkSwapChain;

    public VulkanSwapChain(VulkanLogicalDevice vulkanLogicalDevice, VulkanSurface surface, VulkanWindow vulkanWindow,
                           int requestedImages, boolean vsync) {
        Logger.debug("Creating Vulkan SwapChain");
        this.vulkanLogicalDevice = vulkanLogicalDevice;
        try (MemoryStack stack = MemoryStack.stackPush()) {

            VulkanPhysicalDevice vulkanPhysicalDevice = vulkanLogicalDevice.getPhysicalDevice();

            // Get surface capabilities
            VkSurfaceCapabilitiesKHR surfCapabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
            VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                    vulkanPhysicalDevice.getVkPhysicalDevice(), surface.getVkSurface(), surfCapabilities),
                    "Failed to get surface capabilities");

            int numImages = calcNumImages(surfCapabilities, requestedImages);

            this.vulkanSurfaceFormat = calcSurfaceFormat(vulkanPhysicalDevice, surface);

            this.swapChainExtent = calcSwapChainExtent(vulkanWindow, surfCapabilities);

            VkSwapchainCreateInfoKHR vkSwapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface.getVkSurface())
                    .minImageCount(numImages)
                    .imageFormat(vulkanSurfaceFormat.imageFormat())
                    .imageColorSpace(vulkanSurfaceFormat.colorSpace())
                    .imageExtent(swapChainExtent)
                    .imageArrayLayers(1)
                    .imageUsage(VK13.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .imageSharingMode(VK13.VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(surfCapabilities.currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .clipped(true)
                    .presentMode(vsync ? KHRSurface.VK_PRESENT_MODE_FIFO_KHR :
                            KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);
            LongBuffer lp = stack.mallocLong(1);
            VulkanUtils.vkCheck(KHRSwapchain.vkCreateSwapchainKHR(vulkanLogicalDevice.getVkDevice(),
                    vkSwapchainCreateInfo, null, lp), "Failed to create swap chain");
            this.vkSwapChain = lp.get(0);

            this.imageViews = createImageViews(stack, vulkanLogicalDevice, vkSwapChain,
                    vulkanSurfaceFormat.imageFormat);
        }
    }

    public void cleanup() {
        Logger.debug("Destroying Vulkan SwapChain");
        swapChainExtent.free();
        Arrays.asList(imageViews).forEach(VulkanImageView::cleanup);
        KHRSwapchain.vkDestroySwapchainKHR(vulkanLogicalDevice.getVkDevice(), vkSwapChain, null);
    }

    private int calcNumImages(VkSurfaceCapabilitiesKHR surfCapabilities, int requestedImages) {
        int minImages = surfCapabilities.minImageCount();
        int maxImages = surfCapabilities.maxImageCount();
        int result = minImages;
        if (maxImages != 0) {
            result = Math.min(requestedImages, maxImages);
        }
        result = Math.max(result, minImages);
        Logger.debug("Requested [{}] images, got [{}] images. Surface capabilities, maxImages: [{}], " +
                        "minImages: [{}]", requestedImages, result, maxImages, minImages);
        return result;
    }

    private VulkanSwapChain.VulkanSurfaceFormat calcSurfaceFormat(VulkanPhysicalDevice vulkanPhysicalDevice,
                                                  VulkanSurface vulkanSurface) {
        int imageFormat;
        int colorSpace;

        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer ip = stack.mallocInt(1);
            VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
                    vulkanPhysicalDevice.getVkPhysicalDevice(), vulkanSurface.getVkSurface(), ip, null),
                    "Failed to get the number of surface formats");
            int numFormats = ip.get(0);
            if (numFormats <= 0) {
                throw new RuntimeException("No surface formats retrieved");
            }

            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
            VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
                    vulkanPhysicalDevice.getVkPhysicalDevice(), vulkanSurface.getVkSurface(),
                    ip, surfaceFormats), "Failed to get surface formats"
            );

            imageFormat = surfaceFormats.get(0).format();
            colorSpace = surfaceFormats.get(0).colorSpace();
            for (int index = 0; index < numFormats; index++)
            {
                VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(index);
                if (surfaceFormatKHR.format() == VK13.VK_FORMAT_B8G8R8_SRGB &&
                    surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    imageFormat = surfaceFormatKHR.format();
                    colorSpace = surfaceFormatKHR.colorSpace();
                    break;
                }
            }
        }
        return new VulkanSwapChain.VulkanSurfaceFormat(imageFormat, colorSpace);
    }

    private VkExtent2D calcSwapChainExtent(VulkanWindow window, VkSurfaceCapabilitiesKHR surfCapabilies) {
        VkExtent2D result = VkExtent2D.calloc();
        if (surfCapabilies.currentExtent().width() == 0xFFFFFFFF) {
            // Surface size undefined. Set to the window size if within bounds
            int width = Math.min(window.getWidth(), surfCapabilies.maxImageCount());
            width = Math.max(width, surfCapabilies.minImageExtent().width());

            int height = Math.min(window.getHeight(), surfCapabilies.maxImageExtent().height());
            height = Math.max(height, surfCapabilies.minImageExtent().height());

            result.set(width, height);
        } else {
            result.set(surfCapabilies.currentExtent());
        }
        return result;
    }

    private VulkanImageView[] createImageViews(MemoryStack stack, VulkanLogicalDevice vulkanLogicalDevice, long swapChain, int format) {
        VulkanImageView[] result;

        IntBuffer ip = stack.mallocInt(1);
        VulkanUtils.vkCheck(KHRSwapchain.vkGetSwapchainImagesKHR(vulkanLogicalDevice.getVkDevice(), swapChain, ip,
                null), "Failed to get number of surface images");
        int numImages = ip.get(0);

        LongBuffer swapChainImages = stack.mallocLong(numImages);
        VulkanUtils.vkCheck(KHRSwapchain.vkGetSwapchainImagesKHR(vulkanLogicalDevice.getVkDevice(), swapChain, ip,
                swapChainImages), "Failed to get surface images");

        result = new VulkanImageView[numImages];
        VulkanImageView.VulkanImageViewData imageViewData = new VulkanImageView.VulkanImageViewData().format(format)
                .aspectMask(VK13.VK_IMAGE_ASPECT_COLOR_BIT);
        for (int index = 0; index < numImages; index++) {
            result[index] = new VulkanImageView(vulkanLogicalDevice, swapChainImages.get(0), imageViewData);
        }

        return result;

    }

    public record VulkanSurfaceFormat(int imageFormat, int colorSpace) {}
}

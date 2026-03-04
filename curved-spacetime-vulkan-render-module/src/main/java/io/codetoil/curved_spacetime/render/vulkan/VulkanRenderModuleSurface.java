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

import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import vulkan.VkSurfaceFormatKHR;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.logging.Logger;

import static io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance.arena;

public abstract class VulkanRenderModuleSurface
{
	protected final VulkanModulePhysicalDevice vulkanModulePhysicalDevice;
	protected final Logger logger;

	public VulkanRenderModuleSurface(VulkanModulePhysicalDevice vulkanModulePhysicalDevice, Logger logger)
	{
		this.vulkanModulePhysicalDevice = vulkanModulePhysicalDevice;
		this.logger = logger;
	}

	public abstract void cleanup();

	public abstract MemorySegment getSurfaceCaps();

	public abstract VulkanRenderSurfaceFormat getSurfaceFormat();

	protected VulkanRenderSurfaceFormat calcSurfaceFormat()
	{
		int imageFormat;
		int colorSpace;
		MemorySegment numFormatsSegment = arena.allocateFrom(ValueLayout.ADDRESS, arena.allocate(ValueLayout.JAVA_INT));
		VulkanUtils.vkCheck(Vulkan.vkGetPhysicalDeviceSurfaceFormatsKHR(this.vulkanModulePhysicalDevice
								.getVkPhysicalDevice(), this.getVkSurface(), numFormatsSegment, null),
				"Failed to get the number surface formats");
		int numFormats = numFormatsSegment.get(ValueLayout.ADDRESS, 0).get(ValueLayout.JAVA_INT, 0);
		if (numFormats <= 0)
		{
			throw new RuntimeException("No surface formats retrieved");
		}

		var surfaceFormats = VkSurfaceFormatKHR.allocateArray(numFormats, arena);
		VulkanUtils.vkCheck(Vulkan.vkGetPhysicalDeviceSurfaceFormatsKHR(this.vulkanModulePhysicalDevice
								.getVkPhysicalDevice(), this.getVkSurface(), numFormatsSegment, surfaceFormats),
				"Failed to get surface formats");

		imageFormat = Vulkan.VK_FORMAT_B8G8R8A8_SRGB();
		colorSpace = VkSurfaceFormatKHR.colorSpace(surfaceFormats.asSlice(0, VkSurfaceFormatKHR.layout()));
		for (int i = 0; i < numFormats; i++)
		{
			MemorySegment surfaceFormatKHR = surfaceFormats.asSlice(i * VkSurfaceFormatKHR.sizeof(),
					VkSurfaceFormatKHR.layout());
			if (VkSurfaceFormatKHR.format(surfaceFormatKHR) == Vulkan.VK_FORMAT_B8G8R8A8_SRGB() &&
					VkSurfaceFormatKHR.colorSpace(surfaceFormatKHR) == Vulkan.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR())
			{
				imageFormat = VkSurfaceFormatKHR.format(surfaceFormatKHR);
				colorSpace = VkSurfaceFormatKHR.colorSpace(surfaceFormatKHR);
				break;
			}
		}
		return new VulkanRenderSurfaceFormat(imageFormat, colorSpace);
	}

	public abstract MemorySegment getVkSurface();

	public record VulkanRenderSurfaceFormat(int imageFormat, int colorSpace)
	{
	}
}

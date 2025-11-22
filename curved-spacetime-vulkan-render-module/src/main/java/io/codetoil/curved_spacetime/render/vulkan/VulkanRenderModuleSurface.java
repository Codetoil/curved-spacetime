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

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.IntBuffer;

public abstract class VulkanRenderModuleSurface
{
	protected final VulkanModulePhysicalDevice vulkanModulePhysicalDevice;

	public VulkanRenderModuleSurface(VulkanModulePhysicalDevice vulkanModulePhysicalDevice)
	{
		this.vulkanModulePhysicalDevice = vulkanModulePhysicalDevice;
	}

	public abstract void cleanup();

	public abstract VkSurfaceCapabilitiesKHR getSurfaceCaps();

	public abstract SurfaceFormat getSurfaceFormat();

	protected SurfaceFormat calcSurfaceFormat()
	{
		int imageFormat;
		int colorSpace;
		try (var stack = MemoryStack.stackPush())
		{
			IntBuffer ip = stack.mallocInt(1);
			VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
							this.vulkanModulePhysicalDevice.getVkPhysicalDevice(), this.getVkSurface(), ip, null),
					"Failed to get the number surface formats");
			int numFormats = ip.get(0);
			if (numFormats <= 0)
			{
				throw new RuntimeException("No surface formats retrieved");
			}

			var surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
							this.vulkanModulePhysicalDevice.getVkPhysicalDevice(),
							this.getVkSurface(), ip, surfaceFormats), "Failed to get surface formats");

			imageFormat = VK13.VK_FORMAT_B8G8R8A8_SRGB;
			colorSpace = surfaceFormats.get(0).colorSpace();
			for (int i = 0; i < numFormats; i++)
			{
				VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(i);
				if (surfaceFormatKHR.format() == VK13.VK_FORMAT_B8G8R8A8_SRGB &&
						surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				{
					imageFormat = surfaceFormatKHR.format();
					colorSpace = surfaceFormatKHR.colorSpace();
					break;
				}
			}
		}
		return new SurfaceFormat(imageFormat, colorSpace);
	}

	public abstract long getVkSurface();

	public record SurfaceFormat(int imageFormat, int colorSpace)
	{
	}
}

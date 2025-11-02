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

package io.codetoil.curved_spacetime.api.render.vulkan_glfw;

import io.codetoil.curved_spacetime.api.render.vulkan.VulkanSurface;
import io.codetoil.curved_spacetime.api.vulkan.VulkanInstance;
import io.codetoil.curved_spacetime.api.vulkan.VulkanPhysicalDevice;
import io.codetoil.curved_spacetime.api.vulkan.utils.VulkanUtils;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class VulkanGLFWSurface extends VulkanSurface
{

	protected final VkSurfaceCapabilitiesKHR surfaceCaps;
	protected final SurfaceFormat surfaceFormat;
	protected final long vkSurface;

	public VulkanGLFWSurface(VulkanInstance vulkanInstance, VulkanPhysicalDevice vulkanPhysicalDevice,
							 long windowHandle)
	{
		super(vulkanPhysicalDevice);
		Logger.debug("Creating vulkan glfw surface");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			LongBuffer pSurface = stack.mallocLong(1);
			GLFWVulkan.glfwCreateWindowSurface(vulkanInstance.getVkInstance(),
					windowHandle, null, pSurface);
			this.vkSurface = pSurface.get(0);
			this.surfaceCaps = VkSurfaceCapabilitiesKHR.calloc();

			VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.vulkanPhysicalDevice
							.getVkPhysicalDevice(), vkSurface, surfaceCaps),
					"Failed to get surface capabilities");

			this.surfaceFormat = calcSurfaceFormat();
		}
	}

	public void cleanup()
	{
		Logger.debug("Destroying Vulkan surface");
		this.surfaceCaps.free();
		KHRSurface.vkDestroySurfaceKHR(vulkanPhysicalDevice.getVkPhysicalDevice().getInstance(), this.vkSurface,
				null);
	}

	public long getVkSurface()
	{
		return this.vkSurface;
	}

	@Override
	public VkSurfaceCapabilitiesKHR getSurfaceCaps()
	{
		return this.surfaceCaps;
	}

	@Override
	public SurfaceFormat getSurfaceFormat()
	{
		return this.surfaceFormat;
	}
}

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

package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSurface;
import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.tinylog.Logger;

import java.nio.LongBuffer;

public class VulkanGLFWRenderModuleSurface extends VulkanRenderModuleSurface
{

	protected final VkSurfaceCapabilitiesKHR surfaceCaps;
	protected final SurfaceFormat surfaceFormat;
	protected final long vkSurface;

	public VulkanGLFWRenderModuleSurface(VulkanModuleVulkanInstance vulkanModuleVulkanInstance,
										 VulkanModulePhysicalDevice vulkanModulePhysicalDevice,
										 long windowHandle)
	{
		super(vulkanModulePhysicalDevice);
		Logger.debug("Creating vulkan glfw surface");
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			LongBuffer pSurface = stack.mallocLong(1);
			GLFWVulkan.glfwCreateWindowSurface(vulkanModuleVulkanInstance.getVkInstance(),
					windowHandle, null, pSurface);
			this.vkSurface = pSurface.get(0);
			this.surfaceCaps = VkSurfaceCapabilitiesKHR.calloc();

			VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.vulkanModulePhysicalDevice
							.getVkPhysicalDevice(), vkSurface, surfaceCaps),
					"Failed to get surface capabilities");

			this.surfaceFormat = calcSurfaceFormat();
		}
	}

	public void cleanup()
	{
		Logger.debug("Destroying Vulkan surface");
		this.surfaceCaps.free();
		KHRSurface.vkDestroySurfaceKHR(vulkanModulePhysicalDevice.getVkPhysicalDevice().getInstance(), this.vkSurface,
				null);
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

	public long getVkSurface()
	{
		return this.vkSurface;
	}
}

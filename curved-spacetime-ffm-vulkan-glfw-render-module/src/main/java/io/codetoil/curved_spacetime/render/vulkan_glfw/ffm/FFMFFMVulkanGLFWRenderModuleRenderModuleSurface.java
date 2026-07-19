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

package io.codetoil.curved_spacetime.render.vulkan_glfw.ffm;

import glfw_vulkan.GLFWVulkan;
import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.vulkan.ffm.FFMVulkanRenderModuleSurface;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleVulkanInstance;
import io.codetoil.curved_spacetime.vulkan.ffm.utils.FFMVulkanUtils;
import vulkan.VkSurfaceCapabilitiesKHR;
import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.util.logging.Logger;

public class FFMFFMVulkanGLFWRenderModuleRenderModuleSurface extends FFMVulkanRenderModuleSurface
{

	protected final MemorySegment surfaceCaps;
	protected final VulkanRenderSurfaceFormat vulkanRenderSurfaceFormat;
	protected final MemorySegment vkSurface;

	public FFMFFMVulkanGLFWRenderModuleRenderModuleSurface(FFMVulkanModuleVulkanInstance ffmVulkanModuleVulkanInstance,
	                                                       FFMVulkanModulePhysicalDevice ffmVulkanModulePhysicalDevice,
	                                                       MemorySegment windowHandle, Logger logger)
	{
		super(ffmVulkanModulePhysicalDevice, logger);
		this.logger.fine("Creating vulkan glfw surface");

		this.vkSurface = MainModuleEngine.getInstance().nativeAllocator.allocate(Vulkan.VkSurfaceKHR);
		GLFWVulkan.glfwCreateWindowSurface(ffmVulkanModuleVulkanInstance.getVkInstance(), windowHandle, null,
				this.vkSurface);
		this.surfaceCaps = VkSurfaceCapabilitiesKHR.allocate(MainModuleEngine.getInstance().nativeAllocator);

		FFMVulkanUtils.vkCheck(Vulkan.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.ffmVulkanModulePhysicalDevice
						.getVkPhysicalDevice(), vkSurface, surfaceCaps),
				"Failed to get surface capabilities");

		this.vulkanRenderSurfaceFormat = calcSurfaceFormat();
	}

	public void cleanup()
	{
		this.logger.fine("Destroying Vulkan surface");
		this.surfaceCaps.unload();
		Vulkan.vkDestroySurfaceKHR(ffmVulkanModulePhysicalDevice.getVkPhysicalDevice(), this.vkSurface, null);
	}

	@Override
	public MemorySegment getSurfaceCaps()
	{
		return this.surfaceCaps;
	}

	@Override
	public VulkanRenderSurfaceFormat getSurfaceFormat()
	{
		return this.vulkanRenderSurfaceFormat;
	}

	public MemorySegment getVkSurface()
	{
		return this.vkSurface;
	}
}

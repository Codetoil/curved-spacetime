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

import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleConfig;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSceneRenderContext;
import io.codetoil.curved_spacetime.render.render_enviornments.RenderEnvironment;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanContext;
import org.lwjgl.glfw.GLFWVulkan;

public class VulkanGLFWRenderModuleSceneRenderContext extends VulkanRenderModuleSceneRenderContext
{
	protected final VulkanGLFWRenderModuleEntrypoint entrypoint;

	public VulkanGLFWRenderModuleSceneRenderContext(VulkanGLFWRenderModuleEntrypoint entrypoint, RenderEnvironment renderEnvironment)
	{
		super(renderEnvironment);
		this.entrypoint = entrypoint;
	}

	public void init(VulkanGLFWRenderEnviornmentCallback renderer)
	{
		VulkanModuleVulkanContext context = new VulkanModuleVulkanContext();
		context.init(entrypoint.getVulkanModuleEntrypoint(),
				GLFWVulkan::glfwGetRequiredInstanceExtensions);
		super.init(this.getContext(),
				() -> new VulkanGLFWRenderModuleWindow("curved-spacetime"),
				() -> new VulkanGLFWRenderModuleSurface(this.getContext().getVulkanInstance(),
						this.getContext().getVulkanPhysicalDevice(),
						((VulkanGLFWRenderModuleWindow) this.window).getWindowHandle()),
				() -> ((VulkanGLFWRenderModuleConfig) this.entrypoint.getConfig())
						.getRequestedImages(),
				() -> ((GLFWRenderModuleConfig) this.entrypoint.getGlfwRenderModuleEntrypoint().getConfig())
						.hasVSync());

	}
}

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

package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleConfig;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleRenderContext;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSceneRenderContext;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanContext;
import io.codetoil.curved_spacetime.vulkan_glfw.VulkanGLFWModuleWindow;
import org.lwjgl.glfw.GLFWVulkan;

public class VulkanGLFWRenderRenderModuleSceneRenderContext extends VulkanRenderModuleSceneRenderContext
{
	protected final VulkanGLFWRenderModuleEntrypoint entrypoint;
	protected final VulkanRenderModuleRenderContext renderContext = new VulkanGLFWRenderModuleRenderContext();

	public VulkanGLFWRenderRenderModuleSceneRenderContext(VulkanGLFWRenderModuleEntrypoint entrypoint, Scene scene)
	{
		super(scene);
		this.entrypoint = entrypoint;
	}

	public void init(VulkanGLFWSceneRenderer renderer)
	{
		VulkanModuleVulkanContext context = new VulkanModuleVulkanContext();
		context.init(entrypoint.getVulkanModuleEntrypoint(),
				GLFWVulkan::glfwGetRequiredInstanceExtensions);
		renderContext.init(renderer, context);
		super.init(renderContext,
				() -> new VulkanGLFWModuleWindow("curved-spacetime"),
				() -> new VulkanGLFWRenderModuleSurface(this.getRenderContext().getContext().getVulkanInstance(),
						this.getRenderContext().getContext().getVulkanPhysicalDevice(),
						((VulkanGLFWModuleWindow) this.window).getWindowHandle()),
				() -> ((VulkanGLFWRenderModuleConfig) this.entrypoint.getConfig())
						.getRequestedImages(),
				() -> ((GLFWRenderModuleConfig) this.entrypoint.getGlfwRenderModuleEntrypoint().getConfig())
						.hasVSync());

	}
}

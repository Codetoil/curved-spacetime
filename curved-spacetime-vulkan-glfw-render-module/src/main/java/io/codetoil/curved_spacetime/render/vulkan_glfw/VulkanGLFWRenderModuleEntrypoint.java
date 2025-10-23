/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025 Anthony Michalek
 * (Codetoil)<br> Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

import io.codetoil.curved_spacetime.api.engine.Engine;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.api.render.vulkan_glfw.VulkanGLFWRenderer;
import io.codetoil.curved_spacetime.api.render.vulkan_glfw.entrypoint.VulkanGLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.api.vulkan_glfw.entrypoint.VulkanGLFWModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.glfw.GLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleConfig;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan_glfw.VulkanGLFWModuleEntrypoint;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class VulkanGLFWRenderModuleEntrypoint implements ModuleInitializer
{
	private ModuleConfig config;
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private GLFWModuleEntrypoint glfwModuleEntrypoint = null;
	private GLFWRenderModuleEntrypoint glfwRenderModuleEntrypoint = null;
	private RenderModuleEntrypoint renderModuleEntrypoint = null;
	private VulkanGLFWModuleEntrypoint vulkanGLFWModuleEntrypoint = null;
	private VulkanModuleEntrypoint vulkanModuleEntrypoint = null;
	private VulkanRenderModuleEntrypoint vulkanRenderModuleEntrypoint = null;

	@Override
	public void onInitialize()
	{
		try
		{
			this.config = new VulkanGLFWRenderModuleConfig().load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Vulkan Render Config", ex);
		}
		try
		{
			recieveDependenciesFromTransferQueue();
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		Engine engine = Engine.getInstance();
		engine.registerSceneLooper("vulkan_glfw_renderer",
				new VulkanGLFWRenderer(engine, engine.scene, (VulkanGLFWRenderModuleConfig) this.config,
						(GLFWRenderModuleConfig) this.glfwRenderModuleEntrypoint.getConfig()));
		EntrypointUtil.invoke("vulkan_glfw_render_module_dependent",
				VulkanGLFWRenderModuleDependentModuleInitializer.class,
				(VulkanGLFWRenderModuleDependentModuleInitializer vulkanGLFWModuleDependentModuleInitializer) ->
						vulkanGLFWModuleDependentModuleInitializer.onInitialize(this));

	}

	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer;
		for (int i = 0; i < 6; i++)
		{
			moduleInitializer = this.dependencyModuleTransferQueue.take();

			if (moduleInitializer instanceof GLFWModuleEntrypoint)
			{
				this.glfwModuleEntrypoint = (GLFWModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof RenderModuleEntrypoint)
			{
				this.renderModuleEntrypoint = (RenderModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof VulkanModuleEntrypoint)
			{
				this.vulkanModuleEntrypoint = (VulkanModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof GLFWRenderModuleEntrypoint)
			{
				this.glfwRenderModuleEntrypoint = (GLFWRenderModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof VulkanGLFWModuleEntrypoint)
			{
				this.vulkanGLFWModuleEntrypoint = (VulkanGLFWModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof VulkanRenderModuleEntrypoint)
			{
				this.vulkanRenderModuleEntrypoint = (VulkanRenderModuleEntrypoint) moduleInitializer;
			}
		}
	}

	@Override
	public ModuleConfig getConfig()
	{
		return this.config;
	}

	@Override
	public TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}
}

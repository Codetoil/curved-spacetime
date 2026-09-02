/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_glfw.entrypoint.VulkanGLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

/**
 * The Vulkan GLFW render module's {@code main} entrypoint.
 * <p>
 * The most dependent module in the tree: it needs the render, GLFW render, Vulkan, and Vulkan
 * render modules all present before it can do anything, and it blocks during initialization until
 * every one of them has been handed to it. Once they have, it registers a scene callback
 * generator, so each scene gets a renderer.
 */
public class VulkanGLFWRenderModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Vulkan GLFW Render Module Logger");
	private ModuleConfig config;
	private GLFWRenderModuleEntrypoint glfwRenderModuleEntrypoint = null;
	private RenderModuleEntrypoint renderModuleEntrypoint = null;
	private VulkanModuleEntrypoint vulkanModuleEntrypoint = null;
	private VulkanRenderModuleEntrypoint vulkanRenderModuleEntrypoint = null;

	/**
	 * Creates the Vulkan GLFW render module's entrypoint.
	 * <p>
	 * Called by the loader; nothing happens until {@link #onInitialize()} runs.
	 */
	public VulkanGLFWRenderModuleEntrypoint()
	{
	}

	@Override
	public void onInitialize()
	{
		this.logger.setLevel(MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel());
		try
		{
			this.config = new VulkanGLFWRenderModuleConfig(this.logger).load();
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
		MainModuleEngine mainModuleEngine = MainModuleEngine.getInstance();
		mainModuleEngine.registerSceneCallbackGenerator(scene ->
				new VulkanGLFWRenderModuleRenderer(mainModuleEngine, scene, this));
		try
		{
			MainModuleEngine.callDependents("vulkan_glfw_render_module_dependent",
					VulkanGLFWRenderModuleDependentModuleInitializer.class,
					(VulkanGLFWRenderModuleDependentModuleInitializer vulkanGLFWModuleDependentModuleInitializer) ->
							vulkanGLFWModuleDependentModuleInitializer.onInitialize(this), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}

	}

	/**
	 * Blocks until all four dependencies have been handed to this module.
	 * <p>
	 * Takes exactly four elements and sorts them by type, since the modules initialise
	 * concurrently and may arrive in any order. The count must match the number of dependencies
	 * declared in {@code quilt.mod.json}: taking too few strands a producer, and taking too many
	 * blocks here forever.
	 *
	 * @throws InterruptedException if interrupted while waiting for a dependency
	 */
	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer;
		for (int i = 0; i < 4; i++)
		{
			moduleInitializer = this.dependencyModuleTransferQueue.take();

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
	public Logger getLogger()
	{
		return this.logger;
	}

	@Override
	public TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}

	/**
	 * Returns the GLFW render module's entrypoint.
	 *
	 * @return the GLFW render entrypoint, or {@code null} before the handshake completes
	 */
	public GLFWRenderModuleEntrypoint getGlfwRenderModuleEntrypoint()
	{
		return glfwRenderModuleEntrypoint;
	}

	/**
	 * Returns the render module's entrypoint.
	 *
	 * @return the render entrypoint, or {@code null} before the handshake completes
	 */
	public RenderModuleEntrypoint getRenderModuleEntrypoint()
	{
		return renderModuleEntrypoint;
	}

	/**
	 * Returns the Vulkan module's entrypoint.
	 *
	 * @return the Vulkan entrypoint, or {@code null} before the handshake completes
	 */
	public VulkanModuleEntrypoint getVulkanModuleEntrypoint()
	{
		return vulkanModuleEntrypoint;
	}

	/**
	 * Returns the Vulkan render module's entrypoint.
	 *
	 * @return the Vulkan render entrypoint, or {@code null} before the handshake completes
	 */
	public VulkanRenderModuleEntrypoint getVulkanRenderModuleEntrypoint()
	{
		return vulkanRenderModuleEntrypoint;
	}
}

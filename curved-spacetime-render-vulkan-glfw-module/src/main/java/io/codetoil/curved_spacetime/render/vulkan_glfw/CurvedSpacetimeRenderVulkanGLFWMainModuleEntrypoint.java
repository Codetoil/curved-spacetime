/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025-2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2025 Antonio Hernández Bejarano<br>
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
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.api.Engine;
import io.codetoil.curved_spacetime.loader.entrypoint.MainModuleInitializer;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.render.CurvedSpacetimeRenderMainModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.CurvedSpacetimeRenderGLFWMainModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.CurvedSpacetimeRenderVulkanMainModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_glfw.entrypoint.CurvedSpacetimeRenderVulkanGLFWModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.CurvedSpacetimeVulkanMainModuleEntrypoint;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

/**
 * The Vulkan GLFW render module's {@code main} entrypoint.
 * <p>
 * The most dependent module in the tree: it needs the render, GLFW render, Vulkan, and Vulkan render modules all
 * present before it can do anything, and it blocks during initialization until every one of them has been handed to it.
 * Once they have, it registers a scene callback generator, so each scene gets a renderer.
 */
public class CurvedSpacetimeRenderVulkanGLFWMainModuleEntrypoint implements MainModuleInitializer
{
	private final TransferQueue<MainModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Curved Spacetime Render Vulkan GLFW Module Logger");
	private ModuleConfig config;
	private CurvedSpacetimeRenderGLFWMainModuleEntrypoint glfwRenderModuleEntrypoint = null;
	private CurvedSpacetimeRenderMainModuleEntrypoint renderModuleEntrypoint = null;
	private CurvedSpacetimeVulkanMainModuleEntrypoint vulkanModuleEntrypoint = null;
	private CurvedSpacetimeRenderVulkanMainModuleEntrypoint vulkanRenderModuleEntrypoint = null;
	private Engine engine;

	/**
	 * Creates the Vulkan GLFW render module's entrypoint.
	 * <p>
	 * Called by the loader; nothing happens until {@link #onInitialize(Engine)} runs.
	 */
	public CurvedSpacetimeRenderVulkanGLFWMainModuleEntrypoint()
	{
	}

	@Override
	public void onInitialize(Engine engine)
	{
		this.engine = engine;
		this.logger.setLevel(this.engine.getLoggerLevel());
		this.logger.addHandler(this.engine.getConsoleHandler());
		this.logger.setUseParentHandlers(false);
		try
		{
			this.config = new CurvedSpacetimeRenderVulkanGLFWModuleConfig(this.logger).load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Render Vulkan GLFW Config", ex);
		}
		try
		{
			recieveDependenciesFromTransferQueue();
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		this.engine.registerSceneCallbackGenerator(scene ->
				new RenderVulkanGLFWModuleRenderer(this.engine, scene, this));
		try
		{
			this.engine.callDependents("curved_spacetime_render_vulkan_glfw_module_dependent_module_initializer",
					CurvedSpacetimeRenderVulkanGLFWModuleDependentModuleInitializer.class,
					(CurvedSpacetimeRenderVulkanGLFWModuleDependentModuleInitializer vulkanGLFWModuleDependentModuleInitializer) ->
							vulkanGLFWModuleDependentModuleInitializer
									.onInitialize(this, this.engine), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}

	}

	/**
	 * Blocks until all four dependencies have been handed to this module.
	 * <p>
	 * Takes exactly four elements and sorts them by type, since the modules initialise concurrently and may arrive in
	 * any order. The count must match the number of dependencies declared in {@code quilt.mod.json}: taking too few
	 * strands a producer, and taking too many blocks here forever.
	 *
	 * @throws InterruptedException
	 * 		if interrupted while waiting for a dependency
	 */
	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		MainModuleInitializer mainModuleInitializer;
		for (int i = 0; i < 4; i++)
		{
			mainModuleInitializer = this.dependencyModuleTransferQueue.take();

			if (mainModuleInitializer instanceof CurvedSpacetimeRenderMainModuleEntrypoint)
			{
				this.renderModuleEntrypoint = (CurvedSpacetimeRenderMainModuleEntrypoint) mainModuleInitializer;
			}
			if (mainModuleInitializer instanceof CurvedSpacetimeVulkanMainModuleEntrypoint)
			{
				this.vulkanModuleEntrypoint = (CurvedSpacetimeVulkanMainModuleEntrypoint) mainModuleInitializer;
			}
			if (mainModuleInitializer instanceof CurvedSpacetimeRenderGLFWMainModuleEntrypoint)
			{
				this.glfwRenderModuleEntrypoint = (CurvedSpacetimeRenderGLFWMainModuleEntrypoint) mainModuleInitializer;
			}
			if (mainModuleInitializer instanceof CurvedSpacetimeRenderVulkanMainModuleEntrypoint)
			{
				this.vulkanRenderModuleEntrypoint = (CurvedSpacetimeRenderVulkanMainModuleEntrypoint) mainModuleInitializer;
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
	public TransferQueue<MainModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}

	/**
	 * Returns the GLFW render module's entrypoint.
	 *
	 * @return the GLFW render entrypoint, or {@code null} before the handshake completes
	 */
	public CurvedSpacetimeRenderGLFWMainModuleEntrypoint getGlfwRenderModuleEntrypoint()
	{
		return glfwRenderModuleEntrypoint;
	}

	/**
	 * Returns the render module's entrypoint.
	 *
	 * @return the render entrypoint, or {@code null} before the handshake completes
	 */
	public CurvedSpacetimeRenderMainModuleEntrypoint getRenderModuleEntrypoint()
	{
		return renderModuleEntrypoint;
	}

	/**
	 * Returns the Vulkan module's entrypoint.
	 *
	 * @return the Vulkan entrypoint, or {@code null} before the handshake completes
	 */
	public CurvedSpacetimeVulkanMainModuleEntrypoint getVulkanModuleEntrypoint()
	{
		return vulkanModuleEntrypoint;
	}

	/**
	 * Returns the Vulkan render module's entrypoint.
	 *
	 * @return the Vulkan render entrypoint, or {@code null} before the handshake completes
	 */
	public CurvedSpacetimeRenderVulkanMainModuleEntrypoint getRenderVulkanModuleEntrypoint()
	{
		return vulkanRenderModuleEntrypoint;
	}
}

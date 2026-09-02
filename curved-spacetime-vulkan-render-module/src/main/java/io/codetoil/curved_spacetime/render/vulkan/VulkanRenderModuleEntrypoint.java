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

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.entrypoint.VulkanRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

/**
 * The Vulkan render module's {@code main} entrypoint.
 * <p>
 * Joins the two halves of Vulkan rendering: the render module's window and input abstractions, and
 * the Vulkan module's devices and queues. It depends on both, so it blocks during initialization
 * until each has been handed to it, then hands itself to the windowing-specific module that
 * actually draws.
 */
public class VulkanRenderModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Vulkan Render Module Logger");
	private ModuleConfig config;
	private VulkanModuleEntrypoint vulkanModuleEntrypoint = null;
	private RenderModuleEntrypoint renderModuleEntrypoint = null;

	/**
	 * Creates the Vulkan render module's entrypoint.
	 * <p>
	 * Called by the loader; nothing happens until {@link #onInitialize()} runs.
	 */
	public VulkanRenderModuleEntrypoint()
	{
	}

	@Override
	public void onInitialize()
	{
		this.logger.setLevel(MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel());
		try
		{
			this.config = new VulkanRenderModuleConfig(this.logger).load();
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
		try
		{
			MainModuleEngine.callDependents("vulkan_render_module_dependent",
					VulkanRenderModuleDependentModuleInitializer.class,
					(VulkanRenderModuleDependentModuleInitializer vulkanRenderModuleDependentModuleInitializer) ->
							vulkanRenderModuleDependentModuleInitializer.onInitialize(this), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Blocks until both dependencies have been handed to this module.
	 * <p>
	 * Takes exactly two elements and sorts them by type, since the Vulkan and render modules
	 * initialise concurrently and either may arrive first.
	 *
	 * @throws InterruptedException if interrupted while waiting for a dependency
	 */
	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer = this.dependencyModuleTransferQueue.take();

		if (moduleInitializer instanceof VulkanModuleEntrypoint)
		{
			this.vulkanModuleEntrypoint = (VulkanModuleEntrypoint) moduleInitializer;
		}
		if (moduleInitializer instanceof RenderModuleEntrypoint)
		{
			this.renderModuleEntrypoint = (RenderModuleEntrypoint) moduleInitializer;
		}

		moduleInitializer = this.dependencyModuleTransferQueue.take();

		if (moduleInitializer instanceof VulkanModuleEntrypoint)
		{
			this.vulkanModuleEntrypoint = (VulkanModuleEntrypoint) moduleInitializer;
		}
		if (moduleInitializer instanceof RenderModuleEntrypoint)
		{
			this.renderModuleEntrypoint = (RenderModuleEntrypoint) moduleInitializer;
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

	public TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}
}

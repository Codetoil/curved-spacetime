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

package io.codetoil.curved_spacetime.render.vulkan.ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.ffm.entrypoint.FFMVulkanRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleEntrypoint;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

public class FFMVulkanRenderModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Vulkan Render Module Logger");
	private ModuleConfig config;
	private FFMVulkanModuleEntrypoint ffmVulkanModuleEntrypoint = null;
	private RenderModuleEntrypoint renderModuleEntrypoint = null;

	@Override
	public void onInitialize()
	{
		this.logger.setLevel(MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel());
		try
		{
			this.config = new FFMVulkanRenderModuleConfig(this.logger).load();
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
					FFMVulkanRenderModuleDependentModuleInitializer.class,
					(FFMVulkanRenderModuleDependentModuleInitializer ffmVulkanRenderModuleDependentModuleInitializer) ->
							ffmVulkanRenderModuleDependentModuleInitializer.onInitialize(this), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer = this.dependencyModuleTransferQueue.take();

		if (moduleInitializer instanceof FFMVulkanModuleEntrypoint)
		{
			this.ffmVulkanModuleEntrypoint = (FFMVulkanModuleEntrypoint) moduleInitializer;
		}
		if (moduleInitializer instanceof RenderModuleEntrypoint)
		{
			this.renderModuleEntrypoint = (RenderModuleEntrypoint) moduleInitializer;
		}

		moduleInitializer = this.dependencyModuleTransferQueue.take();

		if (moduleInitializer instanceof FFMVulkanModuleEntrypoint)
		{
			this.ffmVulkanModuleEntrypoint = (FFMVulkanModuleEntrypoint) moduleInitializer;
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

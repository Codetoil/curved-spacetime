/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025 Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.vulkan_ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.vulkan_ffm.entrypoint.CurvedSpacetimeVulkanFFMModuleDependentModuleInitializer;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

public class CurvedSpacetimeVulkanFFMModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Curved Spacetime Vulkan FFM Module Logger");
	private ModuleConfig config;
	private VulkanFFMModuleVulkan vulkan;

	@Override
	public void onInitialize()
	{
		this.logger.setLevel(MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel());
		try
		{
			this.config = new CurvedSpacetimeVulkanFFMModuleConfig(this.logger).load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Vulkan FFM Config", ex);
		}
		MainModuleEngine.getInstance().registerMainCallback(this.vulkan
				= new VulkanFFMModuleVulkan(MainModuleEngine.getInstance(), this));
		try
		{
			MainModuleEngine.callDependents("curved_spacetime_vulkan_ffm_module_dependent", CurvedSpacetimeVulkanFFMModuleDependentModuleInitializer.class,
					(CurvedSpacetimeVulkanFFMModuleDependentModuleInitializer vulkanFFMModuleDependentModuleInitializer) ->
							vulkanFFMModuleDependentModuleInitializer.onInitialize(this), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
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

	public VulkanFFMModuleVulkan getVulkan()
	{
		return vulkan;
	}
}

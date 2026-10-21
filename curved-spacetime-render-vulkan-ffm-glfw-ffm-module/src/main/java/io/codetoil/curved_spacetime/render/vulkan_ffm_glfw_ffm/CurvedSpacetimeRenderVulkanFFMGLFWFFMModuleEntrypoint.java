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

package io.codetoil.curved_spacetime.render.vulkan_ffm_glfw_ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.CurvedSpacetimeRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw_ffm.CurvedSpacetimeRenderGLFWFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_ffm.CurvedSpacetimeRenderVulkanFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_ffm_glfw_ffm.entrypoint.CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan_ffm.CurvedSpacetimeVulkanFFMModuleEntrypoint;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

public class CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Curved Spacetime Render Vulkan FFM GLFW FFM Module Logger");
	private ModuleConfig config;
	private CurvedSpacetimeRenderGLFWFFMModuleEntrypoint ffmglfwRenderModuleEntrypoint = null;
	private CurvedSpacetimeRenderModuleEntrypoint renderModuleEntrypoint = null;
	private CurvedSpacetimeVulkanFFMModuleEntrypoint vulkanFFMModuleEntrypoint = null;
	private CurvedSpacetimeRenderVulkanFFMModuleEntrypoint renderVulkanFFMModuleEntrypoint = null;

	@Override
	public void onInitialize()
	{
		this.logger.setLevel(MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel());
		try
		{
			this.config = new CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleConfig(this.logger).load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Render Vulkan FFM GLFW FFM Config", ex);
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
				new RenderVulkanFFMGLFWFFMModuleRenderer(mainModuleEngine, scene, this));
		try
		{
			MainModuleEngine.callDependents("curved_spacetime_render_vulkan_ffm_glfw_ffm_module_dependent",
					CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleDependentModuleInitializer.class,
					(CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleDependentModuleInitializer vulkanGLFWModuleDependentModuleInitializer) ->
							vulkanGLFWModuleDependentModuleInitializer.onInitialize(this), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}

	}

	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer;
		for (int i = 0; i < 4; i++)
		{
			moduleInitializer = this.dependencyModuleTransferQueue.take();

			if (moduleInitializer instanceof CurvedSpacetimeRenderModuleEntrypoint)
			{
				this.renderModuleEntrypoint = (CurvedSpacetimeRenderModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof CurvedSpacetimeVulkanFFMModuleEntrypoint)
			{
				this.vulkanFFMModuleEntrypoint = (CurvedSpacetimeVulkanFFMModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof CurvedSpacetimeRenderGLFWFFMModuleEntrypoint)
			{
				this.ffmglfwRenderModuleEntrypoint = (CurvedSpacetimeRenderGLFWFFMModuleEntrypoint) moduleInitializer;
			}
			if (moduleInitializer instanceof CurvedSpacetimeRenderVulkanFFMModuleEntrypoint)
			{
				this.renderVulkanFFMModuleEntrypoint = (CurvedSpacetimeRenderVulkanFFMModuleEntrypoint) moduleInitializer;
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

	public CurvedSpacetimeRenderGLFWFFMModuleEntrypoint getGlfwRenderModuleEntrypoint()
	{
		return ffmglfwRenderModuleEntrypoint;
	}

	public CurvedSpacetimeRenderModuleEntrypoint getRenderModuleEntrypoint()
	{
		return renderModuleEntrypoint;
	}

	public CurvedSpacetimeVulkanFFMModuleEntrypoint getVulkanModuleEntrypoint()
	{
		return vulkanFFMModuleEntrypoint;
	}

	public CurvedSpacetimeRenderVulkanFFMModuleEntrypoint getVulkanRenderModuleEntrypoint()
	{
		return renderVulkanFFMModuleEntrypoint;
	}
}

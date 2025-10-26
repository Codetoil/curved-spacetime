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

package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.api.engine.Engine;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.api.render.glfw.entrypoint.GLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.glfw.GLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class GLFWRenderModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private ModuleConfig config;
	private GLFWModuleEntrypoint glfwModuleEntrypoint;
	private RenderModuleEntrypoint renderModuleEntrypoint;

	@Override
	public void onInitialize()
	{
		try
		{
			this.config = new GLFWRenderModuleConfig().load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load GLFW Render Config", ex);
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
			Engine.callDependents("glfw_render_module_dependent",
					GLFWRenderModuleDependentModuleInitializer.class,
					(GLFWRenderModuleDependentModuleInitializer glfwRenderModuleDependentModuleInitializer) ->
							glfwRenderModuleDependentModuleInitializer.onInitialize(this));
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer = this.dependencyModuleTransferQueue.take();

		if (moduleInitializer instanceof GLFWModuleEntrypoint)
		{
			this.glfwModuleEntrypoint = (GLFWModuleEntrypoint) moduleInitializer;
		}
		if (moduleInitializer instanceof RenderModuleEntrypoint)
		{
			this.renderModuleEntrypoint = (RenderModuleEntrypoint) moduleInitializer;
		}

		moduleInitializer = this.dependencyModuleTransferQueue.take();

		if (moduleInitializer instanceof GLFWModuleEntrypoint)
		{
			this.glfwModuleEntrypoint = (GLFWModuleEntrypoint) moduleInitializer;
		}
		if (moduleInitializer instanceof RenderModuleEntrypoint)
		{
			this.renderModuleEntrypoint = (RenderModuleEntrypoint) moduleInitializer;
		}
	}

	@Override
	public ModuleConfig getConfig()
	{
		return config;
	}

	@Override
	public TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}
}

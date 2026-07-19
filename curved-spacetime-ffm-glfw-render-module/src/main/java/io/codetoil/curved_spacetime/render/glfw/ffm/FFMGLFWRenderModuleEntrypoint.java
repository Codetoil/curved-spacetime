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

package io.codetoil.curved_spacetime.render.glfw.ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.ffm.entrypoint.FFMGLFWRenderModuleDependentModuleInitializer;

import java.io.IOException;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

import static org.lwjgl.system.ffm.FFM.*;

public class FFMGLFWRenderModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private static final SymbolLookup
			GLFW_LOADER = SymbolLookup.libraryLookup(Objects.requireNonNull(org.lwjgl.glfw.GLFW.getLibrary().getPath()), Arena.global());
	private final Logger logger = Logger.getLogger("Curved Spacetime GLFW Render Module Logger");
	private ModuleConfig config;
	private RenderModuleEntrypoint renderModuleEntrypoint;

	static {
		ffmConfig(
				FFMGLFWRenderModuleGLFW.class,
				ffmConfigBuilder(MethodHandles.lookup())
						.withSymbolLookup(GLFW_LOADER)
						.build()
		);
	}

	public static final FFMGLFWRenderModuleGLFW glfw = ffmGenerate(FFMGLFWRenderModuleGLFW.class);

	@Override
	public void onInitialize()
	{
		this.logger.setLevel(MainModuleEngine.getInstance().mainModuleConfig.getLoggerLevel());
		try
		{
			this.config = new FFMGLFWRenderModuleConfig(this.logger).load();
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
			MainModuleEngine.callDependents("glfw_render_module_dependent",
					FFMGLFWRenderModuleDependentModuleInitializer.class,
					(FFMGLFWRenderModuleDependentModuleInitializer ffmglfwRenderModuleDependentModuleInitializer) ->
							ffmglfwRenderModuleDependentModuleInitializer.onInitialize(this),
					this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void recieveDependenciesFromTransferQueue() throws InterruptedException
	{
		ModuleInitializer moduleInitializer = this.dependencyModuleTransferQueue.take();

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
	public Logger getLogger()
	{
		return this.logger;
	}

	@Override
	public TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}
}

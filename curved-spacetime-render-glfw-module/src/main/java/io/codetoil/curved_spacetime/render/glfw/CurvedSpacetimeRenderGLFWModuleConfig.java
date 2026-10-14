/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The GLFW render module's settings, read from {@code config/curved-spacetime-render-glfw-module.config}.
 * <p>
 * One key, {@code vsync}, which decides whether presentation waits for the display's refresh. The Vulkan render module
 * reads it when choosing a swap chain present mode.
 */
public class CurvedSpacetimeRenderGLFWModuleConfig implements ModuleConfig
{
	private static final boolean DEFAULT_VSYNC = true;
	private static final String FILENAME = "config/curved-spacetime-render-glfw-module.config";
	private final Logger logger;
	private boolean dirty = false;
	private boolean vsync;

	/**
	 * Creates a configuration that reports load problems to the given logger.
	 *
	 * @param logger
	 * 		the logger to warn through when a key is missing or invalid
	 */
	public CurvedSpacetimeRenderGLFWModuleConfig(Logger logger)
	{
		this.logger = logger;
	}

	/**
	 * Returns whether presentation should wait for the display's refresh.
	 *
	 * @return {@code true} if vertical sync is enabled
	 */
	public boolean hasVSync()
	{
		return this.vsync;
	}


	public CurvedSpacetimeRenderGLFWModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		try (FileReader reader = new FileReader(CurvedSpacetimeRenderGLFWModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			logger.log(Level.WARNING, ex,
					() -> "Could not find config file " + CurvedSpacetimeRenderGLFWModuleConfig.FILENAME);
			this.dirty = true;
		}

		Object vsyncPropValue = props.get("vsync");
		if (vsyncPropValue != null)
		{
			this.vsync = Boolean.parseBoolean(vsyncPropValue.toString());
		} else
		{
			logger.warning("Could not find required key vsync, resetting to default " +
					CurvedSpacetimeRenderGLFWModuleConfig.DEFAULT_VSYNC);
			this.vsync = CurvedSpacetimeRenderGLFWModuleConfig.DEFAULT_VSYNC;
			this.dirty = true;
		}

		try (FileReader reader = new FileReader(CurvedSpacetimeRenderGLFWModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			logger.log(Level.WARNING, ex,
					() -> "Could not find config file " + CurvedSpacetimeRenderGLFWModuleConfig.FILENAME);
			this.dirty = true;
		}

		return this;
	}

	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("vsync", String.valueOf(this.vsync));

		try (FileWriter writer = new FileWriter(CurvedSpacetimeRenderGLFWModuleConfig.FILENAME))
		{
			props.store(writer, "Config for the Render GLFW Module.");
		}
		this.dirty = false;
	}

	public boolean isDirty()
	{
		return this.dirty;
	}
}

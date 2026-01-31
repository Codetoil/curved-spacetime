/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainModuleConfig
{
	private static final int DEFAULT_FPS = 60;
	private static final String FILENAME = "config/main-module.config";
	private final Logger logger;
	private boolean dirty = false;
	private int fps;

	public MainModuleConfig(Logger logger)
	{
		this.logger = logger;
	}

	public int getFPS()
	{
		return this.fps;
	}

	public MainModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		try (FileReader reader = new FileReader(MainModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			logger.log(Level.WARNING, ex, () -> "Could not find config file " + MainModuleConfig.FILENAME);
			this.dirty = true;
		}

		Object fpsPropValue = props.get("fps");
		if (fpsPropValue != null)
		{
			try
			{
				this.fps = Integer.parseInt(fpsPropValue.toString());
			} catch (NumberFormatException ex)
			{
				logger.log(Level.WARNING, ex, () -> "Invalid value for key fps: " + fpsPropValue +
						", valid bounds [1,1000], resetting to default " + MainModuleConfig.DEFAULT_FPS);
				this.fps = MainModuleConfig.DEFAULT_FPS;
				this.dirty = true;
			}
			if (this.fps < 1 || this.fps > 1000)
			{
				logger.log(Level.WARNING, () -> "Invalid value for key fps: " + fpsPropValue +
						", valid bounds [1,1000], resetting to default " + MainModuleConfig.DEFAULT_FPS);
				this.fps = MainModuleConfig.DEFAULT_FPS;
				this.dirty = true;
			}
		} else
		{
			logger.warning("Could not find required key fps, valid bounds [1,1000], resetting to default " +
					MainModuleConfig.DEFAULT_FPS);
			this.fps = MainModuleConfig.DEFAULT_FPS;
			this.dirty = true;
		}

		return this;
	}

	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("fps", String.valueOf(this.fps));

		File configFile = new File(MainModuleConfig.FILENAME);
		if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs())
			throw new IOException("Could not create config directory");

		try (FileWriter writer = new FileWriter(MainModuleConfig.FILENAME))
		{
			props.store(writer, "Config for the Main Module.");
		}
		this.dirty = false;
	}

	public boolean isDirty()
	{
		return this.dirty;
	}
}

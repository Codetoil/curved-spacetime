/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

import org.tinylog.Logger;

import java.io.*;
import java.util.Properties;

public class MainModuleConfig
{
	private static final int DEFAULT_FPS = 60;
	private static final String FILENAME = "main-module.config";
	private boolean dirty = false;
	private int fps;

	public MainModuleConfig()
	{

	}

	public int getFPS()
	{
		return this.fps;
	}

	public MainModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		Object fpsPropValue = props.get("fps");
		if (fpsPropValue != null)
		{
			try
			{
				this.fps = Integer.parseInt(fpsPropValue.toString());
			} catch (NumberFormatException ex)
			{
				Logger.warn(ex, "Invalid value for key fps: {}, valid bounds [1,1000], resetting to default {}",
						fpsPropValue, MainModuleConfig.DEFAULT_FPS);
				this.fps = MainModuleConfig.DEFAULT_FPS;
				this.dirty = true;
			}
			if (this.fps < 1 || this.fps > 1000)
			{
				Logger.warn("Invalid value for key fps: {}, valid bounds [1,1000], resetting to default {}", this.fps,
						MainModuleConfig.DEFAULT_FPS);
				this.fps = MainModuleConfig.DEFAULT_FPS;
				this.dirty = true;
			}
		} else
		{
			Logger.warn("Could not find required key fps, valid bounds [1,1000], resetting to default {}",
					MainModuleConfig.DEFAULT_FPS);
			this.fps = MainModuleConfig.DEFAULT_FPS;
			this.dirty = true;
		}

		try (FileReader reader = new FileReader(MainModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			Logger.warn(ex, "Could not find config file " + MainModuleConfig.FILENAME);
			this.dirty = true;
		}

		return this;
	}

	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("fps", String.valueOf(this.fps));

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

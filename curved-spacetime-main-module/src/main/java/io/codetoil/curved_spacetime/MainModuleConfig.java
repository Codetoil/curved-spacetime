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

import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class MainModuleConfig
{
	private static final int DEFAULT_TPS = 60;
	private static final String FILENAME = "config/main-module.config";
	private boolean dirty = false;
	private int tps;

	public MainModuleConfig()
	{

	}

	public int getTPS()
	{
		return this.tps;
	}

	public MainModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		try (FileReader reader = new FileReader(MainModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			Logger.warn(ex, "Could not find config file " + MainModuleConfig.FILENAME);
			this.dirty = true;
		}

		Object fpsPropValue = props.get("tps");
		if (fpsPropValue != null)
		{
			try
			{
				this.tps = Integer.parseInt(fpsPropValue.toString());
			} catch (NumberFormatException ex)
			{
				Logger.warn(ex, "Invalid value for key tps: {}, valid bounds [1,1000], resetting to default {}",
						fpsPropValue, MainModuleConfig.DEFAULT_TPS);
				this.tps = MainModuleConfig.DEFAULT_TPS;
				this.dirty = true;
			}
			if (this.tps < 1 || this.tps > 1000)
			{
				Logger.warn("Invalid value for key tps: {}, valid bounds [1,1000], resetting to default {}", this.tps,
						MainModuleConfig.DEFAULT_TPS);
				this.tps = MainModuleConfig.DEFAULT_TPS;
				this.dirty = true;
			}
		} else
		{
			Logger.warn("Could not find required key tps, valid bounds [1,1000], resetting to default {}",
					MainModuleConfig.DEFAULT_TPS);
			this.tps = MainModuleConfig.DEFAULT_TPS;
			this.dirty = true;
		}

		return this;
	}

	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("tps", String.valueOf(this.tps));

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

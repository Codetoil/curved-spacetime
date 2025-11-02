/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.render;

import io.codetoil.curved_spacetime.api.loader.entrypoint.ModuleConfig;
import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class RenderModuleConfig implements ModuleConfig
{
	private static final String FILENAME = "render-module.config";
	private boolean dirty = false;

	public RenderModuleConfig()
	{

	}

	public RenderModuleConfig load() throws IOException
	{
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") Properties props = new Properties();

		try (FileReader reader = new FileReader(RenderModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			Logger.warn(ex, "Could not find config file " + RenderModuleConfig.FILENAME);
			this.dirty = true;
		}

		return this;
	}

	public void save() throws IOException
	{
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") Properties props = new Properties();

		try (FileWriter writer = new FileWriter(RenderModuleConfig.FILENAME))
		{
			props.store(writer, "Config for the GLFW Render Module.");
		}
		this.dirty = false;
	}

	public boolean isDirty()
	{
		return this.dirty;
	}
}

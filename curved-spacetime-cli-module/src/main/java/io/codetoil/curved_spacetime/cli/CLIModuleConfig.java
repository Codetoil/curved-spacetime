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

package io.codetoil.curved_spacetime.cli;

import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The CLI module's settings, read from {@code config/cli-module.config}.
 * <p>
 * The module has no settings of its own yet, so the file is written empty. It exists so that the
 * file and its handling are already in place when the first key is added.
 */
public class CLIModuleConfig implements ModuleConfig
{
	private static final String FILENAME = "config/cli-module.config";
	private final Logger logger;
	private boolean dirty = false;

	/**
	 * Creates a configuration that reports load problems to the given logger.
	 *
	 * @param logger the logger to warn through when the file is missing
	 */
	public CLIModuleConfig(Logger logger)
	{

		this.logger = logger;
	}

	/**
	 * Reads the configuration from disk, tolerating an absent file.
	 *
	 * @return this configuration, so that construction and loading compose
	 * @throws IOException if the file exists but cannot be read
	 */
	public CLIModuleConfig load() throws IOException
	{
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") Properties props = new Properties();

		try (FileReader reader = new FileReader(CLIModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			logger.log(Level.WARNING, ex, () -> "Could not find config file " + CLIModuleConfig.FILENAME);
			this.dirty = true;
		}

		return this;
	}

	/**
	 * Writes the configuration to disk and clears the dirty flag.
	 *
	 * @throws IOException if the file cannot be written
	 */
	public void save() throws IOException
	{
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection") Properties props = new Properties();

		try (FileWriter writer = new FileWriter(CLIModuleConfig.FILENAME))
		{
			props.store(writer, "Config for the CLI Module.");
		}
		this.dirty = false;
	}

	/**
	 * Returns whether this configuration differs from what is on disk.
	 * <p>
	 * With no keys yet, the only cause is the file having been absent when it was loaded;
	 * {@link #save()} clears it.
	 *
	 * @return {@code true} if the file does not match this configuration and should be rewritten
	 */
	public boolean isDirty()
	{
		return this.dirty;
	}
}

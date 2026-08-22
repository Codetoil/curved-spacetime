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

/**
 * The engine's own settings, read from {@code config/main-module.config}.
 * <p>
 * Holds the frame rate the callback loop runs at and the level every module's logger is set to.
 * Neither key is required to be present: loading substitutes a documented default for anything
 * missing or unparseable and reports that through {@link #isDirty()}, so that a user who deletes
 * the file gets a complete one back on the next run.
 */
public class MainModuleConfig
{
	private static final Level DEFAULT_LOGGER_LEVEL = Level.INFO;
	private static final int DEFAULT_FPS = 60;
	private static final String FILENAME = "config/main-module.config";
	private final Logger logger;
	private boolean dirty = false;
	private Level loggerLevel;
	private int fps;

	/**
	 * Creates a configuration that reports load problems to the given logger.
	 * <p>
	 * Nothing is read from disk until {@link #load()} is called.
	 *
	 * @param logger the logger to warn through when a key is missing or invalid
	 */
	public MainModuleConfig(Logger logger)
	{
		this.logger = logger;
	}

	/**
	 * Returns the frame rate the engine's callback loop runs at.
	 *
	 * @return the configured frames per second, within the inclusive bounds 1 and 1000
	 */
	public int getFPS()
	{
		return this.fps;
	}

	/**
	 * Returns the level every module sets its logger to.
	 *
	 * @return the configured logging level
	 */
	public Level getLoggerLevel()
	{
		return loggerLevel;
	}

	/**
	 * Reads the configuration from disk, substituting defaults for anything missing or invalid.
	 * <p>
	 * A missing file is not an error. Each key that is absent or fails to parse is reported
	 * through the logger, replaced with its default, and marks the configuration dirty.
	 *
	 * @return this configuration, so that construction and loading compose
	 * @throws IOException if the file exists but cannot be read
	 */
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

		Object logger_level = props.get("logger_level");
		if (logger_level != null)
		{
			try
			{
				this.loggerLevel = Level.parse(logger_level.toString());
			} catch (IllegalArgumentException ex)
			{
				logger.warning("Invalid value " + logger_level + " for key logger_level," +
						" resetting to default value " + DEFAULT_LOGGER_LEVEL.getName());
				logger.warning("Known Valid Values: OFF, FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, ALL");
				logger.warning("An integer value between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE
						+ " is also allowed.");
				this.loggerLevel = MainModuleConfig.DEFAULT_LOGGER_LEVEL;
				this.dirty = true;
			}
		} else
		{
			logger.warning("Could not find required key logger_level, resetting to default value "
					+ DEFAULT_LOGGER_LEVEL.getName());
			logger.warning("Valid values: OFF, FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, ALL");
			logger.warning("An integer value between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE
					+ " is also allowed.");
			this.loggerLevel = MainModuleConfig.DEFAULT_LOGGER_LEVEL;
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

	/**
	 * Writes the configuration to disk, creating {@code config/} if it does not exist.
	 * <p>
	 * Every key is written, not only those that changed, and the dirty flag is cleared on success.
	 *
	 * @throws IOException if the config directory cannot be created or the file cannot be written
	 */
	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("logger_level", this.loggerLevel.getName());
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

	/**
	 * Returns whether this configuration differs from what is on disk.
	 * <p>
	 * Loading having substituted a default for {@code fps} or {@code logger_level} is the usual
	 * cause; {@link #save()} clears it.
	 *
	 * @return {@code true} if the file does not match this configuration and should be rewritten
	 */
	public boolean isDirty()
	{
		return this.dirty;
	}
}

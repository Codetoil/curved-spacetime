/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2026 Anthony Michalek (Codetoil)<br>
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the configuration contract of the Module System Specification against
 * {@link MainModuleConfig}.
 * <p>
 * Covers requirements R32 through R37: {@code load()} returns the receiver, tolerates an absent
 * file, substitutes documented defaults for missing or unparseable keys while setting the dirty
 * flag, and {@code save()} writes a complete file and clears that flag.
 * <p>
 * {@link MainModuleConfig} resolves its file as a path relative to the working directory, so the
 * {@code test} task sets {@code workingDir} to a scratch directory under {@code build/}.
 */
class MainModuleConfigTest
{
	/**
	 * The path {@link MainModuleConfig} reads and writes, relative to the working directory.
	 */
	private static final String CONFIG_PATH = "config/main-module.config";

	/**
	 * The default frame rate substituted when {@code fps} is missing or invalid.
	 */
	private static final int DEFAULT_FPS = 60;

	/**
	 * The default level substituted when {@code logger_level} is missing or invalid.
	 */
	private static final Level DEFAULT_LOGGER_LEVEL = Level.INFO;

	private Logger logger;

	/**
	 * Silences the logger and removes any configuration left by a previous test.
	 *
	 * @throws IOException if the existing configuration file cannot be deleted
	 */
	@BeforeEach
	void setUp() throws IOException
	{
		this.logger = Logger.getLogger(MainModuleConfigTest.class.getName());
		this.logger.setLevel(Level.OFF);
		MainModuleConfigTest.deleteConfig();
	}

	/**
	 * Removes the configuration file so that tests do not leak state into one another.
	 *
	 * @throws IOException if the configuration file cannot be deleted
	 */
	@AfterEach
	void tearDown() throws IOException
	{
		MainModuleConfigTest.deleteConfig();
	}

	/**
	 * Deletes the configuration file if it is present.
	 *
	 * @throws IOException if the file exists but cannot be deleted
	 */
	private static void deleteConfig() throws IOException
	{
		Files.deleteIfExists(new File(MainModuleConfigTest.CONFIG_PATH).toPath());
	}

	/**
	 * Writes a configuration file with the given raw contents.
	 *
	 * @param contents the property lines to write
	 * @throws IOException if the file or its parent directory cannot be created
	 */
	private static void writeConfig(String contents) throws IOException
	{
		File configFile = new File(MainModuleConfigTest.CONFIG_PATH);
		if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs())
		{
			throw new IOException("Could not create config directory");
		}
		try (FileWriter writer = new FileWriter(configFile))
		{
			writer.write(contents);
		}
	}

	/**
	 * Reads the configuration file back as properties.
	 *
	 * @return the properties the configuration file currently holds
	 * @throws IOException if the file cannot be read
	 */
	private static Properties readConfig() throws IOException
	{
		Properties props = new Properties();
		try (var reader = Files.newBufferedReader(new File(MainModuleConfigTest.CONFIG_PATH).toPath()))
		{
			props.load(reader);
		}
		return props;
	}

	/**
	 * R32 — {@code load()} returns the receiver, so construction and loading compose.
	 *
	 * @throws IOException if loading fails
	 */
	@Test
	void loadReturnsTheReceiver() throws IOException
	{
		MainModuleConfig config = new MainModuleConfig(this.logger);
		assertSame(config, config.load(), "load() must return the receiver");
	}

	/**
	 * R33 — an absent file yields defaults for every key and sets the dirty flag.
	 *
	 * @throws IOException if loading fails
	 */
	@Test
	void absentFileYieldsDefaultsAndIsDirty() throws IOException
	{
		MainModuleConfig config = new MainModuleConfig(this.logger).load();

		assertEquals(MainModuleConfigTest.DEFAULT_FPS, config.getFPS(),
				"a missing file must yield the default fps");
		assertEquals(MainModuleConfigTest.DEFAULT_LOGGER_LEVEL, config.getLoggerLevel(),
				"a missing file must yield the default logger level");
		assertTrue(config.isDirty(), "a missing file must leave the config dirty");
	}

	/**
	 * R34 — a valid file is read verbatim and leaves the config clean.
	 *
	 * @throws IOException if writing or loading fails
	 */
	@Test
	void validFileIsReadAndLeavesConfigClean() throws IOException
	{
		MainModuleConfigTest.writeConfig("fps=144\nlogger_level=WARNING\n");

		MainModuleConfig config = new MainModuleConfig(this.logger).load();

		assertEquals(144, config.getFPS(), "a valid fps must be read verbatim");
		assertEquals(Level.WARNING, config.getLoggerLevel(), "a valid logger level must be read verbatim");
		assertFalse(config.isDirty(), "a complete, valid file must leave the config clean");
	}

	/**
	 * R34 — an unparseable {@code fps} falls back to the default and sets the dirty flag.
	 *
	 * @throws IOException if writing or loading fails
	 */
	@Test
	void unparseableFpsFallsBackToDefault() throws IOException
	{
		MainModuleConfigTest.writeConfig("fps=notanumber\nlogger_level=INFO\n");

		MainModuleConfig config = new MainModuleConfig(this.logger).load();

		assertEquals(MainModuleConfigTest.DEFAULT_FPS, config.getFPS(),
				"an unparseable fps must fall back to the default");
		assertTrue(config.isDirty(), "an unparseable fps must leave the config dirty");
	}

	/**
	 * R34 — an {@code fps} outside the documented bounds falls back to the default.
	 *
	 * @throws IOException if writing or loading fails
	 */
	@Test
	void outOfRangeFpsFallsBackToDefault() throws IOException
	{
		MainModuleConfigTest.writeConfig("fps=0\nlogger_level=INFO\n");

		MainModuleConfig config = new MainModuleConfig(this.logger).load();

		assertEquals(MainModuleConfigTest.DEFAULT_FPS, config.getFPS(),
				"an fps below the valid bound must fall back to the default");
		assertTrue(config.isDirty(), "an out-of-range fps must leave the config dirty");
	}

	/**
	 * R34 — an unparseable {@code logger_level} falls back to the default.
	 *
	 * @throws IOException if writing or loading fails
	 */
	@Test
	void unparseableLoggerLevelFallsBackToDefault() throws IOException
	{
		MainModuleConfigTest.writeConfig("fps=60\nlogger_level=NOT_A_LEVEL\n");

		MainModuleConfig config = new MainModuleConfig(this.logger).load();

		assertEquals(MainModuleConfigTest.DEFAULT_LOGGER_LEVEL, config.getLoggerLevel(),
				"an unparseable logger level must fall back to the default");
		assertTrue(config.isDirty(), "an unparseable logger level must leave the config dirty");
	}

	/**
	 * R36 — {@code save()} creates the directory, writes every key, and clears the dirty flag.
	 *
	 * @throws IOException if saving or reading back fails
	 */
	@Test
	void saveWritesEveryKeyAndClearsDirty() throws IOException
	{
		MainModuleConfig config = new MainModuleConfig(this.logger).load();
		assertTrue(config.isDirty(), "precondition: a defaulted config is dirty");

		config.save();

		assertFalse(config.isDirty(), "save() must clear the dirty flag");

		Properties written = MainModuleConfigTest.readConfig();
		assertEquals(String.valueOf(MainModuleConfigTest.DEFAULT_FPS), written.getProperty("fps"),
				"save() must write the fps key");
		assertEquals(MainModuleConfigTest.DEFAULT_LOGGER_LEVEL.getName(), written.getProperty("logger_level"),
				"save() must write the logger_level key");
	}

	/**
	 * R37 — a saved file is complete, so reloading it produces the same values and stays clean.
	 *
	 * @throws IOException if saving or reloading fails
	 */
	@Test
	void savedFileReloadsCleanly() throws IOException
	{
		MainModuleConfigTest.writeConfig("fps=30\nlogger_level=FINE\n");
		MainModuleConfig saved = new MainModuleConfig(this.logger).load();
		saved.save();

		MainModuleConfig reloaded = new MainModuleConfig(this.logger).load();

		assertEquals(30, reloaded.getFPS(), "a saved file must round-trip the fps");
		assertEquals(Level.FINE, reloaded.getLoggerLevel(), "a saved file must round-trip the logger level");
		assertFalse(reloaded.isDirty(), "a saved file must be complete enough to reload cleanly");
	}
}

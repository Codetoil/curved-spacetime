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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Vulkan module's settings, read from {@code config/vulkan-module.config}.
 * <p>
 * Two keys: {@code validation} turns the Vulkan validation layers on, which is expensive but
 * reports API misuse the driver would otherwise ignore; {@code preferredDeviceName} names a GPU to
 * use in preference to the automatic choice, which matters on machines with more than one.
 * {@code preferredDeviceName} is genuinely optional and absent by default, so unlike other keys
 * its absence does not mark the configuration dirty.
 * In a future version {@code preferredDeviceName} will be added as a comment if no such comment is
 * there and the option is absent.
 */
public class VulkanModuleConfig implements ModuleConfig
{
	private static final boolean DEFAULT_VALIDATE = false;
	private static final String DEFAULT_PREFERRED_DEVICE_NAME = null;
	private static final String FILENAME = "config/vulkan-module.config";
	private final Logger logger;
	private boolean _validation;
	private String preferredDeviceName;
	private boolean dirty = false;

	/**
	 * Creates a configuration that reports load problems to the given logger.
	 *
	 * @param logger the logger to warn through when a key is missing or invalid
	 */
	public VulkanModuleConfig(Logger logger)
	{

		this.logger = logger;
	}

	/**
	 * Reads the configuration from disk, substituting defaults for anything missing.
	 *
	 * @return this configuration, so that construction and loading compose
	 * @throws IOException if the file exists but cannot be read
	 */
	public VulkanModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		try (FileReader reader = new FileReader(VulkanModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			logger.log(Level.WARNING, ex, () -> "Could not find config file " + VulkanModuleConfig.FILENAME);
			this.dirty = true;
		}

		Object validatePropValue = props.get("validation");
		if (validatePropValue != null)
		{
			this._validation = Boolean.parseBoolean(validatePropValue.toString());
		} else
		{
			logger.warning("Could not find required key validation, resetting to default {}" +
					VulkanModuleConfig.DEFAULT_VALIDATE);
			this._validation = VulkanModuleConfig.DEFAULT_VALIDATE;
			this.dirty = true;
		}

		Object preferredDeviceNamePropValue = props.get("preferredDeviceName");
		if (preferredDeviceNamePropValue != null)
		{
			this.preferredDeviceName = preferredDeviceNamePropValue.toString();
		} else
		{
			this.preferredDeviceName = VulkanModuleConfig.DEFAULT_PREFERRED_DEVICE_NAME;
		}


		return this;
	}

	/**
	 * Writes the configuration to disk and clears the dirty flag.
	 * <p>
	 * {@code preferredDeviceName} is omitted when unset, rather than written empty.
	 *
	 * @throws IOException if the file cannot be written
	 */
	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("validation", String.valueOf(this._validation));
		if (this.preferredDeviceName != null) props.put("preferredDeviceName", this.preferredDeviceName);

		try (FileWriter writer = new FileWriter(FILENAME))
		{
			props.store(writer, "Config for the Vulkan Module.");
		}
		this.dirty = false;
	}

	/**
	 * Returns whether this configuration differs from what is on disk.
	 * <p>
	 * Loading having substituted a default for {@code validation} is the usual cause;
	 * {@link #save()} clears it. Note that an absent {@code preferredDeviceName} does not count,
	 * since that key is genuinely optional.
	 *
	 * @return {@code true} if the file does not match this configuration and should be rewritten
	 */
	public boolean isDirty()
	{
		return this.dirty;
	}

	/**
	 * Returns whether the Vulkan validation layers should be enabled.
	 *
	 * @return {@code true} if validation was requested
	 */
	public boolean validation()
	{
		return this._validation;
	}

	/**
	 * Returns the GPU to prefer over the automatic choice.
	 *
	 * @return the preferred device name, or {@code null} to select automatically
	 */
	public String getPreferredDeviceName()
	{
		return this.preferredDeviceName;
	}
}

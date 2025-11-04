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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.api.loader.entrypoint.ModuleConfig;
import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class VulkanModuleConfig implements ModuleConfig
{
	private static final boolean DEFAULT_VALIDATE = false;
	private static final String DEFAULT_PREFERRED_DEVICE_NAME = null;
	private static final String FILENAME = "config/vulkan-module.config";
	private boolean _validation;
	private String preferredDeviceName;
	private boolean dirty = false;

	public VulkanModuleConfig()
	{

	}

	public VulkanModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		try (FileReader reader = new FileReader(VulkanModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			Logger.warn(ex, "Could not find config file " + VulkanModuleConfig.FILENAME);
			this.dirty = true;
		}

		Object validatePropValue = props.get("validation");
		if (validatePropValue != null)
		{
			this._validation = Boolean.parseBoolean(validatePropValue.toString());
		} else
		{
			Logger.warn("Could not find required key validation, resetting to default {}",
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

	public boolean isDirty()
	{
		return this.dirty;
	}

	public boolean validation()
	{
		return this._validation;
	}

	public String getPreferredDeviceName()
	{
		return this.preferredDeviceName;
	}
}

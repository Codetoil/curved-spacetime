/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
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

package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class VulkanGLFWRenderModuleConfig implements ModuleConfig
{
	private static final int DEFAULT_REQUESTED_IMAGES = 2;
	private static final String FILENAME = "config/vulkan-glfw-render-module.config";
	private int requestedImages;
	private boolean dirty = false;

	public VulkanGLFWRenderModuleConfig()
	{
	}

	public int getRequestedImages()
	{
		return this.requestedImages;
	}

	public VulkanGLFWRenderModuleConfig load() throws IOException
	{
		Properties props = new Properties();

		try (FileReader reader = new FileReader(VulkanGLFWRenderModuleConfig.FILENAME))
		{
			props.load(reader);
		} catch (FileNotFoundException ex)
		{
			Logger.warn(ex, "Could not find config file " + VulkanGLFWRenderModuleConfig.FILENAME);
			this.dirty = true;
		}


		Object requestedImagesPropValue = props.get("requestedImages");
		if (requestedImagesPropValue != null)
		{
			try
			{
				this.requestedImages = Integer.parseInt(requestedImagesPropValue.toString());
			} catch (NumberFormatException ex)
			{
				Logger.warn(ex,
						"Invalid value for key requestedImages: {}, " + "lower bound 2, resetting to default {}",
						requestedImagesPropValue, VulkanGLFWRenderModuleConfig.DEFAULT_REQUESTED_IMAGES);
				this.requestedImages = VulkanGLFWRenderModuleConfig.DEFAULT_REQUESTED_IMAGES;
				this.dirty = true;
			}
			if (this.requestedImages < 2)
			{
				Logger.warn("Invalid value for key requestedImages: {}, " + "lower bound 2, resetting to default {}",
						this.requestedImages, VulkanGLFWRenderModuleConfig.DEFAULT_REQUESTED_IMAGES);
				this.requestedImages = VulkanGLFWRenderModuleConfig.DEFAULT_REQUESTED_IMAGES;
				this.dirty = true;
			}
		} else
		{
			Logger.warn("Could not find required key requestedImages, " + "lower bound 2, resetting to default {}",
					VulkanGLFWRenderModuleConfig.DEFAULT_REQUESTED_IMAGES);
			this.requestedImages = VulkanGLFWRenderModuleConfig.DEFAULT_REQUESTED_IMAGES;
			this.dirty = true;
		}

		return this;
	}

	public void save() throws IOException
	{
		Properties props = new Properties();
		props.put("requestedImages", String.valueOf(this.requestedImages));

		try (FileWriter writer = new FileWriter(VulkanGLFWRenderModuleConfig.FILENAME))
		{
			props.store(writer, "Config for the Vulkan GLFW Render Module.");
		}
		this.dirty = false;
	}

	public boolean isDirty()
	{
		return this.dirty;
	}
}

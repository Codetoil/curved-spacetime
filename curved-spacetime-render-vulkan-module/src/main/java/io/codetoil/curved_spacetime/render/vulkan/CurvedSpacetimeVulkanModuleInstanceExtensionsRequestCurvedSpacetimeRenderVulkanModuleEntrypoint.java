/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.entrypoint.CurvedSpacetimeVulkanModuleInstanceExtensionsRequest;
import org.lwjgl.vulkan.KHRSurface;

import java.util.List;

/**
 * Ensures the Surface Extension is installed, otherwise refuses to initialize Vulkan
 */
public class CurvedSpacetimeVulkanModuleInstanceExtensionsRequestCurvedSpacetimeRenderVulkanModuleEntrypoint implements
		CurvedSpacetimeVulkanModuleInstanceExtensionsRequest
{
	/**
	 * temp
	 */
	public CurvedSpacetimeVulkanModuleInstanceExtensionsRequestCurvedSpacetimeRenderVulkanModuleEntrypoint()
	{
	}

	@Override
	public List<String> requiredInstanceExtensions()
	{
		return List.of(KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME);
	}

	@Override
	public List<String> optionalInstanceExtensions()
	{
		return List.of();
	}
}

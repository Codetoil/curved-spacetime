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

package io.codetoil.curved_spacetime.vulkan.entrypoint;

import java.util.List;

/**
 * Vulkan Module Device Extensions Request
 */
public interface CurvedSpacetimeVulkanModuleDeviceExtensionsRequest
{
	/**
	 * Extensions a device must supply to initialize Vulkan
	 * Prevents Vulkan from initializing if no device supports these extensions.
	 * If a feature is optional use {@link optionalDeviceExtensions()} instead.
	 * @return the requested required device extensions
	 */
	List<String> requiredDeviceExtensions();

	/**
	 * Extensions a device may optionally support for extra features.
	 * @return the requested optional device extensions
	 */
	List<String> optionalDeviceExtensions();
}

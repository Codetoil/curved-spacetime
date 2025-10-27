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

package io.codetoil.curved_spacetime.api.vulkan.utils;

import org.lwjgl.vulkan.VK13;

import java.util.Locale;

public class VulkanUtils
{
	private VulkanUtils()
	{
		// Utility class
	}

	public static OSType getOS()
	{
		OSType result;
		String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((os.contains("mac")) || (os.contains("darwin")))
		{
			result = OSType.MACOS;
		} else if (os.contains("win"))
		{
			result = OSType.WINDOWS;
		} else if (os.contains("nux"))
		{
			result = OSType.LINUX;
		} else
		{
			result = OSType.OTHER;
		}

		return result;
	}

	public static void vkCheck(int err, String errMsg)
	{
		if (err != VK13.VK_SUCCESS)
		{
			String errCode = switch (err) {
				case VK13.VK_NOT_READY -> "VK_NOT_READY";
				case VK13.VK_TIMEOUT -> "VK_TIMEOUT";
				case VK13.VK_EVENT_SET -> "VK_EVENT_SET";
				case VK13.VK_EVENT_RESET -> "VK_EVENT_RESET";
				case VK13.VK_INCOMPLETE -> "VK_INCOMPLETE";
				case VK13.VK_ERROR_OUT_OF_HOST_MEMORY -> "VK_ERROR_OUT_OF_HOST_MEMORY";
				case VK13.VK_ERROR_OUT_OF_DEVICE_MEMORY -> "VK_ERROR_OUT_OF_DEVICE_MEMORY";
				case VK13.VK_ERROR_INITIALIZATION_FAILED -> "VK_ERROR_INITIALIZATION_FAILED";
				case VK13.VK_ERROR_DEVICE_LOST -> "VK_ERROR_DEVICE_LOST";
				case VK13.VK_ERROR_MEMORY_MAP_FAILED -> "VK_ERROR_MEMORY_MAP_FAILED";
				case VK13.VK_ERROR_LAYER_NOT_PRESENT -> "VK_ERROR_LAYER_NOT_PRESENT";
				case VK13.VK_ERROR_EXTENSION_NOT_PRESENT -> "VK_ERROR_EXTENSION_NOT_PRESENT";
				case VK13.VK_ERROR_FEATURE_NOT_PRESENT -> "VK_ERROR_FEATURE_NOT_PRESENT";
				case VK13.VK_ERROR_INCOMPATIBLE_DRIVER -> "VK_ERROR_INCOMPATIBLE_DRIVER";
				case VK13.VK_ERROR_TOO_MANY_OBJECTS -> "VK_ERROR_TOO_MANY_OBJECTS";
				case VK13.VK_ERROR_FORMAT_NOT_SUPPORTED -> "VK_ERROR_FORMAT_NOT_SUPPORTED";
				case VK13.VK_ERROR_FRAGMENTED_POOL -> "VK_ERROR_FRAGMENTED_POOL";
				case VK13.VK_ERROR_UNKNOWN -> "VK_ERROR_UNKNOWN";
				default -> "Not mapped";
			};
			throw new AssertionError(errMsg + ": " + errCode + "[" + err + "]");
		}
	}

	public enum OSType
	{
		WINDOWS, LINUX, OTHER, MACOS
	}
}

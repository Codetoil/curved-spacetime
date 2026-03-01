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

package io.codetoil.curved_spacetime.vulkan.utils;

import vulkan.Vulkan;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
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

	/**
	 * Reverses the byte order of the given MemorySegment in-place. (Generated using Bing AI)
	 */
	public static void reverseBytes(MemorySegment segment) {
		long size = segment.byteSize();
		for (long i = 0; i < size / 2; i++) {
			byte b1 = segment.get(ValueLayout.JAVA_BYTE, i);
			byte b2 = segment.get(ValueLayout.JAVA_BYTE, size - 1 - i);

			// Swap
			segment.set(ValueLayout.JAVA_BYTE, i, b2);
			segment.set(ValueLayout.JAVA_BYTE, size - 1 - i, b1);
		}
	}

	public static void vkCheck(int err, String errMsg)
	{
		if (err != Vulkan.VK_SUCCESS())
		{
			String errCode = switch (err)
			{
				case Vulkan.VK_NOT_READY() -> "VK_NOT_READY";
				case Vulkan.VK_TIMEOUT() -> "VK_TIMEOUT";
				case Vulkan.VK_EVENT_SET() -> "VK_EVENT_SET";
				case Vulkan.VK_EVENT_RESET() -> "VK_EVENT_RESET";
				case Vulkan.VK_INCOMPLETE() -> "VK_INCOMPLETE";
				case Vulkan.VK_ERROR_OUT_OF_HOST_MEMORY() -> "VK_ERROR_OUT_OF_HOST_MEMORY";
				case Vulkan.VK_ERROR_OUT_OF_DEVICE_MEMORY() -> "VK_ERROR_OUT_OF_DEVICE_MEMORY";
				case Vulkan.VK_ERROR_INITIALIZATION_FAILED() -> "VK_ERROR_INITIALIZATION_FAILED";
				case Vulkan.VK_ERROR_DEVICE_LOST() -> "VK_ERROR_DEVICE_LOST";
				case Vulkan.VK_ERROR_MEMORY_MAP_FAILED() -> "VK_ERROR_MEMORY_MAP_FAILED";
				case Vulkan.VK_ERROR_LAYER_NOT_PRESENT() -> "VK_ERROR_LAYER_NOT_PRESENT";
				case Vulkan.VK_ERROR_EXTENSION_NOT_PRESENT() -> "VK_ERROR_EXTENSION_NOT_PRESENT";
				case Vulkan.VK_ERROR_FEATURE_NOT_PRESENT() -> "VK_ERROR_FEATURE_NOT_PRESENT";
				case Vulkan.VK_ERROR_INCOMPATIBLE_DRIVER() -> "VK_ERROR_INCOMPATIBLE_DRIVER";
				case Vulkan.VK_ERROR_TOO_MANY_OBJECTS() -> "VK_ERROR_TOO_MANY_OBJECTS";
				case Vulkan.VK_ERROR_FORMAT_NOT_SUPPORTED() -> "VK_ERROR_FORMAT_NOT_SUPPORTED";
				case Vulkan.VK_ERROR_FRAGMENTED_POOL() -> "VK_ERROR_FRAGMENTED_POOL";
				case Vulkan.VK_ERROR_UNKNOWN() -> "VK_ERROR_UNKNOWN";
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

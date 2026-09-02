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

package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.utils.VulkanUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.IntBuffer;
import java.util.logging.Logger;

/**
 * A Vulkan surface: the bridge between a platform window and Vulkan's presentation machinery.
 * <p>
 * Creating one is inherently platform-specific, so this class holds only what is common — querying
 * capabilities and negotiating a format — and leaves the actual creation to a subclass that knows
 * the windowing library.
 */
public abstract class VulkanRenderModuleSurface
{
	/**
	 * The physical device the surface's capabilities are queried against.
	 */
	protected final VulkanModulePhysicalDevice vulkanModulePhysicalDevice;

	/**
	 * The logger this surface writes its diagnostics to.
	 */
	protected final Logger logger;

	/**
	 * Creates a surface bound to the given physical device.
	 *
	 * @param vulkanModulePhysicalDevice the device to query surface support against
	 * @param logger                     the logger to write surface diagnostics to
	 */
	public VulkanRenderModuleSurface(VulkanModulePhysicalDevice vulkanModulePhysicalDevice, Logger logger)
	{
		this.vulkanModulePhysicalDevice = vulkanModulePhysicalDevice;
		this.logger = logger;
	}

	/**
	 * Destroys the surface and releases the platform resources behind it.
	 */
	public abstract void cleanup();

	/**
	 * Returns what this surface supports: image count bounds, extent bounds, and transforms.
	 * <p>
	 * A swap chain must be built within these limits.
	 *
	 * @return the surface capabilities
	 */
	public abstract VkSurfaceCapabilitiesKHR getSurfaceCaps();

	/**
	 * Returns the pixel format and colour space chosen for this surface.
	 *
	 * @return the negotiated surface format
	 */
	public abstract SurfaceFormat getSurfaceFormat();

	/**
	 * Picks a format from those the surface advertises.
	 * <p>
	 * Prefers 32-bit BGRA in the sRGB non-linear colour space, which is what a display expects,
	 * and otherwise falls back to whatever the surface offers first.
	 *
	 * @return the chosen format and colour space
	 * @throws RuntimeException if the surface advertises no formats at all
	 */
	protected SurfaceFormat calcSurfaceFormat()
	{
		int imageFormat;
		int colorSpace;
		try (var stack = MemoryStack.stackPush())
		{
			IntBuffer ip = stack.mallocInt(1);
			VulkanUtils.vkCheck(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
							this.vulkanModulePhysicalDevice.getVkPhysicalDevice(), this.getVkSurface(), ip, null),
					"Failed to get the number surface formats");
			int numFormats = ip.get(0);
			if (numFormats <= 0)
			{
				throw new RuntimeException("No surface formats retrieved");
			}

			var surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
			VulkanUtils.vkCheck(
					KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
							this.vulkanModulePhysicalDevice.getVkPhysicalDevice(),
							this.getVkSurface(), ip, surfaceFormats), "Failed to get surface formats");

			imageFormat = VK13.VK_FORMAT_B8G8R8A8_SRGB;
			colorSpace = surfaceFormats.get(0).colorSpace();
			for (int i = 0; i < numFormats; i++)
			{
				VkSurfaceFormatKHR surfaceFormatKHR = surfaceFormats.get(i);
				if (surfaceFormatKHR.format() == VK13.VK_FORMAT_B8G8R8A8_SRGB &&
						surfaceFormatKHR.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				{
					imageFormat = surfaceFormatKHR.format();
					colorSpace = surfaceFormatKHR.colorSpace();
					break;
				}
			}
		}
		return new SurfaceFormat(imageFormat, colorSpace);
	}

	/**
	 * Returns the underlying Vulkan handle.
	 *
	 * @return the {@code VkSurfaceKHR} handle
	 */
	public abstract long getVkSurface();

	/**
	 * A pixel format paired with the colour space it is interpreted in.
	 *
	 * @param imageFormat the Vulkan image format
	 * @param colorSpace  the Vulkan colour space the format is presented in
	 */
	public record SurfaceFormat(int imageFormat, int colorSpace)
	{
	}
}

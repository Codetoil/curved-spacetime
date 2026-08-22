/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br>
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

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;
import java.util.logging.Logger;

/**
 * A GLFW window configured for Vulkan rather than OpenGL.
 * <p>
 * Supplies the two API-specific pieces {@link GLFWRenderModuleWindow} leaves open: checking that a
 * Vulkan driver is present, and setting {@code GLFW_CLIENT_API} to {@code GLFW_NO_API} so GLFW does
 * not create an OpenGL context for a window Vulkan will render into.
 */
public class VulkanGLFWRenderModuleWindow extends GLFWRenderModuleWindow
{
	/**
	 * Creates a Vulkan-capable GLFW window.
	 * <p>
	 * Nothing is opened until {@link #init()} is called.
	 *
	 * @param mainModuleEngine the engine this window belongs to
	 * @param title            the window's title bar text
	 * @param logger           the logger to write window diagnostics to
	 */
	public VulkanGLFWRenderModuleWindow(MainModuleEngine mainModuleEngine, String title, Logger logger)
	{
		super(mainModuleEngine, title, logger);
	}

	@Override
	public boolean doesDriverExist()
	{
		return GLFWVulkan.glfwVulkanSupported();
	}

	@Override
	protected void throwDriverNotFoundException()
	{
		throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
	}

	@Override
	protected void setWindowHints()
	{
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
	}
}

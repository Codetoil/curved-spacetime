/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity. <br> Copyright (C)
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

package io.codetoil.curved_spacetime.render.glfw;

import glfw3.GLFW;
import glfw3.GLFWerrorfun;
import glfw3.GLFWframebuffersizefun;
import glfw3.GLFWvidmode;
import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.logging.Logger;

public abstract class GLFWRenderModuleWindow extends RenderModuleWindow
{
	protected MemorySegment window;
	protected int width;
	protected int height;

	protected GLFWRenderModuleWindow(MainModuleEngine mainModuleEngine, String title, Logger logger)
	{
		super(mainModuleEngine, title, logger);
	}

	public void init()
	{
		GLFW.glfwSetErrorCallback(GLFWerrorfun.allocate((int error_code, MemorySegment s_description) ->
		{
			String description = s_description.getString(0);
			this.logger.severe("GLFW error code " + error_code + ": " + description);
		}, MainModuleEngine.getInstance().nativeAllocator));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (GLFW.glfwInit() != GLFW.GLFW_TRUE()) throw new IllegalStateException("Unable to initialize GLFW");

		this.logger.info("Using GLFW bindings generated via " +
				"`jextract glfw3.h -t glfw3 --symbols-class-name GLFWsymbols --header-class-name GLFW` from v" +
				GLFW.GLFW_VERSION_MAJOR() + "." + GLFW.GLFW_VERSION_MINOR() + "." + GLFW.GLFW_VERSION_REVISION());
		MemorySegment major = MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT);
		MemorySegment minor = MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT);
		MemorySegment revision = MainModuleEngine.getInstance().nativeAllocator.allocate(ValueLayout.JAVA_INT);
		GLFW.glfwGetVersion(major, minor, revision);
		this.logger.info("Using GLFW Runtime v" + major.get(ValueLayout.JAVA_INT, 0) + "." +
				minor.get(ValueLayout.JAVA_INT, 0) + ", " + revision.get(ValueLayout.JAVA_INT, 0));

		MemorySegment vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		assert vidMode != null;
		this.width = GLFWvidmode.width(vidMode);
		this.height = GLFWvidmode.height(vidMode);

		if (!this.doesDriverExist())
		{
			throwDriverNotFoundException();
		}

		// Configure GLFW
		setWindowHints();

		// Create the window
		this.window =
				GLFW.glfwCreateWindow(this.width, this.height,
						MainModuleEngine.getInstance().nativeAllocator.allocateFrom(this.title), null, null);
		if (this.window == null) throw new RuntimeException("Failed to create the GLFW window");

		this.renderModuleKeyboardInput = new GLFWRenderModuleKeyboardInput(this);
		GLFW.glfwSetFramebufferSizeCallback(this.window,
				GLFWframebuffersizefun.allocate((MemorySegment _, int width, int height) ->
				{
					this.width = width;
					this.height = height;
				}, MainModuleEngine.getInstance().nativeAllocator));

		this.renderModuleMouseInput = new GLFWRenderModuleMouseInput(this);
	}

	public void loop()
	{
		// Poll for window events. The key callback above will only be
		// invoked during this call.
		this.pollEvents();
		if (this.shouldClose() == GLFW.GLFW_TRUE())
		{
			this.mainModuleEngine.stop();
		}
	}

	public int getHeight()
	{
		return this.height;
	}

	public int getWidth()
	{
		return this.width;
	}

	public void setShouldClose()
	{
		GLFW.glfwSetWindowShouldClose(this.window, GLFW.GLFW_TRUE());
	}

	public int shouldClose()
	{
		return GLFW.glfwWindowShouldClose(this.window);
	}

	public void clean()
	{
		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
	}

	public abstract boolean doesDriverExist();

	protected abstract void throwDriverNotFoundException();

	protected abstract void setWindowHints();

	public MemorySegment getWindow()
	{
		return this.window;
	}
}

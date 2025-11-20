/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity. <br> Copyright (C) 2023-2025 Anthony
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

package io.codetoil.curved_spacetime.api.glfw;

import io.codetoil.curved_spacetime.api.Window;
import io.codetoil.curved_spacetime.api.engine.Engine;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

public abstract class GLFWWindow extends Window
{
	protected long windowHandle;
	protected int width;
	protected int height;

	protected GLFWWindow(Engine engine, String title)
	{
		super(engine, title);
	}

	public void init()
	{
		// Set up an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		assert vidMode != null;
		this.width = vidMode.width();
		this.height = vidMode.height();

		if (!this.doesDriverExist())
		{
			throwDriverNotFoundException();
		}

		// Configure GLFW
		setWindowHints();

		// Create the window
		this.windowHandle =
				GLFW.glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
		if (this.windowHandle == MemoryUtil.NULL) throw new RuntimeException("Failed to create the GLFW window");

		this.keyboardInput = new GLFWKeyboardInput(this);
		GLFW.glfwSetFramebufferSizeCallback(this.windowHandle, (window, w, h) -> {
			width = w;
			height = h;
		});

		this.mouseInput = new GLFWMouseInput(this);
	}

	public void loop()
	{
		// Poll for window events. The key callback above will only be
		// invoked during this call.
		this.pollEvents();
		if (this.shouldClose())
		{
			this.engine.stop();
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
		GLFW.glfwSetWindowShouldClose(this.windowHandle, true);
	}

	public boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(this.windowHandle);
	}

	public void clean()
	{
		Callbacks.glfwFreeCallbacks(this.windowHandle);
		GLFW.glfwDestroyWindow(this.windowHandle);
		GLFW.glfwTerminate();
	}

	public abstract boolean doesDriverExist();

	protected abstract void throwDriverNotFoundException();

	protected abstract void setWindowHints();

	public long getWindowHandle()
	{
		return this.windowHandle;
	}
}

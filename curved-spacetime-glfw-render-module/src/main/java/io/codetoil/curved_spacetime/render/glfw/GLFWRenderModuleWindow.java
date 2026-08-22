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

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import java.util.logging.Logger;

/**
 * A window backed by GLFW, sized to the primary monitor.
 * <p>
 * Handles the parts of window management that are the same whichever graphics API is being driven:
 * initialising GLFW, opening the window, wiring up input, and tearing it all down. Subclasses
 * supply the API-specific pieces — which window hints to set, and how to check that a usable
 * driver is present.
 * <p>
 * Closing the window stops the engine, so the window's lifetime is effectively the program's.<p>
 * This will be changed in a future build, as to allow no-window and multi-window setups.
 */
public abstract class GLFWRenderModuleWindow extends RenderModuleWindow
{
	/**
	 * The GLFW window handle, valid between {@link #init()} and {@link #clean()}.
	 */
	protected long windowHandle;

	/**
	 * The window's width, kept current by GLFW's framebuffer size callback.
	 */
	protected int width;

	/**
	 * The window's height, kept current by GLFW's framebuffer size callback.
	 */
	protected int height;

	/**
	 * Creates a GLFW window.
	 * <p>
	 * Nothing is opened until {@link #init()} is called.
	 *
	 * @param mainModuleEngine the engine this window belongs to
	 * @param title            the window's title bar text
	 * @param logger           the logger to write window diagnostics to
	 */
	protected GLFWRenderModuleWindow(MainModuleEngine mainModuleEngine, String title, Logger logger)
	{
		super(mainModuleEngine, title, logger);
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

		this.renderModuleKeyboardInput = new GLFWRenderModuleKeyboardInput(this);
		GLFW.glfwSetFramebufferSizeCallback(this.windowHandle, (window, w, h) -> {
			width = w;
			height = h;
		});

		this.renderModuleMouseInput = new GLFWRenderModuleMouseInput(this);
	}

	public void loop()
	{
		// Poll for window events. The key callback above will only be
		// invoked during this call.
		this.pollEvents();
		if (this.shouldClose())
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

	/**
	 * Returns whether a driver for the subclass's graphics API is present.
	 * <p>
	 * Checked during {@link #init()}, before the window is created, so that an unusable
	 * environment is reported before any resource is acquired.
	 *
	 * @return {@code true} if the graphics API can be used on this machine
	 */
	public abstract boolean doesDriverExist();

	/**
	 * Fails with a message naming the graphics API that could not be found.
	 * <p>
	 * Called when {@link #doesDriverExist()} returns {@code false}; implementations are expected
	 * to throw rather than return.
	 */
	protected abstract void throwDriverNotFoundException();

	/**
	 * Applies the GLFW window hints the subclass's graphics API requires.
	 * <p>
	 * Called before the window is created. A Vulkan implementation, for example, sets
	 * {@code GLFW_CLIENT_API} to {@code GLFW_NO_API} so GLFW does not create an OpenGL context.
	 */
	protected abstract void setWindowHints();

	/**
	 * Returns the underlying GLFW window handle.
	 * <p>
	 * Needed to create a rendering surface against this window.
	 *
	 * @return the GLFW window handle, or {@code 0} before {@link #init()}
	 */
	public long getWindowHandle()
	{
		return this.windowHandle;
	}
}

/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br>
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

import io.codetoil.curved_spacetime.render.RenderModuleMouseInput;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;
import org.lwjgl.glfw.GLFW;

/**
 * Mouse input backed by GLFW.
 * <p>
 * Position and button state arrive through GLFW callbacks; the deltas are computed in
 * {@link #poll()} by differencing against the previous frame's position. Movement is only
 * accumulated while the cursor is inside the window, so re-entering does not produce a large
 * spurious delta from wherever the cursor left.
 */
public class GLFWRenderModuleMouseInput implements RenderModuleMouseInput
{
	/**
	 * The window this input reads from.
	 */
	protected final GLFWRenderModuleWindow window;

	/**
	 * The cursor's current x position, or {@code -1.0f} before the first movement.
	 */
	protected float currentX = -1.0f;

	/**
	 * The cursor's current y position, or {@code -1.0f} before the first movement.
	 */
	protected float currentY = -1.0f;

	/**
	 * Horizontal movement since the previous {@link #poll()}.
	 */
	protected float deltaX = 0.0f;

	/**
	 * Vertical movement since the previous {@link #poll()}.
	 */
	protected float deltaY = 0.0f;

	/**
	 * The cursor's x position at the previous {@link #poll()}.
	 */
	protected float previousX = 0.0f;

	/**
	 * The cursor's y position at the previous {@link #poll()}.
	 */
	protected float previousY = 0.0f;

	/**
	 * Whether the cursor is currently inside the window.
	 */
	protected boolean inWindow = false;

	/**
	 * Whether the left button is held.
	 */
	protected boolean leftButtonPressed = false;

	/**
	 * Whether the right button is held.
	 */
	protected boolean rightButtonPressed = false;

	/**
	 * Whether the middle button is held.
	 */
	protected boolean middleButtonPressed = false;

	/**
	 * Attaches mouse handling to the given window.
	 * <p>
	 * Installs GLFW cursor position, cursor enter, and mouse button callbacks, replacing any
	 * previously set on that window.
	 *
	 * @param window the window to read mouse input from
	 */
	public GLFWRenderModuleMouseInput(GLFWRenderModuleWindow window)
	{
		this.window = window;
		GLFW.glfwSetCursorPosCallback(this.window.windowHandle, (handle, xpos, ypos) -> {
			this.currentX = (float) xpos;
			this.currentY = (float) ypos;
		});
		GLFW.glfwSetCursorEnterCallback(this.window.windowHandle, (handle, entered) -> inWindow = entered);
		GLFW.glfwSetMouseButtonCallback(this.window.windowHandle, (handle, button, action, mode) -> {
			leftButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
			rightButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS;
			leftButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
			middleButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_3 && action == GLFW.GLFW_PRESS;
		});
	}

	@Override
	public RenderModuleWindow window()
	{
		return this.window;
	}

	@Override
	public void poll()
	{
		this.deltaX = 0.0f;
		this.deltaY = 0.0f;
		if (previousX >= 0.0f && previousY >= 0.0f && inWindow)
		{
			this.deltaX = currentX - previousX;
			this.deltaY = currentY - previousY;
		}
		this.previousX = currentX;
		this.previousY = currentY;
	}

	@Override
	public float getCurrentX()
	{
		return currentX;
	}

	@Override
	public float getCurrentY()
	{
		return currentY;
	}

	@Override
	public float getDeltaX()
	{
		return deltaX;
	}

	@Override
	public float getDeltaY()
	{
		return deltaY;
	}

	@Override
	public boolean isLeftButtonPressed()
	{
		return leftButtonPressed;
	}

	@Override
	public boolean isRightButtonPressed()
	{
		return rightButtonPressed;
	}

	@Override
	public boolean isMiddleButtonPressed()
	{
		return middleButtonPressed;
	}
}

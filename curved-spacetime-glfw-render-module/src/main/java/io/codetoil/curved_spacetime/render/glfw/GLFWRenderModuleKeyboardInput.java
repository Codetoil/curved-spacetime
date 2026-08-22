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

import io.codetoil.curved_spacetime.render.RenderModuleKeyboardInput;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keyboard input backed by GLFW.
 * <p>
 * Held state and tapped state come from different places: {@link #keyPressed} asks GLFW directly,
 * while {@link #keyTapped} reads a map this class maintains from GLFW's key callback, since a tap
 * is an edge that polling would miss. That map is cleared each frame by {@link #clean()}, so a tap
 * is reported for exactly one frame.
 */
public class GLFWRenderModuleKeyboardInput implements RenderModuleKeyboardInput, GLFWKeyCallbackI
{
	/**
	 * Which keys went down since the last {@link #clean()}, keyed by GLFW key code.
	 */
	protected final Map<Integer, Boolean> tappedKeyMap;

	/**
	 * The window this input reads from.
	 */
	protected final GLFWRenderModuleWindow window;

	/**
	 * Listeners notified whenever a key changes state.
	 */
	protected final List<KeyCallback> callbacks;

	/**
	 * Attaches keyboard handling to the given window.
	 * <p>
	 * Installs a GLFW key callback, replacing any previously set on that window.
	 *
	 * @param window the window to read keyboard input from
	 */
	public GLFWRenderModuleKeyboardInput(GLFWRenderModuleWindow window)
	{
		this.window = window;
		tappedKeyMap = new HashMap<>();
		GLFW.glfwSetKeyCallback(this.window.windowHandle, this);
		callbacks = new ArrayList<>();
	}

	@Override
	public RenderModuleWindow window()
	{
		return this.window;
	}

	@Override
	public void addKeyCallBack(KeyCallback callback)
	{
		callbacks.add(callback);
	}

	@Override
	public void poll()
	{
		GLFW.glfwPollEvents();
	}

	@Override
	public void clean()
	{
		tappedKeyMap.clear();
	}

	@Override
	public boolean keyPressed(RenderModuleKeyboardInput.KeyCtx keyCtx)
	{
		return GLFW.glfwGetKey(this.window.windowHandle, ((KeyCtx) keyCtx).keycode()) == GLFW.GLFW_PRESS;
	}

	@Override
	public boolean keyTapped(RenderModuleKeyboardInput.KeyCtx keyCtx)
	{
		Boolean value = tappedKeyMap.get(((KeyCtx) keyCtx).keycode());
		return value != null && value;
	}

	/**
	 * Records a key change from GLFW and notifies the registered callbacks.
	 * <p>
	 * Events for other windows are ignored, since a GLFW callback is shared across the process.
	 *
	 * @param handle   the GLFW window the event came from
	 * @param keyCode  the GLFW key code
	 * @param scanCode the platform-specific scancode
	 * @param action   the GLFW action, such as {@code GLFW_PRESS} or {@code GLFW_RELEASE}
	 * @param mods     the modifier key bitmask in effect
	 */
	@Override
	public void invoke(long handle, int keyCode, int scanCode, int action, int mods)
	{
		if (handle != this.window.windowHandle) return;
		tappedKeyMap.put(keyCode, action == GLFW.GLFW_PRESS);
		for (KeyCallback callback : callbacks)
		{
			callback.invoke(new KeyCtx(keyCode, scanCode, action, mods));
		}
	}

	/**
	 * A key identified the way GLFW reports it.
	 *
	 * @param keycode  the GLFW key code, which is layout-independent
	 * @param scanCode the platform-specific scancode
	 * @param action   the GLFW action that produced this event
	 * @param mods     the modifier key bitmask in effect
	 */
	public record KeyCtx(int keycode, int scanCode, int action, int mods) implements RenderModuleKeyboardInput.KeyCtx
	{

	}
}

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

package io.codetoil.curved_spacetime.render;

public abstract class RenderModuleWindow
{
	protected final String title;
	protected RenderModuleKeyboardInput keyboardInput;
	protected RenderModuleMouseInput mouseInput;

	protected RenderModuleWindow(String title)
	{
		this.title = title;
	}

	public abstract void init();

	public abstract void loop();

	public abstract int getHeight();

	public abstract int getWidth();

	public abstract void setShouldClose();

	public abstract boolean shouldClose();

	public abstract void clean();

	public RenderModuleKeyboardInput getKeyboardInput()
	{
		return keyboardInput;
	}

	public RenderModuleMouseInput getMouseInput()
	{
		return mouseInput;
	}

	public void pollEvents()
	{
		keyboardInput.poll();
		mouseInput.poll();
	}

	public void resetInput()
	{
		keyboardInput.clean();
	}
}

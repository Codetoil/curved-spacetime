/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.api;

import io.codetoil.curved_spacetime.api.engine.Engine;

public abstract class Window
{
	protected final Engine engine;
	protected final String title;
	protected KeyboardInput keyboardInput;
	protected MouseInput mouseInput;

	protected Window(Engine engine, String title)
	{
		this.engine = engine;
		this.title = title;
	}

	public abstract void init();

	public abstract void loop();

	public abstract int getHeight();

	public abstract int getWidth();

	public abstract void setShouldClose();

	public abstract boolean shouldClose();

	public abstract void clean();

	public KeyboardInput getKeyboardInput()
	{
		return keyboardInput;
	}

	public MouseInput getMouseInput()
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

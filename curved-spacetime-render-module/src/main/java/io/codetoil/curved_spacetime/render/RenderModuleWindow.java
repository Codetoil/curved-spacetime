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

import io.codetoil.curved_spacetime.MainModuleEngine;

import java.util.logging.Logger;

/**
 * A window, and the keyboard and mouse attached to it, independent of the windowing library.
 * <p>
 * Implementations supply the platform behaviour; this class holds only what every window has and
 * the input plumbing that goes with it. An implementation is expected to create its input objects
 * during {@link #init()}, since {@link #pollEvents()} dereferences them.
 */
public abstract class RenderModuleWindow
{
	/**
	 * The engine this window belongs to.
	 */
	protected final MainModuleEngine mainModuleEngine;

	/**
	 * The window's title bar text.
	 */
	protected final String title;

	/**
	 * The logger this window writes its diagnostics to.
	 */
	protected final Logger logger;

	/**
	 * Keyboard state for this window, created during {@link #init()}.
	 */
	protected RenderModuleKeyboardInput renderModuleKeyboardInput;

	/**
	 * Mouse state for this window, created during {@link #init()}.
	 */
	protected RenderModuleMouseInput renderModuleMouseInput;

	/**
	 * Creates a window bound to the given engine.
	 * <p>
	 * Nothing is opened until {@link #init()} is called.
	 *
	 * @param mainModuleEngine the engine this window belongs to
	 * @param title            the window's title bar text
	 * @param logger           the logger to write window diagnostics to
	 */
	protected RenderModuleWindow(MainModuleEngine mainModuleEngine, String title, Logger logger)
	{
		this.mainModuleEngine = mainModuleEngine;
		this.title = title;
		this.logger = logger;
	}

	/**
	 * Opens the window and creates its input objects.
	 */
	public abstract void init();

	/**
	 * Advances the window by one frame, sampling input and handling a close request.
	 */
	public abstract void loop();

	/**
	 * Returns the window's height.
	 *
	 * @return the current height in pixels
	 */
	public abstract int getHeight();

	/**
	 * Returns the window's width.
	 *
	 * @return the current width in pixels
	 */
	public abstract int getWidth();

	/**
	 * Asks the window to close at the next opportunity.
	 */
	public abstract void setShouldClose();

	/**
	 * Returns whether the window has been asked to close.
	 *
	 * @return {@code true} once a close has been requested, by this class or by the user
	 */
	public abstract boolean shouldClose();

	/**
	 * Destroys the window and releases its platform resources.
	 */
	public abstract void clean();

	/**
	 * Returns keyboard state for this window.
	 *
	 * @return the keyboard input, or {@code null} before {@link #init()}
	 */
	public RenderModuleKeyboardInput getKeyboardInput()
	{
		return renderModuleKeyboardInput;
	}

	/**
	 * Returns mouse state for this window.
	 *
	 * @return the mouse input, or {@code null} before {@link #init()}
	 */
	public RenderModuleMouseInput getMouseInput()
	{
		return renderModuleMouseInput;
	}

	/**
	 * Samples the keyboard and mouse for this frame.
	 */
	public void pollEvents()
	{
		renderModuleKeyboardInput.poll();
		renderModuleMouseInput.poll();
	}

	/**
	 * Clears per-frame keyboard state, so tapped keys are not reported twice.
	 */
	public void resetInput()
	{
		renderModuleKeyboardInput.clean();
	}
}

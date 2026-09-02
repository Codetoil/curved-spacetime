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

package io.codetoil.curved_spacetime.render;

/**
 * Mouse state for one window, independent of the windowing library providing it.
 * <p>
 * As with the keyboard, state is sampled rather than delivered: {@link #poll()} captures the
 * mouse once per frame and the query methods answer against that snapshot. The deltas are the
 * movement since the previous sample, which is what a camera wants, rather than absolute motion.
 */
public interface RenderModuleMouseInput
{
	/**
	 * Returns the window this input reads from.
	 *
	 * @return the owning window
	 */
	RenderModuleWindow window();

	/**
	 * Samples the mouse, refreshing position, deltas, and button state.
	 * <p>
	 * Called once per frame by the owning window.
	 */
	void poll();

	/**
	 * Returns the cursor's horizontal position within the window.
	 *
	 * @return the current x coordinate in pixels from the left edge
	 */
	float getCurrentX();

	/**
	 * Returns the cursor's vertical position within the window.
	 *
	 * @return the current y coordinate in pixels from the top edge
	 */
	float getCurrentY();

	/**
	 * Returns how far the cursor moved horizontally since the previous sample.
	 *
	 * @return the change in x since the last {@link #poll()}, in pixels
	 */
	float getDeltaX();

	/**
	 * Returns how far the cursor moved vertically since the previous sample.
	 *
	 * @return the change in y since the last {@link #poll()}, in pixels
	 */
	float getDeltaY();

	/**
	 * Returns whether the left button is held down.
	 *
	 * @return {@code true} while the left button is held
	 */
	boolean isLeftButtonPressed();

	/**
	 * Returns whether the right button is held down.
	 *
	 * @return {@code true} while the right button is held
	 */
	boolean isRightButtonPressed();

	/**
	 * Returns whether the middle button is held down.
	 *
	 * @return {@code true} while the middle button is held
	 */
	boolean isMiddleButtonPressed();
}

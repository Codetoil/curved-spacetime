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
 * Keyboard state for one window, independent of the windowing library providing it.
 * <p>
 * State is sampled rather than delivered: {@link #poll()} captures the keyboard once per frame,
 * and the query methods answer against that snapshot, so every caller within a frame sees the
 * same input regardless of when it asks.
 */
public interface RenderModuleKeyboardInput
{
	/**
	 * Returns the window this input reads from.
	 *
	 * @return the owning window
	 */
	RenderModuleWindow window();

	/**
	 * Registers a callback invoked when a key changes state.
	 *
	 * @param callback the callback to invoke
	 */
	void addKeyCallBack(KeyCallback callback);

	/**
	 * Samples the keyboard, refreshing what the query methods report.
	 * <p>
	 * Called once per frame by the owning window.
	 */
	void poll();

	/**
	 * Clears per-frame state, so that tapped keys are not reported twice.
	 */
	void clean();

	/**
	 * Returns whether a key is currently held down.
	 *
	 * @param keyCtx the key to test
	 * @return {@code true} while the key is held
	 */
	boolean keyPressed(KeyCtx keyCtx);

	/**
	 * Returns whether a key went down during the frame just sampled.
	 * <p>
	 * Unlike {@link #keyPressed}, this is true for a single frame per press.
	 *
	 * @param keyCtx the key to test
	 * @return {@code true} if the key was newly pressed this frame
	 */
	boolean keyTapped(KeyCtx keyCtx);

	/**
	 * A listener notified when a key changes state.
	 */
	interface KeyCallback
	{
		/**
		 * Handles a key changing state.
		 *
		 * @param keyCtx the key that changed
		 */
		void invoke(KeyCtx keyCtx);
	}

	/**
	 * Identifies a key.
	 * <p>
	 * Deliberately opaque: key codes are defined by the windowing library, so an implementation
	 * supplies its own representation rather than this module imposing one.
	 */
	interface KeyCtx
	{

	}
}

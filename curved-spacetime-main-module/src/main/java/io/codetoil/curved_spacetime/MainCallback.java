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

package io.codetoil.curved_spacetime;

/**
 * Work the engine runs once at start-up and then repeatedly, independent of any scene.
 * <p>
 * A module registers one of these with
 * {@link MainModuleEngine#registerMainCallback(MainCallback)}. The engine calls {@link #init()}
 * once, then {@link #loop()} at the configured frame rate, and {@link #clean()} on shutdown. All
 * three run on the engine's single callback thread, so an implementation may hold mutable state
 * without synchronising it against the other callbacks.
 */
public abstract class MainCallback
{
	/**
	 * The engine this callback belongs to.
	 */
	protected final MainModuleEngine mainModuleEngine;

	/**
	 * Whether this callback has finished setting itself up.
	 * <p>
	 * Deliberately not maintained by this class. An implementation sets this to {@code true} from
	 * its own {@link #init()}, at the point where its setup is genuinely complete — only the
	 * implementation knows when that is.
	 */
	protected boolean initialized = false;

	/**
	 * Creates a callback bound to the given engine.
	 *
	 * @param mainModuleEngine the engine this callback belongs to
	 */
	protected MainCallback(MainModuleEngine mainModuleEngine)
	{
		this.mainModuleEngine = mainModuleEngine;
	}

	/**
	 * Returns whether this callback has finished setting itself up.
	 *
	 * @return {@code true} once the implementation has flagged its own setup complete
	 */
	public boolean isInitialized()
	{
		return this.initialized;
	}

	/**
	 * Sets this callback up.
	 * <p>
	 * Called once, before the first {@link #loop()}. An implementation should set
	 * {@link #initialized} when it finishes.
	 */
	public abstract void init();

	/**
	 * Advances this callback by one frame.
	 * <p>
	 * Called repeatedly at the frame rate the engine's configuration specifies.
	 */
	public abstract void loop();

	/**
	 * Releases whatever this callback acquired in {@link #init()}.
	 */
	public abstract void clean();
}

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

package io.codetoil.curved_spacetime.scene;

import io.codetoil.curved_spacetime.MainModuleEngine;

/**
 * Work the engine runs once at start-up and then repeatedly, against one particular scene.
 * <p>
 * A module does not register these directly. It registers a generator with
 * {@link MainModuleEngine#registerSceneCallbackGenerator}, and the engine applies that generator
 * to every scene, so a module that wants to act on scenes gets one callback per scene without
 * having to track scenes itself.
 * <p>
 * The engine calls {@link #init()} once, then {@link #loop()} at the configured frame rate, and
 * {@link #clean()} on shutdown, all on its single callback thread.
 */
public abstract class SceneCallback
{
	/**
	 * The engine this callback belongs to.
	 */
	protected final MainModuleEngine mainModuleEngine;

	/**
	 * The scene this callback acts on.
	 */
	protected final Scene scene;

	/**
	 * Whether this callback has finished setting itself up.
	 * <p>
	 * Deliberately not maintained by this class. An implementation sets this to {@code true} from
	 * its own {@link #init()}, at the point where its setup is genuinely complete — only the
	 * implementation knows when that is.
	 */
	protected boolean initialized = false;

	/**
	 * Creates a callback bound to the given engine and scene.
	 *
	 * @param mainModuleEngine the engine this callback belongs to
	 * @param scene            the scene this callback acts on
	 */
	protected SceneCallback(MainModuleEngine mainModuleEngine, Scene scene)
	{
		this.mainModuleEngine = mainModuleEngine;
		this.scene = scene;
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
	 * Sets this callback up against its scene.
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

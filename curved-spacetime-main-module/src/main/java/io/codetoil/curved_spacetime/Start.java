/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2026
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

package io.codetoil.curved_spacetime;

import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;

/**
 * The common entry point every variant of Curved Spacetime starts through.
 * <p>
 * Each variant reaches this class differently but arrives at the same place. The closed-world
 * variants call {@link #start} directly from their {@code Main}; the Quilt variant loads this
 * class reflectively and invokes {@code start} as a static method, which is why the signature
 * below is part of the game provider's contract and must not change without updating
 * {@code CurvedSpacetimeGameProvider}.
 */
public class Start
{
	/**
	 * Prevents instantiation; this class is only ever used statically.
	 */
	private Start()
	{
		// Utility class
	}

	/**
	 * Starts the engine against the given loader.
	 * <p>
	 * Construction of the engine is what drives module initialization, so this returns only once
	 * every module's {@code main} entrypoint has run.
	 *
	 * @param args   the command-line arguments the variant was launched with
	 * @param loader the loader that will resolve module entrypoints
	 */
	public static void start(String[] args, CurvedSpacetimeLoader loader)
	{
		new MainModuleEngine(loader);
		// TODO implement argument handling
	}
}

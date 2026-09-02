/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2025-2026 Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.Start;

import java.util.logging.Logger;

/**
 * The launcher for both closed-world variants.
 * <p>
 * Named as the {@code Main-Class} of the shadow jar and as the native image's entry point, so this
 * is where both the closed-world jar and the native binary begin. It does nothing but hand a
 * {@link CurvedSpacetimeLoaderClosedLoader} to {@link Start}; the Quilt variant reaches the same
 * place by a different route.
 */
public class Main
{
	/**
	 * Prevents instantiation; this class is only ever used statically.
	 */
	private Main()
	{
		// Utility class
	}

	/**
	 * Starts the closed-world variant.
	 *
	 * @param args the command-line arguments the program was launched with
	 */
	static void main(String[] args)
	{
		Logger.getGlobal().info("Starting closed-world version of Engine!");
		Start.start(args, new CurvedSpacetimeLoaderClosedLoader());
	}
}

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

package io.codetoil.curved_spacetime.loader;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Discovers modules and resolves their entrypoints on the engine's behalf.
 * <p>
 * An implementation is free to resolve entrypoints however it likes. The closed-world loader
 * holds them in static tables so that GraalVM native-image can reach them by ordinary static
 * analysis; the Quilt loader delegates to Quilt Loader, which discovers them dynamically. Neither
 * strategy is privileged by this interface.
 * <p>
 * Implementations must satisfy requirements R6 through R11 of the Module System Specification.
 */
public interface CurvedSpacetimeLoader
{
	/**
	 * Prepares this loader for module initialization.
	 * <p>
	 * The engine calls this exactly once, before invoking any entrypoint. An implementation must
	 * retain {@code engine} such that {@link #getEngine()} returns it thereafter.
	 *
	 * @param path   the run directory modules resolve their resources against
	 * @param engine the engine driving initialization
	 */
	void prepareModInit(Path path, Object engine);

	/**
	 * Returns every entrypoint registered under the given name whose runtime type is assignable to
	 * {@code moduleInitializerClass}.
	 * <p>
	 * An implementation returns the same instances across repeated calls within one run. The
	 * dependency handshake relies on that: a dependent locates its own main entrypoint by filtering
	 * this list, then transfers to the queue owned by that exact object.
	 *
	 * @param <E>                    the entrypoint type
	 * @param name                   the entrypoint name, such as {@code main}
	 * @param moduleInitializerClass the type registered entrypoints must be assignable to
	 * @return the matching entrypoints, empty when the name is recognised but nothing is registered
	 * @throws IllegalArgumentException if the entrypoint name is not recognised
	 */
	<E> List<E> getEntrypoints(String name, Class<E> moduleInitializerClass);

	/**
	 * Applies the given consumer to every entrypoint {@link #getEntrypoints} would return for the
	 * same arguments.
	 * <p>
	 * No ordering is imposed, and callers must not depend on one.
	 *
	 * @param <E>                       the entrypoint type
	 * @param name                      the entrypoint name, such as {@code main}
	 * @param moduleInitializerClass    the type registered entrypoints must be assignable to
	 * @param moduleInitializerConsumer the action to apply to each entrypoint
	 * @throws IllegalArgumentException if the entrypoint name is not recognised
	 */
	<E> void invokeEntrypoints(String name, Class<E> moduleInitializerClass,
							   Consumer<? super E> moduleInitializerConsumer);

	/**
	 * Returns the engine this loader was prepared with.
	 *
	 * @return the engine passed to {@link #prepareModInit}, or {@code null} before that call
	 */
	Object getEngine();
}

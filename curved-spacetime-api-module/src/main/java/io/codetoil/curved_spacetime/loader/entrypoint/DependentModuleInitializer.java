/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025-2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <a href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */
package io.codetoil.curved_spacetime.loader.entrypoint;

import io.codetoil.curved_spacetime.api.Engine;

/**
 * The callback by which one module learns of another module it depends on.
 * <p>
 * Each module extends this with a named interface of its own, and any module depending on that module registers an
 * implementation of it. When the depended-on module reaches the last step of its initialization, it invokes every such
 * implementation, passing itself and the engine. The implementation's job is to hand that entrypoint to its own module,
 * by transferring it into that module's {@link MainModuleInitializer#getDependencyModuleTransferQueue() dependency queue}:
 * <pre>{@code
 * engine.getCurvedSpacetimeLoader()
 *         .getEntrypoints("main", MainModuleInitializer.class).stream()
 *         .filter(MainMyModuleEntrypoint.class::isInstance)
 *         .findFirst().orElseThrow()
 *         .getDependencyModuleTransferQueue().transfer(moduleEntrypoint);
 * }</pre>
 * <p>
 * Implementations must satisfy requirements R17 through R19 and R25.
 *
 * @param <E>
 * 		the entrypoint type of the module being depended on
 */
public interface DependentModuleInitializer<E extends MainModuleInitializer>
{
	/**
	 * Receives the entrypoint of a module this module depends on.
	 * <p>
	 * Called on the depended-on module's initialization thread, not this module's.
	 *
	 * @param moduleEntrypoint
	 * 		the entrypoint of the module being depended on
	 * @param engine
	 * 		the engine driving initialization
	 */
	void onInitialize(E moduleEntrypoint, Engine engine);
}

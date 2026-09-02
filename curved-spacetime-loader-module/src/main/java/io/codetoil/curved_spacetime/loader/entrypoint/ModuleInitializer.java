/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright 2022, 2023 QuiltMC<br>
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

package io.codetoil.curved_spacetime.loader.entrypoint;

import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

/**
 * A module's {@code main} entrypoint, the object the engine drives during initialization.
 * <p>
 * Every module registers exactly one of these. The engine invokes {@link #onInitialize()} on all
 * of them concurrently, so a module must not assume anything about the order in which its peers
 * come up; the only ordering guarantee available is the one the dependency handshake provides.
 * <p>
 * Implementations must satisfy requirements R12 through R16 of the Module System Specification.
 */
public interface ModuleInitializer
{
	/**
	 * Brings this module up.
	 * <p>
	 * An implementation sets its logger's level from the engine's configuration, loads its own
	 * configuration and saves it if loading substituted defaults, receives any dependencies it
	 * declares through {@link #getDependencyModuleTransferQueue()}, performs any operations it
	 * must perform during initialization, and finally invokes its own dependent entrypoints,
	 * passing itself.
	 */
	void onInitialize();

	/**
	 * Returns this module's configuration.
	 *
	 * @return the configuration, or {@code null} before {@link #onInitialize()} has loaded it
	 */
	ModuleConfig getConfig();

	/**
	 * Returns the logger this module writes its diagnostics to.
	 * <p>
	 * The same instance is returned for the lifetime of this entrypoint.
	 *
	 * @return this module's logger
	 */
	Logger getLogger();

	/**
	 * Returns the queue by which this module receives the entrypoints it depends on.
	 * <p>
	 * A module does not look its dependencies up. Each module it depends on invokes a dependent
	 * entrypoint, which transfers that module's own entrypoint into this queue. The same instance
	 * is returned for the lifetime of this entrypoint; a fresh or defensively copied queue breaks
	 * the handshake, because the producer transfers into whichever queue this method returned.
	 *
	 * @return this module's dependency transfer queue
	 */
	TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue();
}

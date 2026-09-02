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

package io.codetoil.curved_spacetime.simulator.entrypoint;

import io.codetoil.curved_spacetime.loader.entrypoint.ModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.simulator.SimulatorModuleEntrypoint;

/**
 * The entrypoint a module registers to depend on the simulator module.
 * <p>
 * Registered under the simulator module's dependent entrypoint name. Implementations transfer the
 * received entrypoint into their own module's dependency queue.
 */
public interface SimulatorModuleDependentModuleInitializer
		extends ModuleDependentModuleInitializer<SimulatorModuleEntrypoint>
{
	/**
	 * Receives the simulator module's entrypoint.
	 * <p>
	 * Called on the simulator module's initialization thread, not the receiving module's.
	 *
	 * @param simulatorModuleEntrypoint the simulator module's main entrypoint
	 */
	void onInitialize(SimulatorModuleEntrypoint simulatorModuleEntrypoint);
}

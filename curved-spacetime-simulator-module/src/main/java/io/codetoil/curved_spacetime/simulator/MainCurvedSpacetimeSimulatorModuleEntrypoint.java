/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025-2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.simulator;

import io.codetoil.curved_spacetime.api.Engine;
import io.codetoil.curved_spacetime.loader.entrypoint.MainModuleInitializer;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.simulator.entrypoint.CurvedSpacetimeSimulatorModuleDependentModuleInitializer;

import java.io.IOException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.logging.Logger;

/**
 * The simulator module's {@code main} entrypoint.
 * <p>
 * This is where the physics will live. At present it is scaffolding only: it loads its configuration and hands itself
 * to any dependents, but nothing is simulated yet, and the simulation model — spacetimes, metrics, worldlines — has not
 * been specified.
 */
public class MainCurvedSpacetimeSimulatorModuleEntrypoint implements MainModuleInitializer
{
	private final TransferQueue<MainModuleInitializer> depdencyModuleTransferQueue = new LinkedTransferQueue<>();
	private final Logger logger = Logger.getLogger("Curved Spacetime Simulator Module Logger");
	private ModuleConfig config;
	private Engine engine;

	/**
	 * Creates the simulator module's entrypoint.
	 * <p>
	 * Called by the loader; nothing happens until {@link #onInitialize(Engine)} runs.
	 */
	public MainCurvedSpacetimeSimulatorModuleEntrypoint()
	{
	}

	@Override
	public void onInitialize(Engine engine)
	{
		this.engine = engine;
		this.logger.setLevel(this.engine.getLoggerLevel());
		this.logger.addHandler(this.engine.getConsoleHandler());
		this.logger.setUseParentHandlers(false);
		try
		{
			this.config = new CurvedSpacetimeSimulatorModuleConfig(this.logger).load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Simulator Module Config", ex);
		}
		try
		{
			this.engine.callDependents("curved_spacetime_simulator_module_dependent_module_initializer",
					CurvedSpacetimeSimulatorModuleDependentModuleInitializer.class,
					(CurvedSpacetimeSimulatorModuleDependentModuleInitializer simulatorModuleDependentModuleInitializer) ->
							simulatorModuleDependentModuleInitializer.onInitialize(this), this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public ModuleConfig getConfig()
	{
		return this.config;
	}

	@Override
	public Logger getLogger()
	{
		return this.logger;
	}

	@Override
	public TransferQueue<MainModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.depdencyModuleTransferQueue;
	}
}

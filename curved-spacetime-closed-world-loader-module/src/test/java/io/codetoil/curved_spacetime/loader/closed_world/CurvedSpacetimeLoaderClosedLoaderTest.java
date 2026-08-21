/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2026 Anthony Michalek (Codetoil)<br>
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

import io.codetoil.curved_spacetime.cli.entrypoint.CLIModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the loader contract of the Module System Specification against
 * {@link CurvedSpacetimeLoaderClosedLoader}.
 * <p>
 * Covers requirements R6 through R11: the engine reference is retained, registered entrypoints are
 * returned and type-filtered, a recognised name with no registrations yields an empty list rather
 * than {@code null}, an unrecognised name is rejected loudly, and instances are stable across
 * repeated calls.
 * <p>
 * This loader resolves entrypoints from static tables so that GraalVM native-image can reach them
 * by ordinary static analysis, which R7 explicitly permits. Nothing here touches Vulkan, GLFW, or
 * Quilt — entrypoint construction only initialises loggers and transfer queues.
 */
class CurvedSpacetimeLoaderClosedLoaderTest
{
	/**
	 * An entrypoint name the loader recognises but for which nothing is registered.
	 */
	private static final String EMPTY_ENTRYPOINT_NAME = "cli_module_dependent";

	/**
	 * An entrypoint name no loader recognises.
	 */
	private static final String UNKNOWN_ENTRYPOINT_NAME = "no_such_module_dependent";

	private CurvedSpacetimeLoaderClosedLoader loader;

	/**
	 * Creates a fresh loader for each test.
	 */
	@BeforeEach
	void setUp()
	{
		this.loader = new CurvedSpacetimeLoaderClosedLoader();
	}

	/**
	 * R6 — {@code prepareModInit} retains the engine, so that {@code getEngine()} returns it.
	 */
	@Test
	void prepareModInitRetainsTheEngine()
	{
		Object engine = new Object();

		this.loader.prepareModInit(Path.of("."), engine);

		assertSame(engine, this.loader.getEngine(),
				"getEngine() must return the engine given to prepareModInit");
	}

	/**
	 * R7 — the main entrypoints are returned, and every one implements {@link ModuleInitializer}.
	 */
	@Test
	void mainEntrypointsAreReturnedAndTypeCorrect()
	{
		List<ModuleInitializer> entrypoints = this.loader.getEntrypoints("main", ModuleInitializer.class);

		assertNotNull(entrypoints, "the main entrypoints must not be null");
		assertFalse(entrypoints.isEmpty(), "at least one main entrypoint must be registered");
		entrypoints.forEach(CurvedSpacetimeLoaderClosedLoaderTest::assertInstanceOfModuleInitializer);
	}

	/**
	 * Asserts that the given entrypoint implements {@link ModuleInitializer}.
	 *
	 * @param entrypoint the entrypoint to check
	 */
	private static void assertInstanceOfModuleInitializer(Object entrypoint)
	{
		assertInstanceOf(ModuleInitializer.class, entrypoint, () -> entrypoint + " must implement ModuleInitializer");
	}

	/**
	 * R8 — a recognised name with no registrations yields an empty list rather than {@code null}.
	 */
	@Test
	void recognisedNameWithNoRegistrationsYieldsEmptyList()
	{
		List<CLIModuleDependentModuleInitializer> entrypoints =
				this.loader.getEntrypoints(CurvedSpacetimeLoaderClosedLoaderTest.EMPTY_ENTRYPOINT_NAME,
						CLIModuleDependentModuleInitializer.class);

		assertNotNull(entrypoints, "a recognised entrypoint name must never yield null");
		assertTrue(entrypoints.isEmpty(), "a recognised name with no registrations must yield an empty list");
	}

	/**
	 * R9 — an unrecognised name is rejected, and the message identifies the name and the class.
	 */
	@Test
	void unrecognisedNameIsRejectedLoudly()
	{
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> this.loader.getEntrypoints(CurvedSpacetimeLoaderClosedLoaderTest.UNKNOWN_ENTRYPOINT_NAME,
						ModuleInitializer.class),
				"an unrecognised entrypoint name must be rejected, not silently empty");

		String message = thrown.getMessage();
		assertNotNull(message, "the rejection must carry a message");
		assertTrue(message.contains(CurvedSpacetimeLoaderClosedLoaderTest.UNKNOWN_ENTRYPOINT_NAME),
				() -> "the message must identify the entrypoint name, but was: " + message);
		assertTrue(message.contains(ModuleInitializer.class.getSimpleName()),
				() -> "the message must identify the requested class, but was: " + message);
	}

	/**
	 * R10 — {@code invokeEntrypoints} applies the consumer to every entrypoint
	 * {@code getEntrypoints} would return.
	 */
	@Test
	void invokeEntrypointsVisitsEveryEntrypoint()
	{
		List<ModuleInitializer> expected = this.loader.getEntrypoints("main", ModuleInitializer.class);
		List<ModuleInitializer> visited = new ArrayList<>();

		this.loader.invokeEntrypoints("main", ModuleInitializer.class, visited::add);

		assertEquals(expected.size(), visited.size(),
				"every registered entrypoint must be visited exactly once");
		assertTrue(visited.containsAll(expected), "every registered entrypoint must be visited");
	}

	/**
	 * R11 — repeated lookups return the same instances, which the handshake depends upon.
	 */
	@Test
	void entrypointInstancesAreStableAcrossCalls()
	{
		List<ModuleInitializer> first = this.loader.getEntrypoints("main", ModuleInitializer.class);
		List<ModuleInitializer> second = this.loader.getEntrypoints("main", ModuleInitializer.class);

		assertEquals(first.size(), second.size(),
				"repeated lookups must return the same number of entrypoints");
		for (int index = 0; index < first.size(); index++)
		{
			assertSame(first.get(index), second.get(index),
					"repeated lookups must return the same entrypoint instances");
		}
	}
}

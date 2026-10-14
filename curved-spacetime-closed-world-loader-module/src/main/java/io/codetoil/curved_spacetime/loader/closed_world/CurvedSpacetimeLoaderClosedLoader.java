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

package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.api.Engine;
import io.codetoil.curved_spacetime.cli.entrypoint.CurvedSpacetimeCLIModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.CurvedSpacetimeRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.entrypoint.CurvedSpacetimeRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.glfw.CurvedSpacetimeRenderGLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderGLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.entrypoint.CurvedSpacetimeRenderGLFWModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan.CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderVulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.CurvedSpacetimeRenderVulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.CurvedSpacetimeVulkanModuleDependentCurvedSpacetimeRenderVulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.entrypoint.CurvedSpacetimeRenderVulkanModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan_glfw.*;
import io.codetoil.curved_spacetime.render.vulkan_glfw.entrypoint.CurvedSpacetimeRenderVulkanGLFWModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.glfw_ffm.CurvedSpacetimeRenderGLFWFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw_ffm.CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderGLFWFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw_ffm.entrypoint.CurvedSpacetimeRenderGLFWFFMModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan_ffm.CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderVulkanFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_ffm.CurvedSpacetimeRenderVulkanFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_ffm.CurvedSpacetimeVulkanFFMModuleDependentCurvedSpacetimeRenderVulkanFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan_ffm.entrypoint.CurvedSpacetimeRenderVulkanFFMModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan_ffm_glfw_ffm.*;
import io.codetoil.curved_spacetime.render.vulkan_ffm_glfw_ffm.entrypoint.CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.simulator.entrypoint.CurvedSpacetimeSimulatorModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.CurvedSpacetimeVulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan.entrypoint.CurvedSpacetimeVulkanModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan_ffm.CurvedSpacetimeVulkanFFMModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan_ffm.entrypoint.CurvedSpacetimeVulkanFFMModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.webserver.entrypoint.CurvedSpacetimeWebserverModuleDependentModuleInitializer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * A loader that resolves entrypoints from static tables rather than discovering them.
 * <p>
 * <strong>This exists because of GraalVM.</strong> Native-image performs closed-world reachability
 * analysis at build time: only code it can prove reachable is compiled into the image. Writing the entrypoints as
 * {@code static final List.of(new …Entrypoint())} makes every one of them reachable through ordinary static analysis,
 * so the image needs no reflection metadata and does no classpath scanning at start-up. A dynamically discovering
 * loader would need reachability configuration for every module. R7 of the Module System Specification explicitly
 * permits this: a loader may resolve entrypoints statically.
 * <p>
 * The corollary is that this file is not merely a registry — <strong>it decides what is in the image</strong>.
 * Entrypoints commented out of {@link #MAIN_ENTRYPOINTS} are excluded deliberately, not left half-finished.
 * <p>
 * That exclusion is less complete than it looks. The dependent-entrypoint lists below are {@code static final} too, so
 * they are constructed during class initialization and their classes are reachable regardless of whether the
 * corresponding {@code main} entrypoint is commented out. Removing a module from the image therefore means clearing it
 * from <em>both</em> places. {@code -H:+PrintClassInitialization}, already enabled in the build, shows what actually
 * got in.
 *
 * @see io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader
 */
public class CurvedSpacetimeLoaderClosedLoader implements CurvedSpacetimeLoader
{
	private static final boolean FFM = true;

	private static final String MAIN_ENTRYPOINT_NAME = "main";
	private static final List<ModuleInitializer> MAIN_ENTRYPOINTS = List.of(
			// new CurvedSpacetimeCLIModuleEntrypoint(),
			FFM ? new CurvedSpacetimeVulkanFFMModuleEntrypoint() : new CurvedSpacetimeVulkanModuleEntrypoint(),
			new CurvedSpacetimeRenderModuleEntrypoint(),
			// new CurvedSpacetimeSimulatorModuleEntrypoint(),
			FFM ? new CurvedSpacetimeRenderGLFWFFMModuleEntrypoint() : new CurvedSpacetimeRenderGLFWModuleEntrypoint(),
			FFM ? new CurvedSpacetimeRenderVulkanFFMModuleEntrypoint() : new CurvedSpacetimeRenderVulkanModuleEntrypoint(),
			FFM ? new CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint() : new CurvedSpacetimeRenderVulkanGLFWModuleEntrypoint()//,
			//new CurvedSpacetimeWebserverModuleEntrypoint()
	);
	private static final String CLI_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "curved_spacetime_cli_module_dependent";
	private static final List<CurvedSpacetimeCLIModuleDependentModuleInitializer>
			CLI_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "curved_spacetime_render_module_dependent";
	private static final List<CurvedSpacetimeRenderModuleDependentModuleInitializer>
			RENDER_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			FFM ? new CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderGLFWFFMModuleEntrypoint() : new CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderGLFWModuleEntrypoint(),
			FFM ? new CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderVulkanFFMModuleEntrypoint() : new CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderVulkanModuleEntrypoint(),
			FFM ? new CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint() : new CurvedSpacetimeRenderModuleDependentCurvedSpacetimeRenderVulkanGLFWModuleEntrypoint()
	);
	private static final String VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME = "curved_spacetime_vulkan_module_dependent";
	private static final List<CurvedSpacetimeVulkanModuleDependentModuleInitializer>
			VULKAN_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			new CurvedSpacetimeVulkanModuleDependentCurvedSpacetimeRenderVulkanModuleEntrypoint(),
			new CurvedSpacetimeVulkanModuleDependentCurvedSpacetimeRenderVulkanGLFWModuleEntrypoint()
	);
	private static final String FFM_VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME = "curved_spacetime_vulkan_ffm_module_dependent";
	private static final List<CurvedSpacetimeVulkanFFMModuleDependentModuleInitializer> FFM_VULKAN_MODULE_DEPENDENT_ENTRYPOINTS =
			List.of(
					new CurvedSpacetimeVulkanFFMModuleDependentCurvedSpacetimeRenderVulkanFFMModuleEntrypoint(),
					new CurvedSpacetimeVulkanFFMModuleDependentCurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint()
			);
	private static final String SIMULATOR_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "curved_spacetime_simulator_module_dependent";
	private static final List<CurvedSpacetimeSimulatorModuleDependentModuleInitializer>
			SIMULATOR_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME =
			"curved_spacetime_render_glfw_module_dependent";
	private static final List<CurvedSpacetimeRenderGLFWModuleDependentModuleInitializer>
			GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new CurvedSpacetimeRenderGLFWModuleDependentCurvedSpacetimeRenderVulkanGLFWModuleEntrypoint());
	private static final String VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME =
			"curved_spacetime_render_vulkan_module_dependent";
	private static final List<CurvedSpacetimeRenderVulkanModuleDependentModuleInitializer>
			VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new CurvedSpacetimeRenderVulkanModuleDependentCurvedSpacetimeRenderVulkanGLFWModuleEntrypoint());
	private static final String VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "curved_spacetime_render_vulkan_glfw_module_dependent";
	private static final List<CurvedSpacetimeRenderVulkanGLFWModuleDependentModuleInitializer>
			VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String FFM_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "curved_spacetime_render_glfw_ffm_module_dependent";
	private static final List<CurvedSpacetimeRenderGLFWFFMModuleDependentModuleInitializer>
			FFM_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new CurvedSpacetimeRenderGLFWFFMModuleDependentCurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint());
	private static final String FFM_VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME =
			"curved_spacetime_render_vulkan_ffm_module_dependent";
	private static final List<CurvedSpacetimeRenderVulkanFFMModuleDependentModuleInitializer>
			FFM_VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new CurvedSpacetimeRenderVulkanFFMModuleDependentCurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint());
	private static final String FFM_VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "curved_spacetime_render_vulkan_ffm_glfw_ffm_module_dependent";
	private static final List<CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleDependentModuleInitializer>
			FFM_VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String WEBSERVER_MODULE_DEPENDENT_ENTRYPOINT_NAME =
			"curved_spacetime_webserver_module_dependent";
	private static final List<CurvedSpacetimeWebserverModuleDependentModuleInitializer>
			WEBSERVER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of();
	private Engine engine;

	/**
	 * Creates the loader.
	 * <p>
	 * The entrypoint tables are static, so they are populated during class initialization rather than here.
	 */
	public CurvedSpacetimeLoaderClosedLoader()
	{
	}

	@Override
	public void prepareModInit(Path path, Engine engine)
	{
		this.engine = engine;
	}

	@SuppressWarnings("unchecked") // Should always be valid in this case.
	@Override
	public <E> List<E> getEntrypoints(String name, Class<E> moduleInitializerClass)
	{
		if (MAIN_ENTRYPOINT_NAME.equals(name) && moduleInitializerClass.isAssignableFrom(ModuleInitializer.class))
		{
			return (List<E>) MAIN_ENTRYPOINTS;
		}
		if (CLI_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeCLIModuleDependentModuleInitializer.class))
		{
			return (List<E>) CLI_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeRenderModuleDependentModuleInitializer.class))
		{
			return (List<E>) RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeVulkanModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (FFM_VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeVulkanFFMModuleDependentModuleInitializer.class))
		{
			return (List<E>) FFM_VULKAN_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (SIMULATOR_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeSimulatorModuleDependentModuleInitializer.class))
		{
			return (List<E>) SIMULATOR_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(
						CurvedSpacetimeRenderGLFWModuleDependentModuleInitializer.class))
		{
			return (List<E>) GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(
						CurvedSpacetimeRenderVulkanModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(
						CurvedSpacetimeRenderVulkanGLFWModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (FFM_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeRenderGLFWFFMModuleDependentModuleInitializer.class))
		{
			return (List<E>) FFM_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (FFM_VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeRenderVulkanFFMModuleDependentModuleInitializer.class))
		{
			return (List<E>) FFM_VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (FFM_VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleDependentModuleInitializer.class))
		{
			return (List<E>) FFM_VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (WEBSERVER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(CurvedSpacetimeWebserverModuleDependentModuleInitializer.class))
		{
			return (List<E>) WEBSERVER_MODULE_DEPENDENT_ENTRYPOINTS;
		}

		throw new IllegalArgumentException("Cannot get Entrypoints: Invalid entrypoint type: " + name + " with class "
				+ moduleInitializerClass + ". Add corresponding code to io.codetoil.curved_spacetime.loader.closed." +
				"CurvedSpacetimeLoaderClosedLoader.");
	}

	@Override
	public <E> void invokeEntrypoints(String name, Class<E> moduleInitializerClass,
									  Consumer<? super E> moduleInitializerConsumer)
	{
		this.getEntrypoints(name, moduleInitializerClass).forEach(moduleInitializerConsumer);
	}

	@Override
	public Engine getEngine()
	{
		return this.engine;
	}
}

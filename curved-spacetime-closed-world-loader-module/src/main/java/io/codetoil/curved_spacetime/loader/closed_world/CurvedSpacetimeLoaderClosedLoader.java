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

package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.cli.entrypoint.CLIModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.entrypoint.RenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.RenderModuleDependentGLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.entrypoint.GLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan.RenderModuleDependentVulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.VulkanModuleDependentVulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.entrypoint.VulkanRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan_glfw.*;
import io.codetoil.curved_spacetime.render.vulkan_glfw.entrypoint.VulkanGLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.simulator.entrypoint.SimulatorModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan.entrypoint.VulkanModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.webserver.entrypoint.WebserverModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.webserver.openapi.WebserverModuleDependentWebserverOpenAPIModuleEntrypoint;
import io.codetoil.curved_spacetime.webserver.openapi.entrypoint.WebserverOpenAPIModuleDependentModuleInitializer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * A loader that resolves entrypoints from static tables rather than discovering them.
 * <p>
 * <strong>This exists because of GraalVM.</strong> Native-image performs closed-world reachability
 * analysis at build time: only code it can prove reachable is compiled into the image. Writing the
 * entrypoints as {@code static final List.of(new …Entrypoint())} makes every one of them reachable
 * through ordinary static analysis, so the image needs no reflection metadata and does no
 * classpath scanning at start-up. A dynamically discovering loader would need reachability
 * configuration for every module. R7 of the Module System Specification explicitly permits this:
 * a loader may resolve entrypoints statically.
 * <p>
 * The corollary is that this file is not merely a registry — <strong>it decides what is in the
 * image</strong>. Entrypoints commented out of {@link #MAIN_ENTRYPOINTS} are excluded
 * deliberately, not left half-finished.
 * <p>
 * That exclusion is less complete than it looks. The dependent-entrypoint lists below are
 * {@code static final} too, so they are constructed during class initialization and their classes
 * are reachable regardless of whether the corresponding {@code main} entrypoint is commented out.
 * Removing a module from the image therefore means clearing it from <em>both</em> places.
 * {@code -H:+PrintClassInitialization}, already enabled in the build, shows what actually got in.
 *
 * @see io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader
 */
public class CurvedSpacetimeLoaderClosedLoader implements CurvedSpacetimeLoader
{
	private static final String MAIN_ENTRYPOINT_NAME = "main";
	private static final List<ModuleInitializer> MAIN_ENTRYPOINTS = List.of(
			// new CLIModuleEntrypoint(),
			new VulkanModuleEntrypoint(),
			new RenderModuleEntrypoint(),
			// new SimulatorModuleEntrypoint(),
			new GLFWRenderModuleEntrypoint(),
			new VulkanRenderModuleEntrypoint(),
			new VulkanGLFWRenderModuleEntrypoint()//,
			//new WebserverModuleEntrypoint(),
			//new WebserverOpenAPIModuleEntrypoint()
	);
	private static final String CLI_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "cli_module_dependent";
	private static final List<CLIModuleDependentModuleInitializer>
			CLI_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "render_module_dependent";
	private static final List<RenderModuleDependentModuleInitializer> RENDER_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			new RenderModuleDependentGLFWRenderModuleEntrypoint(),
			new RenderModuleDependentVulkanRenderModuleEntrypoint(),
			new RenderModuleDependentVulkanGLFWRenderModuleEntrypoint()
	);
	private static final String VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME = "vulkan_module_dependent";
	private static final List<VulkanModuleDependentModuleInitializer> VULKAN_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			new VulkanModuleDependentVulkanRenderModuleEntrypoint(),
			new VulkanModuleDependentVulkanGLFWRenderModuleEntrypoint()
	);
	private static final String SIMULATOR_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "simulator_module_dependent";
	private static final List<SimulatorModuleDependentModuleInitializer>
			SIMULATOR_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "glfw_render_module_dependent";
	private static final List<GLFWRenderModuleDependentModuleInitializer> GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new GLFWRenderModuleDependentVulkanGLFWRenderModuleEntrypoint());
	private static final String VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "vulkan_render_module_dependent";
	private static final List<VulkanRenderModuleDependentModuleInitializer> VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new VulkanRenderModuleDependentVulkanGLFWRenderModuleEntrypoint());
	private static final String VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "vulkan_glfw_render_module_dependent";
	private static final List<VulkanGLFWRenderModuleDependentModuleInitializer>
			VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private static final String WEBSERVER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "webserver_module_dependent";
	private static final List<WebserverModuleDependentModuleInitializer> WEBSERVER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new WebserverModuleDependentWebserverOpenAPIModuleEntrypoint());
	private static final String WEBSERVER_OPENAPI_MODULE_DEPENDENT_ENTRYPOINT_NAME
			= "webserver_openapi_module_dependent";
	private static final List<WebserverOpenAPIModuleDependentModuleInitializer>
			WEBSERVER_OPENAPI_MODULE_DEPENDENT_ENTRYPOINTS = List.of();
	private Object engine;

	/**
	 * Creates the loader.
	 * <p>
	 * The entrypoint tables are static, so they are populated during class initialization rather
	 * than here.
	 */
	public CurvedSpacetimeLoaderClosedLoader()
	{
	}

	@Override
	public void prepareModInit(Path path, Object engine)
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
				moduleInitializerClass.isAssignableFrom(CLIModuleDependentModuleInitializer.class))
		{
			return (List<E>) CLI_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(RenderModuleDependentModuleInitializer.class))
		{
			return (List<E>) RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(VulkanModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (SIMULATOR_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(SimulatorModuleDependentModuleInitializer.class))
		{
			return (List<E>) SIMULATOR_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(GLFWRenderModuleDependentModuleInitializer.class))
		{
			return (List<E>) GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(VulkanRenderModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(VulkanGLFWRenderModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS;
		}

		if (WEBSERVER_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(WebserverModuleDependentModuleInitializer.class))
		{
			return (List<E>) WEBSERVER_MODULE_DEPENDENT_ENTRYPOINTS;
		}
		if (WEBSERVER_OPENAPI_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(WebserverOpenAPIModuleDependentModuleInitializer.class))
		{
			return (List<E>) WEBSERVER_OPENAPI_MODULE_DEPENDENT_ENTRYPOINTS;
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
	public Object getEngine()
	{
		return this.engine;
	}
}

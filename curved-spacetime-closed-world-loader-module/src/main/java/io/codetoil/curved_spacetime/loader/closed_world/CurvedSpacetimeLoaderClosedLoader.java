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
import io.codetoil.curved_spacetime.spring.web.SpringWebModuleEntrypoint;
import io.codetoil.curved_spacetime.spring.web.entrypoint.SpringWebModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan.entrypoint.VulkanModuleDependentModuleInitializer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

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
			new VulkanGLFWRenderModuleEntrypoint(),
			new SpringWebModuleEntrypoint()
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
	private static final String SPRING_WEB_MODULE_DEPENDENT_ENTRYPOINT_NAME = "spring_web_module_dependent";
	private static final List<SpringWebModuleDependentModuleInitializer> SPRING_WEB_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of();
	private Object engine;

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

		if (SPRING_WEB_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(SpringWebModuleDependentModuleInitializer.class))
		{
			return (List<E>) SPRING_WEB_MODULE_DEPENDENT_ENTRYPOINTS;
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

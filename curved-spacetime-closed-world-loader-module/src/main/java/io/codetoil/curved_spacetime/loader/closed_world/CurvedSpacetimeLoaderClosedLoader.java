package io.codetoil.curved_spacetime.loader.closed_world;

import io.codetoil.curved_spacetime.api.render.entrypoint.RenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.glfw.GLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.glfw.entrypoint.GLFWModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.GLFWModuleDependentGLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.RenderModuleDependentGLFWRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.glfw.entrypoint.GLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan.RenderModuleDependentVulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.VulkanModuleDependentVulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.vulkan.entrypoint.VulkanRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.render.vulkan_glfw.*;
import io.codetoil.curved_spacetime.render.vulkan_glfw.entrypoint.VulkanGLFWRenderModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan.entrypoint.VulkanModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.vulkan_glfw.GLFWModuleDependentVulkanGLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan_glfw.VulkanGLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan_glfw.VulkanModuleDependentVulkanGLFWModuleEntrypoint;
import io.codetoil.curved_spacetime.vulkan_glfw.entrypoint.VulkanGLFWModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.webserver.entrypoint.WebserverModuleDependentModuleInitializer;
import io.codetoil.curved_spacetime.webserver.openapi.WebserverModuleDependentWebserverOpenAPIModuleEntrypoint;
import io.codetoil.curved_spacetime.webserver.openapi.entrypoint.WebserverOpenAPIModuleDependentModuleInitializer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class CurvedSpacetimeLoaderClosedLoader implements CurvedSpacetimeLoader
{
	private static final String MAIN_ENTRYPOINT_NAME = "main";
	private static final List<ModuleInitializer> MAIN_ENTRYPOINTS = List.of(
			new GLFWModuleEntrypoint(),
			new VulkanModuleEntrypoint(),
			new RenderModuleEntrypoint(),
			new GLFWRenderModuleEntrypoint(),
			new VulkanGLFWModuleEntrypoint(),
			new VulkanRenderModuleEntrypoint(),
			new VulkanGLFWRenderModuleEntrypoint()//,
			//new WebserverModuleEntrypoint(),
			//new WebserverOpenAPIModuleEntrypoint()
	);
	private static final String GLFW_MODULE_DEPENDENT_ENTRYPOINT_NAME = "glfw_module_dependent";
	private static final List<GLFWModuleDependentModuleInitializer> GLFW_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			new GLFWModuleDependentGLFWRenderModuleEntrypoint(),
			new GLFWModuleDependentVulkanGLFWModuleEntrypoint(),
			new GLFWModuleDependentVulkanGLFWRenderModuleEntrypoint()
	);
	private static final String RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "render_module_dependent";
	private static final List<RenderModuleDependentModuleInitializer> RENDER_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			new RenderModuleDependentGLFWRenderModuleEntrypoint(),
			new RenderModuleDependentVulkanRenderModuleEntrypoint(),
			new RenderModuleDependentVulkanGLFWRenderModuleEntrypoint()
	);
	private static final String VULKAN_MODULE_DEPENDENT_ENTRYPOINT_NAME = "vulkan_module_dependent";
	private static final List<VulkanModuleDependentModuleInitializer> VULKAN_MODULE_DEPENDENT_ENTRYPOINTS = List.of(
			new VulkanModuleDependentVulkanRenderModuleEntrypoint(),
			new VulkanModuleDependentVulkanGLFWModuleEntrypoint(),
			new VulkanModuleDependentVulkanGLFWRenderModuleEntrypoint()
	);
	private static final String GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "glfw_render_module_dependent";
	private static final List<GLFWRenderModuleDependentModuleInitializer> GLFW_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new GLFWRenderModuleDependentVulkanGLFWRenderModuleEntrypoint());
	private static final String VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINT_NAME = "vulkan_render_module_dependent";
	private static final List<VulkanRenderModuleDependentModuleInitializer> VULKAN_RENDER_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new VulkanRenderModuleDependentVulkanGLFWRenderModuleEntrypoint());
	private static final String VULKAN_GLFW_MODULE_DEPENDENT_ENTRYPOINT_NAME = "vulkan_glfw_module_dependent";
	private static final List<VulkanGLFWModuleDependentModuleInitializer> VULKAN_GLFW_MODULE_DEPENDENT_ENTRYPOINTS
			= List.of(new VulkanGLFWModuleDependentVulkanGLFWRenderModuleEntrypoint());
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

	@Override
	public void prepareModInit(Path path, Object engine)
	{
		this.engine = engine;
	}

	@Override
	public <E> List<E> getEntrypoints(String name, Class<E> moduleInitializerClass)
	{
		if (MAIN_ENTRYPOINT_NAME.equals(name) && moduleInitializerClass.isAssignableFrom(ModuleInitializer.class))
		{
			return (List<E>) MAIN_ENTRYPOINTS;
		}

		if (GLFW_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(GLFWModuleDependentModuleInitializer.class))
		{
			return (List<E>) GLFW_MODULE_DEPENDENT_ENTRYPOINTS;
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
		if (VULKAN_GLFW_MODULE_DEPENDENT_ENTRYPOINT_NAME.equals(name) &&
				moduleInitializerClass.isAssignableFrom(VulkanGLFWModuleDependentModuleInitializer.class))
		{
			return (List<E>) VULKAN_GLFW_MODULE_DEPENDENT_ENTRYPOINTS;
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

/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.render.vulkan_glfw;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleConfig;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleRenderer;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleForwardRenderActivity;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleGraphicsQueue.VulkanRenderPresentModuleGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSurface;
import io.codetoil.curved_spacetime.render.vulkan.VulkanRenderModuleSwapChain;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.VulkanModuleVulkanInstance;

import java.util.logging.Logger;

/**
 * Draws a scene into a GLFW window using Vulkan.
 * <p>
 * This is where the two stacks finally meet: it opens a GLFW window, creates a Vulkan surface onto
 * it, and builds the swap chain and per-frame resources against that surface. Everything is
 * created in {@link #init()} and torn down in reverse in {@link #clean()}, with the queue drained
 * first so nothing is destroyed while still in use.
 * <p>
 * The draw loop itself is not implemented yet — {@link #loop()} currently only services the
 * window — so several of the fields below are created but not yet driven.
 */
public class VulkanGLFWRenderModuleRenderer extends GLFWRenderModuleRenderer
{
	private final VulkanGLFWRenderModuleEntrypoint entrypoint;

	/**
	 * The GLFW window this renderer draws into.
	 */
	protected VulkanGLFWRenderModuleWindow vulkanGLFWRenderWindow;

	/**
	 * The pool command buffers for this renderer are allocated from.
	 */
	protected VulkanModuleCommandPool vulkanGraphicsCommandPool = null;

	/**
	 * The queue rendering work is submitted to.
	 */
	protected VulkanRenderModuleGraphicsQueue vulkanGraphicsQueue = null;

	/**
	 * The Vulkan surface backed by the GLFW window.
	 */
	protected VulkanRenderModuleSurface vulkanRenderModuleSurface = null;

	/**
	 * The swap chain presenting to the surface.
	 */
	protected VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain = null;

	/**
	 * The queue completed frames are presented on.
	 */
	protected VulkanRenderPresentModuleGraphicsQueue vulkanGraphicsPresentQueue = null;

	/**
	 * The render pass and per-frame resources used to draw.
	 */
	protected VulkanRenderModuleForwardRenderActivity vulkanRenderModuleForwardRenderActivity = null;

	/**
	 * Creates a renderer for one scene.
	 * <p>
	 * No window or Vulkan object is created until {@link #init()} runs on the engine's callback
	 * thread.
	 *
	 * @param mainModuleEngine the engine this renderer belongs to
	 * @param scene            the scene to draw
	 * @param entrypoint       the module entrypoint supplying configuration and dependencies
	 */
	public VulkanGLFWRenderModuleRenderer(MainModuleEngine mainModuleEngine, Scene scene,
										  VulkanGLFWRenderModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine, scene);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		Logger logger = this.entrypoint.getLogger();

		VulkanModuleVulkanInstance vulkanModuleVulkanInstance = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModuleVulkanInstance();
		VulkanModulePhysicalDevice vulkanModulePhysicalDevice = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModulePhysicalDevice();
		VulkanModuleLogicalDevice vulkanModuleLogicalDevice = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModuleLogicalDevice();

		this.vulkanGLFWRenderWindow = new VulkanGLFWRenderModuleWindow(mainModuleEngine, "curved-spacetime", logger);

		this.vulkanGLFWRenderWindow.init();

		this.vulkanRenderModuleSurface = new VulkanGLFWRenderModuleRenderModuleSurface(vulkanModuleVulkanInstance,
				vulkanModulePhysicalDevice,
				this.vulkanGLFWRenderWindow.getWindowHandle(), logger);
		this.vulkanGraphicsQueue = new VulkanRenderModuleGraphicsQueue(vulkanModuleLogicalDevice, 0, logger);
		//this.vulkanGraphicsPresentQueue =
		//		new VulkanGraphicsQueue.VulkanGraphicsPresentQueue(this.vulkanInstance.getVulkanLogicalDevice(),
		//				this.vulkanSurface, 0);
		this.vulkanRenderModuleSwapChain =
				new VulkanRenderModuleSwapChain(vulkanModuleLogicalDevice, this.vulkanRenderModuleSurface,
						this.vulkanGLFWRenderWindow,
						((VulkanGLFWRenderModuleConfig) this.entrypoint.getConfig())
								.getRequestedImages(),
						((GLFWRenderModuleConfig) this.entrypoint.getGlfwRenderModuleEntrypoint().getConfig())
								.hasVSync(), logger);//, this.vulkanGraphicsPresentQueue,
		// new VulkanGraphicsQueue[] {this.vulkanGraphicsQueue});
		/*this.vulkanGraphicsCommandPool = new VulkanCommandPool(this.vulkanInstance.getVulkanLogicalDevice(),
				this.vulkanGraphicsQueue.getQueueFamilyIndex());
		this.vulkanForwardRenderActivity =
				new VulkanForwardRenderActivity(this.vulkanSwapChain, this.vulkanGraphicsCommandPool);*/
		this.initialized = true;
	}

	public void loop()
	{
		/*this.vulkanForwardRenderActivity.waitForVulkanFence();
		int imageIndex = vulkanSwapChain.acquireNextImage();
		if (imageIndex < 0) return;

		this.vulkanForwardRenderActivity.submit(this.vulkanGraphicsQueue);
		this.vulkanSwapChain.presentImage(vulkanGraphicsPresentQueue, imageIndex);*/
		this.vulkanGLFWRenderWindow.loop();
	}

	public void clean()
	{
		//this.vulkanGraphicsPresentQueue.waitIdle();
		this.vulkanGraphicsQueue.waitIdle();
		//this.vulkanForwardRenderActivity.cleanup();
		this.vulkanRenderModuleSwapChain.cleanup();
		this.vulkanRenderModuleSurface.cleanup();
		this.vulkanGLFWRenderWindow.setShouldClose();
		this.vulkanGLFWRenderWindow.clean();
	}
}

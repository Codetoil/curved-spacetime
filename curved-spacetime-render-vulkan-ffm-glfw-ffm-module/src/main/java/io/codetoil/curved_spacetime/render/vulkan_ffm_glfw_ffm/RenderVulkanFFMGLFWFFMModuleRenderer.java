/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.render.vulkan_ffm_glfw_ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.glfw_ffm.CurvedSpacetimeRenderGLFWFFMModuleConfig;
import io.codetoil.curved_spacetime.render.glfw_ffm.RenderGLFWFFMModuleRenderer;
import io.codetoil.curved_spacetime.render.vulkan_ffm.RenderVulkanFFMModuleForwardRenderActivity;
import io.codetoil.curved_spacetime.render.vulkan_ffm.RenderVulkanFFMModuleGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan_ffm.RenderVulkanFFMModuleGraphicsQueue.RenderVulkanFFMPresentModuleGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan_ffm.RenderVulkanFFMModuleSurface;
import io.codetoil.curved_spacetime.render.vulkan_ffm.RenderVulkanFFMModuleSwapChain;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan_ffm.VulkanFFMModuleVulkanInstance;

import java.util.logging.Logger;

public class RenderVulkanFFMGLFWFFMModuleRenderer extends RenderGLFWFFMModuleRenderer
{
	private final CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint entrypoint;
	protected RenderVulkanFFMGLFWFFMModuleWindow vulkanGLFWRenderWindow;
	protected VulkanFFMModuleCommandPool vulkanGraphicsCommandPool = null;
	protected RenderVulkanFFMModuleGraphicsQueue vulkanGraphicsQueue = null;
	protected RenderVulkanFFMModuleSurface renderVulkanFFMModuleSurface = null;
	protected RenderVulkanFFMModuleSwapChain renderVulkanFFMModuleSwapChain = null;
	protected RenderVulkanFFMPresentModuleGraphicsQueue vulkanGraphicsPresentQueue = null;
	protected RenderVulkanFFMModuleForwardRenderActivity renderVulkanFFMModuleForwardRenderActivity = null;

	public RenderVulkanFFMGLFWFFMModuleRenderer(MainModuleEngine mainModuleEngine, Scene scene,
											 CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine, scene);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		Logger logger = this.entrypoint.getLogger();

		VulkanFFMModuleVulkanInstance vulkanFFMModuleVulkanInstance = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModuleVulkanInstance();
		VulkanFFMModulePhysicalDevice vulkanFFMModulePhysicalDevice = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModulePhysicalDevice();
		VulkanFFMModuleLogicalDevice vulkanFFMModuleLogicalDevice = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModuleLogicalDevice();

		this.vulkanGLFWRenderWindow = new RenderVulkanFFMGLFWFFMModuleWindow(mainModuleEngine, "curved-spacetime", logger);

		this.vulkanGLFWRenderWindow.init();

		this.renderVulkanFFMModuleSurface =
				new RenderVulkanFFMGLFWFFMModuleRenderModuleSurface(vulkanFFMModuleVulkanInstance,
						vulkanFFMModulePhysicalDevice,
						this.vulkanGLFWRenderWindow.getWindow(), logger);
		this.vulkanGraphicsQueue = new RenderVulkanFFMModuleGraphicsQueue(vulkanFFMModuleLogicalDevice, 0, logger);
		//this.vulkanGraphicsPresentQueue =
		//		new VulkanGraphicsQueue.VulkanGraphicsPresentQueue(this.vulkanInstance.getVulkanLogicalDevice(),
		//				this.vulkanSurface, 0);
		this.renderVulkanFFMModuleSwapChain =
				new RenderVulkanFFMModuleSwapChain(vulkanFFMModuleLogicalDevice, this.renderVulkanFFMModuleSurface,
						this.vulkanGLFWRenderWindow,
						((CurvedSpacetimeRenderVulkanFFMGLFWFFMModuleConfig) this.entrypoint.getConfig())
								.getRequestedImages(),
						((CurvedSpacetimeRenderGLFWFFMModuleConfig) this.entrypoint.getGlfwRenderModuleEntrypoint().getConfig())
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
		this.renderVulkanFFMModuleSwapChain.cleanup();
		this.renderVulkanFFMModuleSurface.cleanup();
		this.vulkanGLFWRenderWindow.setShouldClose();
		this.vulkanGLFWRenderWindow.clean();
	}
}

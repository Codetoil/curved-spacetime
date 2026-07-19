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

package io.codetoil.curved_spacetime.render.vulkan_glfw.ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.glfw.ffm.FFMGLFWRenderModuleConfig;
import io.codetoil.curved_spacetime.render.glfw.ffm.FFMGLFWRenderModuleRenderer;
import io.codetoil.curved_spacetime.render.vulkan.ffm.FFMVulkanRenderModuleForwardRenderActivity;
import io.codetoil.curved_spacetime.render.vulkan.ffm.FFMVulkanRenderModuleGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan.ffm.FFMVulkanRenderModuleGraphicsQueue.FFMVulkanRenderPresentModuleGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan.ffm.FFMVulkanRenderModuleSurface;
import io.codetoil.curved_spacetime.render.vulkan.ffm.FFMVulkanRenderModuleSwapChain;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleCommandPool;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleLogicalDevice;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModulePhysicalDevice;
import io.codetoil.curved_spacetime.vulkan.ffm.FFMVulkanModuleVulkanInstance;

import java.util.logging.Logger;

public class FFMVulkanGLFWRenderModuleRenderer extends FFMGLFWRenderModuleRenderer
{
	private final FFMVulkanGLFWRenderModuleEntrypoint entrypoint;
	protected FFMVulkanGLFWRenderModuleWindow vulkanGLFWRenderWindow;
	protected FFMVulkanModuleCommandPool vulkanGraphicsCommandPool = null;
	protected FFMVulkanRenderModuleGraphicsQueue vulkanGraphicsQueue = null;
	protected FFMVulkanRenderModuleSurface ffmVulkanRenderModuleSurface = null;
	protected FFMVulkanRenderModuleSwapChain ffmVulkanRenderModuleSwapChain = null;
	protected FFMVulkanRenderPresentModuleGraphicsQueue vulkanGraphicsPresentQueue = null;
	protected FFMVulkanRenderModuleForwardRenderActivity ffmVulkanRenderModuleForwardRenderActivity = null;

	public FFMVulkanGLFWRenderModuleRenderer(MainModuleEngine mainModuleEngine, Scene scene,
	                                         FFMVulkanGLFWRenderModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine, scene);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		Logger logger = this.entrypoint.getLogger();

		FFMVulkanModuleVulkanInstance ffmVulkanModuleVulkanInstance = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModuleVulkanInstance();
		FFMVulkanModulePhysicalDevice ffmVulkanModulePhysicalDevice = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModulePhysicalDevice();
		FFMVulkanModuleLogicalDevice ffmVulkanModuleLogicalDevice = entrypoint.getVulkanModuleEntrypoint().getVulkan()
				.getVulkanModuleLogicalDevice();

		this.vulkanGLFWRenderWindow = new FFMVulkanGLFWRenderModuleWindow(mainModuleEngine, "curved-spacetime", logger);

		this.vulkanGLFWRenderWindow.init();

		this.ffmVulkanRenderModuleSurface = new FFMFFMVulkanGLFWRenderModuleRenderModuleSurface(ffmVulkanModuleVulkanInstance,
				ffmVulkanModulePhysicalDevice,
				this.vulkanGLFWRenderWindow.getWindow(), logger);
		this.vulkanGraphicsQueue = new FFMVulkanRenderModuleGraphicsQueue(ffmVulkanModuleLogicalDevice, 0, logger);
		//this.vulkanGraphicsPresentQueue =
		//		new VulkanGraphicsQueue.VulkanGraphicsPresentQueue(this.vulkanInstance.getVulkanLogicalDevice(),
		//				this.vulkanSurface, 0);
		this.ffmVulkanRenderModuleSwapChain =
				new FFMVulkanRenderModuleSwapChain(ffmVulkanModuleLogicalDevice, this.ffmVulkanRenderModuleSurface,
						this.vulkanGLFWRenderWindow,
						((FFMVulkanGLFWRenderModuleConfig) this.entrypoint.getConfig())
								.getRequestedImages(),
						((FFMGLFWRenderModuleConfig) this.entrypoint.getGlfwRenderModuleEntrypoint().getConfig())
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
		this.ffmVulkanRenderModuleSwapChain.cleanup();
		this.ffmVulkanRenderModuleSurface.cleanup();
		this.vulkanGLFWRenderWindow.setShouldClose();
		this.vulkanGLFWRenderWindow.clean();
	}
}

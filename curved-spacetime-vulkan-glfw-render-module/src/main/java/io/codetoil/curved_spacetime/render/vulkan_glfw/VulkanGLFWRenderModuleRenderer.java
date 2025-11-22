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

import io.codetoil.curved_spacetime.engine.Engine;
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
import io.codetoil.curved_spacetime.vulkan_glfw.VulkanGLFWModuleWindow;
import org.lwjgl.glfw.GLFWVulkan;

public class VulkanGLFWRenderModuleRenderer extends GLFWRenderModuleRenderer
{
	private final VulkanGLFWRenderModuleEntrypoint entrypoint;
	protected VulkanGLFWModuleWindow vulkanGLFWWindow;
	protected VulkanModuleVulkanInstance vulkanModuleVulkanInstance = null;
	protected VulkanModulePhysicalDevice vulkanModulePhysicalDevice;
	protected VulkanModuleLogicalDevice vulkanModuleLogicalDevice;
	protected VulkanModuleCommandPool vulkanGraphicsCommandPool = null;
	protected VulkanRenderModuleGraphicsQueue vulkanGraphicsQueue = null;
	protected VulkanRenderModuleSurface vulkanRenderModuleSurface = null;
	protected VulkanRenderModuleSwapChain vulkanRenderModuleSwapChain = null;
	protected VulkanRenderPresentModuleGraphicsQueue vulkanGraphicsPresentQueue = null;
	protected VulkanRenderModuleForwardRenderActivity vulkanRenderModuleForwardRenderActivity = null;

	public VulkanGLFWRenderModuleRenderer(Engine engine, Scene scene,
										  VulkanGLFWRenderModuleEntrypoint entrypoint)
	{
		super(engine, scene);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		this.vulkanGLFWWindow = new VulkanGLFWModuleWindow(engine, "curved-spacetime");

		this.vulkanGLFWWindow.init();

		this.vulkanModuleVulkanInstance = new VulkanModuleVulkanInstance(entrypoint.getVulkanModuleEntrypoint(),
				GLFWVulkan::glfwGetRequiredInstanceExtensions);
		this.vulkanModulePhysicalDevice =
				VulkanModulePhysicalDevice.createPhysicalDevice(this.vulkanModuleVulkanInstance,
						entrypoint.getVulkanModuleEntrypoint());
		this.vulkanModuleLogicalDevice = new VulkanModuleLogicalDevice(this.vulkanModulePhysicalDevice);
		this.vulkanRenderModuleSurface = new VulkanGLFWRenderModuleRenderModuleSurface(this.vulkanModuleVulkanInstance,
				this.vulkanModulePhysicalDevice,
				this.vulkanGLFWWindow.getWindowHandle());
		this.vulkanGraphicsQueue = new VulkanRenderModuleGraphicsQueue(this.vulkanModuleLogicalDevice, 0);
		//this.vulkanGraphicsPresentQueue =
		//		new VulkanGraphicsQueue.VulkanGraphicsPresentQueue(this.vulkanInstance.getVulkanLogicalDevice(),
		//				this.vulkanSurface, 0);
		this.vulkanRenderModuleSwapChain =
				new VulkanRenderModuleSwapChain(this.vulkanModuleLogicalDevice, this.vulkanRenderModuleSurface,
						this.vulkanGLFWWindow,
						((VulkanGLFWRenderModuleConfig) this.entrypoint.getConfig())
								.getRequestedImages(),
						((GLFWRenderModuleConfig) this.entrypoint.getGlfwRenderModuleEntrypoint().getConfig())
								.hasVSync());//, this.vulkanGraphicsPresentQueue,
		// new VulkanGraphicsQueue[] {this.vulkanGraphicsQueue});
		/*this.vulkanGraphicsCommandPool = new VulkanCommandPool(this.vulkanInstance.getVulkanLogicalDevice(),
				this.vulkanGraphicsQueue.getQueueFamilyIndex());
		this.vulkanForwardRenderActivity =
				new VulkanForwardRenderActivity(this.vulkanSwapChain, this.vulkanGraphicsCommandPool);*/

	}

	public void loop()
	{
		/*this.vulkanForwardRenderActivity.waitForVulkanFence();
		int imageIndex = vulkanSwapChain.acquireNextImage();
		if (imageIndex < 0) return;

		this.vulkanForwardRenderActivity.submit(this.vulkanGraphicsQueue);
		this.vulkanSwapChain.presentImage(vulkanGraphicsPresentQueue, imageIndex);*/
		this.vulkanGLFWWindow.loop();
	}

	public void clean()
	{
		//this.vulkanGraphicsPresentQueue.waitIdle();
		this.vulkanGraphicsQueue.waitIdle();
		//this.vulkanForwardRenderActivity.cleanup();
		this.vulkanRenderModuleSwapChain.cleanup();
		this.vulkanRenderModuleSurface.cleanup();
		this.vulkanModuleLogicalDevice.waitIdle();
		this.vulkanModuleLogicalDevice.cleanup();
		this.vulkanModulePhysicalDevice.cleanup();
		this.vulkanModuleVulkanInstance.cleanup();
		this.vulkanGLFWWindow.setShouldClose();
		this.vulkanGLFWWindow.clean();
	}
}

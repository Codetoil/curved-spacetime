/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

import io.codetoil.curved_spacetime.api.engine.Engine;
import io.codetoil.curved_spacetime.api.scene.Scene;
import io.codetoil.curved_spacetime.api.render.Renderer;
import io.codetoil.curved_spacetime.render.vulkan.VulkanForwardRenderActivity;
import io.codetoil.curved_spacetime.render.vulkan.VulkanGraphicsQueue;
import io.codetoil.curved_spacetime.render.vulkan.VulkanSurface;
import io.codetoil.curved_spacetime.render.vulkan.VulkanSwapChain;
import io.codetoil.curved_spacetime.vulkan.VulkanCommandPool;
import io.codetoil.curved_spacetime.vulkan.VulkanInstance;
import org.lwjgl.glfw.GLFWVulkan;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VulkanGLFWRenderer extends Renderer
{
	public final VulkanGLFWRenderConfig vulkanRenderConfig;
	protected final VulkanInstance vulkanInstance;
	protected final VulkanCommandPool vulkanGraphicsCommandPool;
	protected final VulkanGraphicsQueue vulkanGraphicsQueue;
	protected final VulkanSurface vulkanSurface;
	protected final VulkanSwapChain vulkanSwapChain;
	protected final VulkanGraphicsQueue.VulkanGraphicsPresentQueue vulkanGraphicsPresentQueue;
	protected final VulkanForwardRenderActivity vulkanForwardRenderActivity;

	public VulkanGLFWRenderer(Engine engine, Scene scene)
	{
		super(engine, scene);

		try
		{
			this.vulkanRenderConfig = new VulkanGLFWRenderConfig().load();
			if (this.vulkanRenderConfig.isDirty()) this.vulkanRenderConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Vulkan Render Config", ex);
		}

		this.window = new VulkanGLFWWindow(engine);

		this.executor = Executors.newSingleThreadScheduledExecutor();
		this.window.init();
		this.window.showWindow();

		this.vulkanInstance = new VulkanInstance(GLFWVulkan::glfwGetRequiredInstanceExtensions);
		this.vulkanSurface = new VulkanGLFWSurface(this.vulkanInstance.getVulkanPhysicalDevice(),
				((VulkanGLFWWindow) this.window).getWindowHandle());
		this.vulkanGraphicsQueue = new VulkanGraphicsQueue(this.vulkanInstance.getVulkanLogicalDevice(), 0);
		this.vulkanGraphicsPresentQueue =
				new VulkanGraphicsQueue.VulkanGraphicsPresentQueue(this.vulkanInstance.getVulkanLogicalDevice(),
						this.vulkanSurface, 0);
		this.vulkanSwapChain =
				new VulkanSwapChain(this.vulkanInstance.getVulkanLogicalDevice(), this.vulkanSurface, this.window,
						this.vulkanRenderConfig.getRequestedImages(), this.vulkanRenderConfig.hasVSync(),
						this.vulkanGraphicsPresentQueue, new VulkanGraphicsQueue[] {this.vulkanGraphicsQueue});
		this.vulkanGraphicsCommandPool = new VulkanCommandPool(this.vulkanInstance.getVulkanLogicalDevice(),
				this.vulkanGraphicsQueue.getQueueFamilyIndex());
		this.vulkanForwardRenderActivity =
				new VulkanForwardRenderActivity(this.vulkanSwapChain, this.vulkanGraphicsCommandPool);

		this.frameHandler =
				this.executor.scheduleAtFixedRate(this.window::loop, 1_000 / this.vulkanRenderConfig.getFPS(),
						1_000 / this.vulkanRenderConfig.getFPS(), TimeUnit.MILLISECONDS);
	}

	public void clean()
	{
		super.clean();
		this.vulkanGraphicsPresentQueue.waitIdle();
		this.vulkanGraphicsQueue.waitIdle();
		this.vulkanForwardRenderActivity.cleanup();
		this.vulkanSwapChain.cleanup();
		this.vulkanSurface.cleanup();
		this.vulkanInstance.cleanup();
	}

	public void render()
	{
		this.vulkanForwardRenderActivity.waitForVulkanFence();
		int imageIndex = vulkanSwapChain.acquireNextImage();
		if (imageIndex < 0) return;

		this.vulkanForwardRenderActivity.submit(this.vulkanGraphicsQueue);
		this.vulkanSwapChain.presentImage(vulkanGraphicsPresentQueue, imageIndex);
	}
}

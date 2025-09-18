/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025 Anthony Michalek
 * (Codetoil)<br> Copyright (c) 2024 Antonio Hernández Bejarano<br>
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
import io.codetoil.curved_spacetime.api.loader.ModuleConfig;
import io.codetoil.curved_spacetime.api.loader.ModuleInitializer;
import io.codetoil.curved_spacetime.api.render.vulkan_glfw.VulkanGLFWRenderer;
import io.codetoil.curved_spacetime.render.glfw.GLFWRenderModuleConfig;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.IOException;

public class VulkanGLFWRenderModuleEntrypoint implements ModuleInitializer
{
	private ModuleConfig config;

	@Override
	public void onInitialize()
	{
		try
		{
			this.config = new VulkanGLFWRenderModuleConfig().load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Vulkan Render Config", ex);
		}

		Engine engine = Engine.getInstance();
		engine.registerSceneLooper("vulkan_glfw_renderer",
				new VulkanGLFWRenderer(engine, engine.scene, (VulkanGLFWRenderModuleConfig) this.config,
						(GLFWRenderModuleConfig) glfwRenderModuleConfig));
	}

	@Override
	public ModuleConfig getConfig()
	{
		return config;
	}
}

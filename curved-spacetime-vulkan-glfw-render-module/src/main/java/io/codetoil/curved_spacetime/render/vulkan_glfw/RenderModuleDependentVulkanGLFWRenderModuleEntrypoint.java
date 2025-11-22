/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025 Anthony Michalek
 * (Codetoil)<br>
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

import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.engine.Engine;
import io.codetoil.curved_spacetime.render.RenderModuleEntrypoint;
import io.codetoil.curved_spacetime.render.entrypoint.RenderModuleDependentModuleInitializer;

public class RenderModuleDependentVulkanGLFWRenderModuleEntrypoint implements RenderModuleDependentModuleInitializer
{

	@Override
	public void onInitialize(RenderModuleEntrypoint renderModuleEntrypoint)
	{
		try
		{
			Engine.getInstance().getCurvedSpacetimeLoader()
					.getEntrypoints("main", ModuleInitializer.class).stream()
					.filter(VulkanGLFWRenderModuleEntrypoint.class::isInstance)
					.findFirst().orElseThrow().getDependencyModuleTransferQueue().transfer(renderModuleEntrypoint);
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}

/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025-2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.api.Engine;
import io.codetoil.curved_spacetime.loader.entrypoint.MainModuleInitializer;
import io.codetoil.curved_spacetime.render.CurvedSpacetimeRenderMainModuleEntrypoint;
import io.codetoil.curved_spacetime.render.entrypoint.CurvedSpacetimeRenderModuleDependentModuleInitializer;

/**
 * Delivers the render module's entrypoint to the GLFW render module.
 * <p>
 * Registered by the GLFW render module under {@code curved_spacetime_render_module_dependent_module_initializer}. The render module
 * invokes this during its own initialization; it locates the GLFW module's main entrypoint through the loader and
 * transfers the render entrypoint into that module's dependency queue, where the GLFW module is blocked waiting for it.
 */
public class CurvedSpacetimeRenderModuleDependentModuleInitializerCurvedSpacetimeRenderGLFWModuleEntrypoint
		implements CurvedSpacetimeRenderModuleDependentModuleInitializer
{
	/**
	 * Creates the dependent entrypoint.
	 * <p>
	 * Called by the loader; it holds no state of its own.
	 */
	public CurvedSpacetimeRenderModuleDependentModuleInitializerCurvedSpacetimeRenderGLFWModuleEntrypoint()
	{
	}


	@Override
	public void onInitialize(CurvedSpacetimeRenderMainModuleEntrypoint renderModuleEntrypoint, Engine engine)
	{
		try
		{
			engine.getCurvedSpacetimeLoader()
					.getEntrypoints("main", MainModuleInitializer.class).stream()
					.filter(CurvedSpacetimeRenderGLFWMainModuleEntrypoint.class::isInstance)
					.findFirst().orElseThrow().getDependencyModuleTransferQueue().transfer(renderModuleEntrypoint);
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}

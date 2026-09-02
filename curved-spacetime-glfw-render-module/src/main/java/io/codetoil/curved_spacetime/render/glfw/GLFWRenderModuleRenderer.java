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

package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.scene.SceneCallback;

/**
 * Base for renderers that draw a scene into a GLFW window.
 * <p>
 * Adds nothing to {@link SceneCallback} beyond narrowing it to the GLFW-backed case; the drawing
 * itself belongs to a subclass that knows which graphics API is in use.
 */
public abstract class GLFWRenderModuleRenderer extends SceneCallback
{
	/**
	 * Creates a renderer for one scene.
	 *
	 * @param mainModuleEngine the engine this renderer belongs to
	 * @param scene            the scene to draw
	 */
	protected GLFWRenderModuleRenderer(MainModuleEngine mainModuleEngine, Scene scene)
	{
		super(mainModuleEngine, scene);
	}
}

/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.scene;

/**
 * One independent thing being simulated and drawn.
 * <p>
 * The engine holds a list of these and pairs each with a {@link SceneCallback} per registered
 * generator, so modules act on scenes without tracking them. A scene currently carries no state
 * of its own: what a scene <em>contains</em> — spacetimes, metrics, worldlines — belongs to the
 * simulation model, which has not been settled yet and is deliberately outside the scope of the
 * Module System Specification.
 */
public class Scene
{
	/**
	 * Creates an empty scene.
	 */
	public Scene()
	{
	}
}

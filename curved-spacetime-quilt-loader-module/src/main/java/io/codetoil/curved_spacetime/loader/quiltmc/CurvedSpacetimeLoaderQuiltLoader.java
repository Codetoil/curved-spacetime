/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.loader.quiltmc;

import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.quiltmc.loader.impl.QuiltLoaderImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * A loader that delegates to Quilt Loader.
 * <p>
 * Where the closed-world loader hard-codes its entrypoints so a native image can reach them, this
 * one discovers modules at run time: Quilt reads each module's {@code quilt.mod.json}, resolves
 * dependencies, and holds the entrypoint registry that the methods here read from. That makes the
 * Quilt variant the extensible one — modules can be dropped in without rebuilding — at the cost of
 * needing the loader present at run time.
 *
 * @see io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader
 */
public class CurvedSpacetimeLoaderQuiltLoader implements CurvedSpacetimeLoader
{
	/**
	 * Creates the loader.
	 * <p>
	 * Holds no state of its own; every lookup is delegated to Quilt Loader.
	 */
	public CurvedSpacetimeLoaderQuiltLoader()
	{
	}

	@Override
	public void prepareModInit(Path path, Object engine)
	{
		QuiltLoaderImpl.INSTANCE.prepareModInit(path, engine);
	}

	@Override
	public <E> List<E> getEntrypoints(String name, Class<E> moduleInitializerClass)
	{
		return QuiltLoaderImpl.INSTANCE.getEntrypoints(name, moduleInitializerClass);
	}

	@Override
	public <E> void invokeEntrypoints(String name, Class<E> moduleInitializerClass,
									  Consumer<? super E> moduleInitializerConsumer)
	{
		EntrypointUtil.invoke(name, moduleInitializerClass, moduleInitializerConsumer);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object getEngine()
	{
		return QuiltLoader.getGameInstance();
	}
}

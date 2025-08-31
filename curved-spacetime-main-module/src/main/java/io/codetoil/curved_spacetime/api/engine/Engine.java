/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.api.engine;

import io.codetoil.curved_spacetime.api.APIConfig;
import io.codetoil.curved_spacetime.api.scene.Scene;
import io.codetoil.curved_spacetime.api.scene.SceneLooper;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Engine
{
	public final APIConfig APIConfig;
	public Scene scene;
	private final Map<String, SceneLooper> sceneLooperMap = new HashMap<>();
	private static Engine INSTANCE;

	public Engine()
	{
		try
		{
			this.APIConfig = new APIConfig().load();
			if (this.APIConfig.isDirty()) this.APIConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load API Config", ex);
		}
		this.scene = new Scene();
	}

	public void clean()
	{
		this.sceneLooperMap.forEach((value, sceneLooper) -> sceneLooper.clean());
	}

	public void registerSceneLooper(String id, SceneLooper sceneLooper)
	{
		this.sceneLooperMap.put(id, sceneLooper);
	}

	public void stop()
	{
		this.clean();
		// TODO Send Stop Event to all mods
	}

	@SuppressWarnings("deprecation")
	public static Engine getInstance()
	{
		if (INSTANCE == null) INSTANCE = (Engine) QuiltLoader.getGameInstance();
		return INSTANCE;
	}
}

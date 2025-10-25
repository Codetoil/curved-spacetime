/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
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

import io.codetoil.curved_spacetime.MainModuleConfig;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.api.scene.Scene;
import io.codetoil.curved_spacetime.api.scene.SceneLooper;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.Future.State;

public class Engine
{
	public final MainModuleConfig mainModuleConfig;
	public Scene scene;
	public static final LogCategory ENGINE_CATEGORY = LogCategory.create("Curved Spacetime Engine");
	private final Map<String, SceneLooper> sceneLooperMap = new HashMap<>();
	protected final ScheduledExecutorService loopExecutor;
	protected final ExecutorService moduleInitializerThreadPool;
	protected ScheduledFuture<?> loopHandler;
	private static Engine INSTANCE;

	public Engine()
	{
		try
		{
			this.mainModuleConfig = new MainModuleConfig().load();
			if (this.mainModuleConfig.isDirty()) this.mainModuleConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load API Config", ex);
		}
		this.scene = new Scene();

		this.loopExecutor = Executors.newSingleThreadScheduledExecutor();
		this.moduleInitializerThreadPool = Executors.newCachedThreadPool();
	}

	public static void main(String[] args) throws Throwable
	{
		Engine engine = new Engine();
		Log.info(Engine.ENGINE_CATEGORY, "Initializing Modules");
		engine.initModules();
		Log.info(Engine.ENGINE_CATEGORY, "Starting main loop.");
		engine.startLoop(1_000 / engine.mainModuleConfig.getFPS(),
				1_000 / engine.mainModuleConfig.getFPS(), TimeUnit.MILLISECONDS);
	}

	public void initModules() throws Throwable
	{
		QuiltLoaderImpl.INSTANCE.prepareModInit(Paths.get(System.getProperty("user.dir")), this);
		CompletionService<?> completionService = new ExecutorCompletionService<>(this.moduleInitializerThreadPool);
		List<Future<?>> futures = new ArrayList<>();
		EntrypointUtils.invoke("main", ModuleInitializer.class, moduleInitializer ->
		{
			futures.add(completionService.submit(moduleInitializer::onInitialize, null));
		});
		this.moduleInitializerThreadPool.shutdown();
		while (!futures.isEmpty())
		{
			Future<?> future = completionService.poll();
			if (future != null)
			{
				futures.remove(future);
				if (State.FAILED.equals(future.state()))
				{
					throw future.exceptionNow();
				}
			}
		}
	}

	public void startLoop(long delay, long period, @NotNull TimeUnit timeUnit)
	{
		this.loopHandler = this.loopExecutor.scheduleAtFixedRate(this::loop, delay, period, timeUnit);
	}

	public void loop() {
		this.sceneLooperMap.forEach((_, sceneLooper) -> sceneLooper.loop());
	}

	public void clean()
	{
		this.loopExecutor.shutdown();
		this.sceneLooperMap.forEach((_, sceneLooper) -> sceneLooper.clean());
	}

	public void registerSceneLooper(String id, SceneLooper sceneLooper)
	{
		this.sceneLooperMap.put(id, sceneLooper);
	}

	public void stop()
	{
		this.loopHandler.cancel(true);
		this.clean();
	}

	@SuppressWarnings("deprecation")
	public static Engine getInstance()
	{
		if (INSTANCE == null) INSTANCE = (Engine) QuiltLoader.getGameInstance();
		return INSTANCE;
	}
}

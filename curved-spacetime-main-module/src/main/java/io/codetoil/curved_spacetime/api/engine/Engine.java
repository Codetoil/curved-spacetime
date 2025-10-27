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

import io.codetoil.curved_spacetime.MainModuleConfig;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.api.scene.Scene;
import io.codetoil.curved_spacetime.api.scene.SceneCallback;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.Future.State;
import java.util.function.Consumer;

public class Engine
{
	private static Engine INSTANCE;
	public final MainModuleConfig mainModuleConfig;
	protected final ScheduledExecutorService sceneCallbackExecutor;
	private final Map<String, SceneCallback> sceneCallbackMap = new HashMap<>();
	public Scene scene;
	protected Future<?> sceneCallbackInitializeHandler;
	protected ScheduledFuture<?> sceneCallbackLoopHandler;

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

		this.sceneCallbackExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	public static void main(String[] args)
	{
		Engine engine = new Engine();
		Logger.info("Running Entrypoints in parallel");
		engine.runEntrypoints();
		Logger.info("Initializing Scene Callbacks");
		engine.sceneCallbackInitializeHandler = engine.sceneCallbackExecutor.submit(() ->
				engine.sceneCallbackMap.forEach((_, sceneCallback) -> sceneCallback.init()));
		Logger.info("Looping Scene Callbacks");
		engine.sceneCallbackLoopHandler = engine.sceneCallbackExecutor.scheduleAtFixedRate(() ->
						engine.sceneCallbackMap.forEach((_, sceneCallback) -> sceneCallback.loop()),
				1_000 / engine.mainModuleConfig.getFPS(),
				1_000 / engine.mainModuleConfig.getFPS(), TimeUnit.MILLISECONDS);
	}

	public void runEntrypoints()
	{
		QuiltLoaderImpl.INSTANCE.prepareModInit(Paths.get(System.getProperty("user.dir")), this);
		try
		{
			Engine.callDependents("main", ModuleInitializer.class, ModuleInitializer::onInitialize);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <C> void callDependents(String name, Class<C> moduleInitializerClass, Consumer<C> onInitialize)
			throws Throwable
	{
		Logger.trace("Dependents of {}: {}\n", name, QuiltLoader.getEntrypoints(name, moduleInitializerClass));
		try (ExecutorService moduleInitializerThreadPool = Executors.newCachedThreadPool())
		{
			CompletionService<?> completionService = new ExecutorCompletionService<>(moduleInitializerThreadPool);
			List<Future<?>> futures = new ArrayList<>();
			EntrypointUtils.invoke(name, moduleInitializerClass, moduleInitializer ->
					futures.add(completionService.submit(() -> {
						Logger.trace("{}: Calling {}.", name, moduleInitializer);
						onInitialize.accept(moduleInitializer);
					}, null)));
			moduleInitializerThreadPool.shutdown();
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

	}

	@SuppressWarnings("deprecation")
	public static Engine getInstance()
	{
		if (INSTANCE == null) INSTANCE = (Engine) QuiltLoader.getGameInstance();
		return INSTANCE;
	}

	public void registerSceneCallback(String id, SceneCallback sceneCallback)
	{
		this.sceneCallbackMap.put(id, sceneCallback);
	}

	public void stop()
	{
		this.sceneCallbackLoopHandler.cancel(true);
		this.clean();
	}

	public void clean()
	{
		this.sceneCallbackExecutor.shutdown();
		this.sceneCallbackMap.forEach((_, sceneCallback) -> sceneCallback.clean());
	}
}

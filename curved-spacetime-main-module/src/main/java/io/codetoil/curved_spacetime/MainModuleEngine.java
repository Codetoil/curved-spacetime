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

package io.codetoil.curved_spacetime;

import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.scene.SceneCallback;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.Future.State;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class MainModuleEngine
{
	protected static MainModuleEngine INSTANCE;
	public final MainModuleConfig mainModuleConfig;
	public final Logger logger = Logger.getLogger("Curved Spacetime Main Module Logger");
	public final Arena nativeAllocator = Arena.ofShared();
	protected final ScheduledExecutorService callbackExecutor;
	protected final CurvedSpacetimeLoader loader;
	private final List<MainCallback> mainCallbacks = new ArrayList<>();
	private final Map<Function<Scene, SceneCallback>, List<SceneCallback>> sceneCallbacks = new HashMap<>();
	private final List<Scene> scenes = new ArrayList<>();
	protected Future<?> callbackInitializeHandler;
	protected ScheduledFuture<?> callbackLoopHandler;

	public MainModuleEngine(CurvedSpacetimeLoader loader)
	{
		INSTANCE = this;
		this.loader = loader;
		try
		{
			this.mainModuleConfig = new MainModuleConfig(this.logger).load();
			if (this.mainModuleConfig.isDirty()) this.mainModuleConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load API Config", ex);
		}
		this.logger.setLevel(this.mainModuleConfig.getLoggerLevel());
		registerScene(new Scene());
		//registerScene(new Scene());
		this.callbackExecutor = Executors.newSingleThreadScheduledExecutor();
		logger.info("Running Entrypoints in parallel");
		this.runEntrypoints();
		logger.info("Initializing Scene Callbacks");
		this.callbackInitializeHandler = this.callbackExecutor.submit(() -> {
			this.mainCallbacks.forEach(MainCallback::init);
			this.sceneCallbacks.forEach((_,
										 sceneCallbacksForGenerator) ->
					sceneCallbacksForGenerator.forEach(SceneCallback::init));
		});
		logger.info("Looping Scene Callbacks");
		this.callbackLoopHandler = this.callbackExecutor.scheduleAtFixedRate(() -> {
					this.mainCallbacks.forEach(MainCallback::loop);
					this.sceneCallbacks.forEach((_,
												 sceneCallbacksForGenerator) ->
							sceneCallbacksForGenerator.forEach(SceneCallback::loop));
				},
				1_000 / this.mainModuleConfig.getFPS(),
				1_000 / this.mainModuleConfig.getFPS(), TimeUnit.MILLISECONDS);
	}

	public void registerScene(Scene scene)
	{
		this.scenes.add(scene);
		this.sceneCallbacks.forEach((sceneCallbackGenerator,
									 sceneCallbacksForGenerator) -> {
			sceneCallbacksForGenerator.add(sceneCallbackGenerator.apply(scene));
		});
	}

	public void runEntrypoints()
	{
		this.loader.prepareModInit(Paths.get(System.getProperty("user.dir")), this);
		try
		{
			MainModuleEngine.callDependents("main", ModuleInitializer.class,
					ModuleInitializer::onInitialize, this.logger);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <E> void callDependents(String name,
										  Class<E> moduleInitializerClass,
										  Consumer<E> onInitialize,
										  Logger logger)
			throws Throwable
	{
		logger.finer("Dependents of " + name + ": " + moduleInitializerClass + "\n");
		try (ExecutorService moduleInitializerThreadPool = Executors.newCachedThreadPool())
		{
			CompletionService<?> completionService = new ExecutorCompletionService<>(moduleInitializerThreadPool);
			List<Future<?>> futures = new ArrayList<>();
			INSTANCE.loader.invokeEntrypoints(name, moduleInitializerClass, moduleInitializer ->
					futures.add(completionService.submit(() -> {
						logger.finer(name + ": Calling " + moduleInitializer + ".");
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

	public static MainModuleEngine getInstance()
	{
		return INSTANCE;
	}

	public CurvedSpacetimeLoader getCurvedSpacetimeLoader()
	{
		return loader;
	}

	public void registerMainCallback(MainCallback mainCallback)
	{
		callbackExecutor.submit(() -> {
			this.mainCallbacks.add(mainCallback);
			mainCallback.init();
		});
	}

	public void registerSceneCallbackGenerator(Function<Scene, SceneCallback> sceneCallbackGenerator)
	{
		callbackExecutor.submit(() -> {
			this.sceneCallbacks.put(sceneCallbackGenerator, this.scenes.stream().map(sceneCallbackGenerator).toList());
			this.sceneCallbacks.get(sceneCallbackGenerator).forEach(SceneCallback::init);
		});
	}

	public void stop()
	{
		this.callbackLoopHandler.cancel(true);
		this.clean();
	}

	public void clean()
	{
		this.callbackExecutor.shutdown();
		this.sceneCallbacks.forEach((_, sceneCallbacks) ->
				sceneCallbacks.forEach(SceneCallback::clean));
	}
}

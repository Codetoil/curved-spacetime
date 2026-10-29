/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2026 Angelina Michalek / Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime;

import io.codetoil.curved_spacetime.api.Engine;
import io.codetoil.curved_spacetime.api.MainCallback;
import io.codetoil.curved_spacetime.api.scene.Scene;
import io.codetoil.curved_spacetime.api.scene.SceneCallback;
import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import io.codetoil.curved_spacetime.loader.entrypoint.MainModuleInitializer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.Future.State;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Engine Implementation
 */
public class MainModuleEngineImpl extends Engine
{

	/**
	 * The engine's settings, loaded before any module runs.
	 */
	private final MainModuleConfig mainModuleConfig;

	/**
	 * The logger the engine writes its own diagnostics to.
	 */
	public final Logger logger = Logger.getLogger("Curved Spacetime Main Module Logger");
	private final ConsoleHandler consoleHandler = new ConsoleHandler();

	/**
	 * The single thread every callback runs on.
	 */
	protected final ScheduledExecutorService callbackExecutor;

	private final List<MainCallback> mainCallbacks = new ArrayList<>();
	private final Map<Function<Scene, SceneCallback>, List<SceneCallback>> sceneCallbacks = new HashMap<>();
	private final List<Scene> scenes = new ArrayList<>();

	/**
	 * Tracks the repeating task that drives every registered callback, cancelled by {@link #clean()}.
	 */
	protected ScheduledFuture<?> callbackLoopHandler;

	/**
	 * Is {@code true} if the Engine is initialized. Note that this doesn't mean all callbacks are registered, As a
	 * callback can be registered at any time and is registered asynchronously.
	 */
	protected boolean initialized = false;

	/**
	 * Creates the engine and brings the whole module graph up.
	 * <p>
	 * Loads the main configuration, registers the initial scene, runs every {@code main} entrypoint, and starts the
	 * callback loop at the configured frame rate.
	 *
	 * @param loader
	 * 		the loader that resolves module entrypoints
	 *
	 * @throws RuntimeException
	 * 		if the main configuration cannot be loaded, or if any module's entrypoint fails
	 */
	public MainModuleEngineImpl(CurvedSpacetimeLoader loader)
	{
		super(loader);
		try
		{
			this.mainModuleConfig = new MainModuleConfig(this.logger).load();
			if (this.mainModuleConfig.isDirty()) this.mainModuleConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load API Config", ex);
		}
		this.logger.setLevel(this.mainModuleConfig.getLoggerLevel());
		this.consoleHandler.setLevel(this.mainModuleConfig.getLoggerLevel());
		this.logger.addHandler(this.consoleHandler);
		this.logger.setUseParentHandlers(false);
		logger.info("Engine initializing");
		registerScene(new Scene());
		//registerScene(new Scene());
		this.callbackExecutor = Executors.newSingleThreadScheduledExecutor();
		logger.finer("Running Entrypoints in parallel");
		this.runEntrypoints();
		logger.info("Engine initialized");
		logger.finer("Looping Scene Callbacks");
		this.callbackLoopHandler = this.callbackExecutor.scheduleAtFixedRate(() -> {
					try
					{
						for (MainCallback c : List.copyOf(this.mainCallbacks))
						{
							if (this.callbackExecutor.isShutdown()) return;
							c.loop();
						}
						for (List<SceneCallback> l : List.copyOf(this.sceneCallbacks.values()))
							for (SceneCallback c : List.copyOf(l))
							{
								if (this.callbackExecutor.isShutdown()) return;
								c.loop();
							}
					} catch (Throwable t)
					{
						this.throwError(t, "Exception called during Main Loop");
					}
				},
				1_000 / this.mainModuleConfig.getFPS(),
				1_000 / this.mainModuleConfig.getFPS(), TimeUnit.MILLISECONDS);
	}

	@Override
	public void registerScene(Scene scene)
	{
		this.scenes.add(scene);
		this.sceneCallbacks.forEach((sceneCallbackGenerator,
									 sceneCallbacksForGenerator) -> {
			sceneCallbacksForGenerator.add(sceneCallbackGenerator.apply(scene));
		});
	}

	@Override
	public void runEntrypoints()
	{
		this.loader.prepareModInit(Paths.get(System.getProperty("user.dir")), this);
		try
		{
			this.callDependents("main", MainModuleInitializer.class,
					(e) -> e.onInitialize(this), this.logger);
			this.initialized = true;
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public <E> void callDependents(String name,
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
			this.getCurvedSpacetimeLoader().invokeEntrypoints(name, moduleInitializerClass,
					(moduleInitializer) ->
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

	@Override
	public CompletableFuture<Void> registerMainCallback(MainCallback mainCallback)
	{
		if (!this.callbackExecutor.isShutdown())
		{
			return CompletableFuture.runAsync(() -> {
						this.mainCallbacks.add(mainCallback);
						mainCallback.init();
					}, this.callbackExecutor)
					.whenCompleteAsync((Void _, Throwable t) -> {
						if (t != null)
						{
							this.throwError(t, "Exception called during Main Callback Initialization");
						}
					}, this.callbackExecutor);
		} else
		{
			this.logger.info("Attempted to register Main Callback after shutdown, ignoring.");
			return CompletableFuture.completedFuture(null);
		}
	}

	@Override
	public CompletableFuture<Void> registerSceneCallbackGenerator(Function<Scene, SceneCallback> sceneCallbackGenerator)
	{
		if (!this.callbackExecutor.isShutdown())
		{
			return CompletableFuture.runAsync(() -> {
						this.sceneCallbacks.put(sceneCallbackGenerator,
								this.scenes.stream().map(sceneCallbackGenerator)
										.collect(Collectors.toCollection(ArrayList::new)));
						this.sceneCallbacks.get(sceneCallbackGenerator).forEach(SceneCallback::init);
					}, this.callbackExecutor)
					.whenCompleteAsync((Void _, Throwable t) -> {
						if (t != null)
						{
							this.throwError(t, "Exception called during Scene Callback Generation");
						}
					}, this.callbackExecutor);
		} else
		{
			this.logger.info("Attempted to register Scene Callback Generator after shutdown, ignoring.");
			return CompletableFuture.completedFuture(null);
		}
	}

	@Override
	public void clean()
	{
		if (this.callbackLoopHandler != null)
			this.callbackLoopHandler.cancel(false);
		for (Iterator<MainCallback> it = this.mainCallbacks.iterator(); it.hasNext(); )
		{
			MainCallback mainCallback = it.next();
			mainCallback.clean();
			it.remove();
		}
		for (Iterator<Entry<Function<Scene, SceneCallback>, List<SceneCallback>>> it =
			 this.sceneCallbacks.entrySet().iterator(); it.hasNext(); )
		{
			Entry<Function<Scene, SceneCallback>, List<SceneCallback>> entry = it.next();
			for (SceneCallback sceneCallback : entry.getValue())
			{
				sceneCallback.clean();
			}
			it.remove();
		}
		if (!this.callbackExecutor.isShutdown())
		{
			this.callbackExecutor.shutdown();
		}
	}

	@Override
	public void throwError(Throwable t, String message)
	{
		this.logger.log(Level.SEVERE, message, t);
		this.clean(); // TODO: Implement Error GUI
	}

	@Override
	public ConsoleHandler getConsoleHandler()
	{
		return consoleHandler;
	}

	@Override
	public boolean isInitialized()
	{
		return initialized;
	}

	@Override
	public Level getLoggerLevel()
	{
		return this.mainModuleConfig.getLoggerLevel();
	}

	@Override
	public int getFPS()
	{
		return this.mainModuleConfig.getFPS();
	}
}

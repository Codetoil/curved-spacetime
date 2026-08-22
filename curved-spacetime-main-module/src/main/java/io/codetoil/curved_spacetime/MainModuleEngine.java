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

/**
 * The engine: it owns the configuration, the scenes, and the callback loop, and it drives module
 * initialization.
 * <p>
 * Constructing an engine is what starts the program. The constructor loads the main
 * configuration, registers the initial scene, runs every module's {@code main} entrypoint, and
 * then begins the callback loop — so by the time it returns, the whole module graph is up.
 * <p>
 * Callbacks all run on a single scheduled thread, so implementations need no synchronisation
 * against one another. Module initialization is the opposite: entrypoints run concurrently on an
 * unbounded pool, which the dependency handshake requires — see
 * {@link #callDependents(String, Class, Consumer, Logger)}.
 */
public class MainModuleEngine
{
	/**
	 * The running engine, set as soon as construction begins so that entrypoints can reach it.
	 */
	protected static MainModuleEngine INSTANCE;

	/**
	 * The engine's settings, loaded before any module runs.
	 */
	public final MainModuleConfig mainModuleConfig;

	/**
	 * The logger the engine writes its own diagnostics to.
	 */
	public final Logger logger = Logger.getLogger("Curved Spacetime Main Module Logger");

	/**
	 * The single thread every callback runs on.
	 */
	protected final ScheduledExecutorService callbackExecutor;

	/**
	 * The loader module entrypoints are resolved through.
	 */
	protected final CurvedSpacetimeLoader loader;
	private final List<MainCallback> mainCallbacks = new ArrayList<>();
	private final Map<Function<Scene, SceneCallback>, List<SceneCallback>> sceneCallbacks = new HashMap<>();
	private final List<Scene> scenes = new ArrayList<>();
	/**
	 * Tracks the one-off task that initialises every registered callback.
	 */
	protected Future<?> callbackInitializeHandler;

	/**
	 * Tracks the repeating task that drives every registered callback, cancelled by {@link #stop()}.
	 */
	protected ScheduledFuture<?> callbackLoopHandler;

	/**
	 * Creates the engine and brings the whole module graph up.
	 * <p>
	 * Loads the main configuration, registers the initial scene, runs every {@code main}
	 * entrypoint, and starts the callback loop at the configured frame rate.
	 *
	 * @param loader the loader that resolves module entrypoints
	 * @throws RuntimeException if the main configuration cannot be loaded, or if any module's
	 *                          entrypoint fails
	 */
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

	/**
	 * Adds a scene and gives every registered generator a callback for it.
	 *
	 * @param scene the scene to add
	 */
	public void registerScene(Scene scene)
	{
		this.scenes.add(scene);
		this.sceneCallbacks.forEach((sceneCallbackGenerator,
									 sceneCallbacksForGenerator) -> {
			sceneCallbacksForGenerator.add(sceneCallbackGenerator.apply(scene));
		});
	}

	/**
	 * Prepares the loader and runs every module's {@code main} entrypoint.
	 *
	 * @throws RuntimeException wrapping whatever a module's entrypoint threw
	 */
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

	/**
	 * Invokes every entrypoint registered under the given name, concurrently, and waits for all of
	 * them.
	 * <p>
	 * The pool this uses is unbounded, and must remain so. The dependency handshake blocks a
	 * consumer in {@code TransferQueue.take} while its producer is still to run, so a producer and
	 * its consumer have to be runnable at the same time; a bounded pool deadlocks as soon as its
	 * threads fill with consumers whose producers are still queued.
	 * <p>
	 * The first entrypoint to fail aborts the whole call rather than leaving a half-initialized
	 * module graph running.
	 *
	 * @param <E>                    the entrypoint type
	 * @param name                   the entrypoint name to invoke, such as {@code main}
	 * @param moduleInitializerClass the type registered entrypoints must be assignable to
	 * @param onInitialize           the action to apply to each entrypoint
	 * @param logger                 the logger to trace dispatch through
	 * @throws Throwable whatever the first failing entrypoint threw
	 */
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

	/**
	 * Returns the running engine.
	 *
	 * @return the engine, or {@code null} before one has been constructed
	 */
	public static MainModuleEngine getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Returns the loader this engine resolves entrypoints through.
	 *
	 * @return the loader
	 */
	public CurvedSpacetimeLoader getCurvedSpacetimeLoader()
	{
		return loader;
	}

	/**
	 * Registers scene-independent work and initialises it on the callback thread.
	 *
	 * @param mainCallback the callback to register
	 */
	public void registerMainCallback(MainCallback mainCallback)
	{
		callbackExecutor.submit(() -> {
			this.mainCallbacks.add(mainCallback);
			mainCallback.init();
		});
	}

	/**
	 * Registers a factory that produces one callback per scene.
	 * <p>
	 * The generator is applied to every scene that exists now, and to each one registered later,
	 * so a module never has to track scenes itself.
	 *
	 * @param sceneCallbackGenerator the factory producing a callback for a given scene
	 */
	public void registerSceneCallbackGenerator(Function<Scene, SceneCallback> sceneCallbackGenerator)
	{
		callbackExecutor.submit(() -> {
			this.sceneCallbacks.put(sceneCallbackGenerator, this.scenes.stream().map(sceneCallbackGenerator).toList());
			this.sceneCallbacks.get(sceneCallbackGenerator).forEach(SceneCallback::init);
		});
	}

	/**
	 * Stops the callback loop and shuts the engine down.
	 */
	public void stop()
	{
		this.callbackLoopHandler.cancel(true);
		this.clean();
	}

	/**
	 * Shuts the callback executor down and releases every scene callback.
	 */
	public void clean()
	{
		this.callbackExecutor.shutdown();
		this.sceneCallbacks.forEach((_, sceneCallbacks) ->
				sceneCallbacks.forEach(SceneCallback::clean));
	}
}

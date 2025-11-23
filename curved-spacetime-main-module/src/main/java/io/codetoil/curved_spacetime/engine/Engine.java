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

package io.codetoil.curved_spacetime.engine;

import com.google.common.collect.Sets;
import io.codetoil.curved_spacetime.*;
import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Future.State;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Engine
{
	protected static Engine INSTANCE;
	public final MainModuleConfig mainModuleConfig;
	protected final ScheduledExecutorService callbackExecutor;
	protected final CurvedSpacetimeLoader loader;
	protected final Set<Function<?, ?>> callbackSuppliers = Sets.newConcurrentHashSet();
	private final Set<MainCallback> mainCallbacks = Sets.newConcurrentHashSet();
	private final Set<SceneCallback> sceneCallbacks = Sets.newConcurrentHashSet();
	protected final Set<Scene> scenes = Sets.newConcurrentHashSet();
	protected Future<?> callbackInitializeHandler;
	protected ScheduledFuture<?> callbackLoopHandler;

	private Engine(CurvedSpacetimeLoader loader)
	{
		INSTANCE = this;
		this.loader = loader;
		try
		{
			this.mainModuleConfig = new MainModuleConfig().load();
			if (this.mainModuleConfig.isDirty()) this.mainModuleConfig.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load API Config", ex);
		}
		this.scenes.add(new Scene());
		this.callbackExecutor = Executors.newSingleThreadScheduledExecutor();
		Logger.info("Running Entrypoints in parallel");
		this.runEntrypoints();
		Logger.info("Initializing Callbacks");
		this.callbackInitializeHandler = this.callbackExecutor.submit(() ->
		{
			accumulateCallbacks(
					MainCallbackSupplier.class,
					Sets.newHashSet((Void) null),
					this.mainCallbacks);
			this.mainCallbacks.forEach(MainCallback::init);

			accumulateCallbacks(SceneCallbackSupplier.class, this.scenes, this.sceneCallbacks);
			this.sceneCallbacks.forEach(SceneCallback::init);
		});
		this.callbackLoopHandler = this.callbackExecutor.scheduleAtFixedRate(() ->
				{
					this.mainCallbacks.forEach(MainCallback::loop);
					this.sceneCallbacks.forEach(SceneCallback::loop);
				},
				1_000 / this.mainModuleConfig.getFPS(),
				1_000 / this.mainModuleConfig.getFPS(), TimeUnit.MILLISECONDS);
	}

	public void runEntrypoints()
	{
		this.loader.prepareModInit(Paths.get(System.getProperty("user.dir")), this);
		try
		{
			Engine.callDependents("main", ModuleInitializer.class,
					ModuleInitializer::onInitialize);
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <E> void callDependents(String name,
										  Class<E> moduleInitializerClass,
										  Consumer<E> onInitialize)
			throws Throwable
	{
		Logger.trace("Dependents of {}: {}\n", name,
				INSTANCE.loader.getEntrypoints(name, moduleInitializerClass));
		try (ExecutorService moduleInitializerThreadPool = Executors.newCachedThreadPool())
		{
			CompletionService<?> completionService = new ExecutorCompletionService<>(moduleInitializerThreadPool);
			List<Future<?>> futures = new ArrayList<>();
			INSTANCE.loader.invokeEntrypoints(name, moduleInitializerClass, moduleInitializer ->
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

	public static void start(String[] args, CurvedSpacetimeLoader loader)
	{
		INSTANCE = new Engine(loader);
	}

	public static Engine getInstance()
	{
		return INSTANCE;
	}

	public CurvedSpacetimeLoader getCurvedSpacetimeLoader()
	{
		return loader;
	}

	public <A, C> void addCallbackSupplier(Function<A, C> callbackSupplier)
	{
		this.callbackSuppliers.add(callbackSupplier);
	}

	public <A, C> void registerCallbackAndInit(Function<A, C> callbackSupplier, Set<A> argsSet, Set<C> callbacks, Consumer<C> init)
	{
		if (!callbackSuppliers.contains(callbackSupplier))
			addCallbackSupplier(callbackSupplier);
		argsSet.forEach(args -> {
			C callback = callbackSupplier.apply(args);
			callbacks.add(callback);
			init.accept(callback);
		});
	}

	public void registerMainCallbackAndInit(Supplier<MainCallback> callbackSupplier)
	{
		this.registerCallbackAndInit((Void _) -> callbackSupplier.get(),
				Sets.newHashSet((Void) null), this.mainCallbacks,
				MainCallback::init);
	}

	public void registerSceneCallbackAndInit(Function<Scene, SceneCallback> callbackSupplier)
	{
		this.registerCallbackAndInit(callbackSupplier, this.scenes,
				this.sceneCallbacks, SceneCallback::init);
	}

	public <A, C, S extends Function<A, C>> void accumulateCallbacks(Class<S> supplierClass, Set<A> argsSet,
																	 Set<C> callbacks)
	{
		argsSet.forEach(args ->
				this.callbackSuppliers.stream()
						.filter(supplierClass::isInstance)
						.map(supplierClass::cast)
						.map(supplier -> supplier.apply(args))
						.forEach(callbacks::add));
	}

	public void deregisterScene(Scene scene)
	{
		this.sceneCallbacks.stream()
				.filter(callback -> Objects.equals(callback.scene(), scene))
				.toList().forEach(callback -> {
					callback.clean();
					this.sceneCallbacks.remove(callback);
				});
		this.scenes.remove(scene);
	}

	public void stop()
	{
		this.callbackLoopHandler.cancel(true);
		this.clean();
	}

	protected void clean()
	{
		this.scenes.forEach(this::deregisterScene);
		this.callbackExecutor.shutdown();
	}

	public Set<Scene> getScenes()
	{
		return scenes;
	}
}

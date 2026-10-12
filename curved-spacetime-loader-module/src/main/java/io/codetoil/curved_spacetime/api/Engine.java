package io.codetoil.curved_spacetime.api;

import io.codetoil.curved_spacetime.api.scene.Scene;
import io.codetoil.curved_spacetime.api.scene.SceneCallback;
import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The engine: it owns the configuration, the scenes, and the callback loop, and it drives module initialization.
 * <p>
 * Constructing an engine is what starts the program. The constructor loads the main configuration, registers the
 * initial scene, runs every module's {@code main} entrypoint, and then begins the callback loop — so by the time it
 * returns, the whole module graph is up.
 * <p>
 * Callbacks all run on a single scheduled thread, so implementations need no synchronisation against one another.
 * Module initialization is the opposite: entrypoints run concurrently on an unbounded pool, which the dependency
 * handshake requires — see {@link #callDependents(String, Class, Consumer, Logger)}.
 */
public abstract class Engine
{
	/**
	 * The loader module entrypoints are resolved through
	 */
	protected final CurvedSpacetimeLoader loader;

	/**
	 * Constructor
	 *
	 * @param loader
	 * 		loader
	 */
	public Engine(CurvedSpacetimeLoader loader)
	{
		this.loader = loader;
	}

	/**
	 * Adds a scene and gives every registered generator a callback for it.
	 *
	 * @param scene
	 * 		the scene to add
	 */
	public abstract void registerScene(Scene scene);

	/**
	 * Invokes every entrypoint registered under the given name, concurrently, and waits for all of them.
	 * <p>
	 * The pool this uses is unbounded, and must remain so. The dependency handshake blocks a consumer in
	 * {@code TransferQueue.take} while its producer is still to run, so a producer and its consumer have to be runnable
	 * at the same time; a bounded pool deadlocks as soon as its threads fill with consumers whose producers are still
	 * queued.
	 * <p>
	 * The first entrypoint to fail aborts the whole call rather than leaving a half-initialized module graph running.
	 *
	 * @param <E>
	 * 		the entrypoint type
	 * @param name
	 * 		the entrypoint name to invoke, such as {@code main}
	 * @param moduleInitializerClass
	 * 		the type registered entrypoints must be assignable to
	 * @param onInitialize
	 * 		the action to apply to each entrypoint
	 * @param logger
	 * 		the logger to trace dispatch through
	 *
	 * @throws Throwable
	 * 		whatever the first failing entrypoint threw
	 */
	public abstract <E> void callDependents(String name,
											Class<E> moduleInitializerClass,
											Consumer<E> onInitialize,
											Logger logger) throws Throwable;

	/**
	 * Prepares the loader and runs every module's {@code main} entrypoint.
	 *
	 * @throws RuntimeException
	 * 		wrapping whatever a module's entrypoint threw
	 */
	public abstract void runEntrypoints();

	/**
	 * Returns the loader this engine resolves entrypoints through.
	 *
	 * @return the loader
	 */
	public CurvedSpacetimeLoader getCurvedSpacetimeLoader()
	{
		return this.loader;
	}

	/**
	 * Registers scene-independent work and initialises it on the callback thread.
	 *
	 * @param mainCallback
	 * 		the callback to register
	 *
	 * @return the completable future generated for this task
	 */
	public abstract CompletableFuture<Void> registerMainCallback(MainCallback mainCallback);

	/**
	 * Registers a factory that produces one callback per scene.
	 * <p>
	 * The generator is applied to every scene that exists now, and to each one registered later, so a module never has
	 * to track scenes itself.
	 *
	 * @param sceneCallbackGenerator
	 * 		the factory producing a callback for a given scene
	 *
	 * @return the completable future generated for this task
	 */
	public abstract CompletableFuture<Void> registerSceneCallbackGenerator(
			Function<Scene, SceneCallback> sceneCallbackGenerator);

	/**
	 * Stops the callback loop, releases every callback, and then shuts the callback executor down.
	 */
	public abstract void clean();

	/**
	 * Throws an error and stops the engine.
	 *
	 * @param t
	 * 		the error to throw
	 * @param message
	 * 		the error message
	 */
	public abstract void throwError(Throwable t, String message);

	/**
	 * Returns the console handler used for the Curved Spacetime Loggers.
	 *
	 * @return the console handler
	 */
	public abstract ConsoleHandler getConsoleHandler();

	/**
	 * Returns whether the Engine is initialized. Note that this doesn't mean all callbacks are registered, As a
	 * callback can be registered at any time and is registered asynchronously.
	 *
	 * @return {@code true} if the engine is initialized
	 */
	public abstract boolean isInitialized();

	/**
	 * Log Level
	 *
	 * @return level
	 */
	public abstract Level getLoggerLevel();

	/**
	 * FPS
	 *
	 * @return FPS
	 */
	public abstract int getFPS();
}

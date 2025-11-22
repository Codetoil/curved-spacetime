package io.codetoil.curved_spacetime.api.loader;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface CurvedSpacetimeLoader
{
	void prepareModInit(Path path, Object engine);

	<E> List<E> getEntrypoints(String name, Class<E> moduleInitializerClass);

	<E> void invokeEntrypoints(String name, Class<E> moduleInitializerClass,
							   Consumer<? super E> moduleInitializerConsumer);

	Object getEngine();
}

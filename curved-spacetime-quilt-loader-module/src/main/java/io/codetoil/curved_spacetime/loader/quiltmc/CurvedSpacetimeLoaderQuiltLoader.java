package io.codetoil.curved_spacetime.loader.quiltmc;

import io.codetoil.curved_spacetime.api.loader.CurvedSpacetimeLoader;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.quiltmc.loader.impl.QuiltLoaderImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class CurvedSpacetimeLoaderQuiltLoader implements CurvedSpacetimeLoader
{
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

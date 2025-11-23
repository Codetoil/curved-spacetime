package io.codetoil.curved_spacetime;

import io.codetoil.curved_spacetime.engine.Scene;

import java.util.function.Function;

public record SceneCallbackSupplier(Function<Scene, SceneCallback> supplier)
		implements Function<Scene, SceneCallback>
{
	public SceneCallback apply(Scene args)
	{
		return supplier.apply(args);
	}
}

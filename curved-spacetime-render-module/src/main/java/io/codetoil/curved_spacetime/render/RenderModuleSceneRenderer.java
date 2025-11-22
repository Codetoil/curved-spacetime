package io.codetoil.curved_spacetime.render;

import io.codetoil.curved_spacetime.scene.Scene;
import io.codetoil.curved_spacetime.scene.SceneCallback;

public abstract class RenderModuleSceneRenderer implements SceneCallback
{
	protected final Scene scene;

	protected RenderModuleSceneRenderer(Scene scene)
	{
		this.scene = scene;
	}

	public abstract void init();

	public abstract void loop();

	public abstract void clean();

	@Override
	public Scene scene()
	{
		return this.scene;
	}
}

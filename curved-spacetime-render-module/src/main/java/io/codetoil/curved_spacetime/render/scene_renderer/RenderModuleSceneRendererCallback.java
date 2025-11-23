package io.codetoil.curved_spacetime.render.scene_renderer;

public interface RenderModuleSceneRendererCallback
{

	void init();

	void loop();

	void clean();

	RenderModuleSceneRenderer renderEnvironment();
}

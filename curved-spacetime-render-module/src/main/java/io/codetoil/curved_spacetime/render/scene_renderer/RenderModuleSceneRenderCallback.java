package io.codetoil.curved_spacetime.render.scene_renderer;

public interface RenderModuleSceneRenderCallback
{

	void init();

	void loop();

	void clean();

	RenderModuleSceneRenderer renderEnviornment();
}

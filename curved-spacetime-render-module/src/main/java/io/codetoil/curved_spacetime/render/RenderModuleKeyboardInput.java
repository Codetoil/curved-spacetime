package io.codetoil.curved_spacetime.render;

public interface RenderModuleKeyboardInput
{
	RenderModuleWindow window();

	void addKeyCallBack(KeyCallback callback);

	void poll();

	void clean();

	boolean keyPressed(KeyCtx keyCtx);

	boolean keyTapped(KeyCtx keyCtx);

	interface KeyCallback
	{
		void invoke(KeyCtx keyCtx);
	}

	interface KeyCtx
	{

	}
}

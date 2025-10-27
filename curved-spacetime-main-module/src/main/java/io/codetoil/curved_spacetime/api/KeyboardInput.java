package io.codetoil.curved_spacetime.api;

public interface KeyboardInput
{
	Window window();

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

package io.codetoil.curved_spacetime.render;

public interface RenderModuleMouseInput
{
	RenderModuleWindow window();

	void poll();

	float getCurrentX();

	float getCurrentY();

	float getDeltaX();

	float getDeltaY();

	boolean isLeftButtonPressed();

	boolean isRightButtonPressed();

	boolean isMiddleButtonPressed();
}

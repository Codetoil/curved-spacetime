package io.codetoil.curved_spacetime.api;

public interface MouseInput
{
	Window window();

	void poll();

	float getCurrentX();

	float getCurrentY();

	float getDeltaX();

	float getDeltaY();

	boolean isLeftButtonPressed();

	boolean isRightButtonPressed();

	boolean isMiddleButtonPressed();
}

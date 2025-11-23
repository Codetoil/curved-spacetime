package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.render.RenderModuleMouseInput;
import org.lwjgl.glfw.GLFW;

public class GLFWRenderModuleMouseInput implements RenderModuleMouseInput
{
	protected final GLFWRenderModuleWindow window;
	protected float currentX = -1.0f;
	protected float currentY = -1.0f;
	protected float deltaX = 0.0f;
	protected float deltaY = 0.0f;
	protected float previousX = 0.0f;
	protected float previousY = 0.0f;
	protected boolean inWindow = false;
	protected boolean leftButtonPressed = false;
	protected boolean rightButtonPressed = false;
	protected boolean middleButtonPressed = false;

	public GLFWRenderModuleMouseInput(GLFWRenderModuleWindow window)
	{
		this.window = window;
		GLFW.glfwSetCursorPosCallback(this.window.windowHandle, (handle, xpos, ypos) -> {
			this.currentX = (float) xpos;
			this.currentY = (float) ypos;
		});
		GLFW.glfwSetCursorEnterCallback(this.window.windowHandle, (handle, entered) -> inWindow = entered);
		GLFW.glfwSetMouseButtonCallback(this.window.windowHandle, (handle, button, action, mode) -> {
			leftButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
			rightButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS;
			leftButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
			middleButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_3 && action == GLFW.GLFW_PRESS;
		});
	}

	@Override
	public GLFWRenderModuleWindow window()
	{
		return this.window;
	}

	@Override
	public void poll()
	{
		this.deltaX = 0.0f;
		this.deltaY = 0.0f;
		if (previousX >= 0.0f && previousY >= 0.0f && inWindow)
		{
			this.deltaX = currentX - previousX;
			this.deltaY = currentY - previousY;
		}
		this.previousX = currentX;
		this.previousY = currentY;
	}

	public float getCurrentX()
	{
		return currentX;
	}

	public float getCurrentY()
	{
		return currentY;
	}

	public float getDeltaX()
	{
		return deltaX;
	}

	public float getDeltaY()
	{
		return deltaY;
	}

	public boolean isLeftButtonPressed()
	{
		return leftButtonPressed;
	}

	public boolean isRightButtonPressed()
	{
		return rightButtonPressed;
	}

	public boolean isMiddleButtonPressed()
	{
		return middleButtonPressed;
	}
}

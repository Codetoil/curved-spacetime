package io.codetoil.curved_spacetime.render.glfw_ffm;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.render.RenderModuleMouseInput;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;

public class RenderGLFWFFMModuleMouseInput implements RenderModuleMouseInput
{
	protected final RenderGLFWFFMModuleWindow window;
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

	public RenderGLFWFFMModuleMouseInput(RenderGLFWFFMModuleWindow window)
	{
		this.window = window;
		CurvedSpacetimeRenderGLFWFFMModuleEntrypoint.glfw.glfwSetCursorPosCallback(MainModuleEngine.getInstance().nativeAllocator,
				this.window.window, (long _, double xPos, double yPos) -> {
					this.currentX = (float) xPos;
					this.currentY = (float) yPos;
				});
		CurvedSpacetimeRenderGLFWFFMModuleEntrypoint.glfw.glfwSetCursorEnterCallback(MainModuleEngine.getInstance().nativeAllocator,
				this.window.window, (long _, int entered) ->
						this.inWindow = entered == GLFW.GLFW_TRUE());
		CurvedSpacetimeRenderGLFWFFMModuleEntrypoint.glfw.glfwSetMouseButtonCallback(MainModuleEngine.getInstance().nativeAllocator,
				this.window.window, (long _, int button, int action, int _) ->
				{
					this.leftButtonPressed =
							button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
					this.rightButtonPressed =
							button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS;
					this.middleButtonPressed =
							button == GLFW.GLFW_MOUSE_BUTTON_3 && action == GLFW.GLFW_PRESS;
				});
	}

	@Override
	public RenderModuleWindow window()
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

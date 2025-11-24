package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.render.RenderModuleKeyboardInput;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLFWRenderModuleKeyboardInput implements RenderModuleKeyboardInput, GLFWKeyCallbackI
{
	protected final Map<Integer, Boolean> tappedKeyMap;
	protected final GLFWRenderModuleWindow window;
	protected final List<KeyCallback> callbacks;

	public GLFWRenderModuleKeyboardInput(GLFWRenderModuleWindow window)
	{
		this.window = window;
		tappedKeyMap = new HashMap<>();
		GLFW.glfwSetKeyCallback(this.window.windowHandle, this);
		callbacks = new ArrayList<>();
	}

	@Override
	public RenderModuleWindow window()
	{
		return this.window;
	}

	public void addKeyCallBack(KeyCallback callback)
	{
		callbacks.add(callback);
	}

	@Override
	public void poll()
	{
		GLFW.glfwPollEvents();
	}

	@Override
	public void clean()
	{
		tappedKeyMap.clear();
	}

	@Override
	public boolean keyPressed(RenderModuleKeyboardInput.KeyCtx keyCtx)
	{
		return GLFW.glfwGetKey(this.window.windowHandle, ((KeyCtx) keyCtx).keycode()) == GLFW.GLFW_PRESS;
	}

	@Override
	public boolean keyTapped(RenderModuleKeyboardInput.KeyCtx keyCtx)
	{
		Boolean value = tappedKeyMap.get(((KeyCtx) keyCtx).keycode());
		return value != null && value;
	}

	@Override
	public void invoke(long handle, int keyCode, int scanCode, int action, int mods)
	{
		if (handle != this.window.windowHandle) return;
		tappedKeyMap.put(keyCode, action == GLFW.GLFW_PRESS);
		for (KeyCallback callback : callbacks)
		{
			callback.invoke(new KeyCtx(keyCode, scanCode, action, mods));
		}
	}

	public record KeyCtx(int keycode, int scanCode, int action, int mods) implements RenderModuleKeyboardInput.KeyCtx
	{

	}
}

package io.codetoil.curved_spacetime.glfw;

import io.codetoil.curved_spacetime.KeyboardInput;
import io.codetoil.curved_spacetime.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLFWKeyboardInput implements KeyboardInput, GLFWKeyCallbackI
{
	protected final Map<Integer, Boolean> tappedKeyMap;
	protected final GLFWWindow window;
	protected final List<KeyCallback> callbacks;

	public GLFWKeyboardInput(GLFWWindow window)
	{
		this.window = window;
		tappedKeyMap = new HashMap<>();
		GLFW.glfwSetKeyCallback(this.window.windowHandle, this);
		callbacks = new ArrayList<>();
	}

	@Override
	public Window window()
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
	public boolean keyPressed(KeyboardInput.KeyCtx keyCtx)
	{
		return GLFW.glfwGetKey(this.window.windowHandle, ((KeyCtx) keyCtx).keycode()) == GLFW.GLFW_PRESS;
	}

	@Override
	public boolean keyTapped(KeyboardInput.KeyCtx keyCtx)
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

	public record KeyCtx(int keycode, int scanCode, int action, int mods) implements KeyboardInput.KeyCtx
	{

	}
}

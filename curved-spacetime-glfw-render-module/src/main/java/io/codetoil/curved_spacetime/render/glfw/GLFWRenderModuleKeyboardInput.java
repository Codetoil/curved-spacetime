package io.codetoil.curved_spacetime.render.glfw;

import glfw3.GLFW;
import glfw3.GLFWkeyfun;
import io.codetoil.curved_spacetime.render.RenderModuleKeyboardInput;
import io.codetoil.curved_spacetime.render.RenderModuleWindow;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GLFWRenderModuleKeyboardInput implements RenderModuleKeyboardInput
{
	protected final Map<Integer, Boolean> tappedKeyMap;
	protected final GLFWRenderModuleWindow window;
	protected final List<KeyCallback> callbacks;

	public GLFWRenderModuleKeyboardInput(GLFWRenderModuleWindow window)
	{
		this.window = window;
		tappedKeyMap = new HashMap<>();
		callbacks = new ArrayList<>();
		GLFW.glfwSetKeyCallback(this.window.window,
				GLFWkeyfun.allocate((MemorySegment window1, int keyCode, int scanCode,
									 int action, int mods) ->
				{
					if (window1 != this.window.window) return;
					tappedKeyMap.put(keyCode, action == GLFW.GLFW_PRESS());
					for (KeyCallback callback : callbacks)
					{
						callback.invoke(new KeyCtx(keyCode, scanCode, action, mods));
					}
				}, window.arena));
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
		return GLFW.glfwGetKey(this.window.window, ((KeyCtx) keyCtx).keycode()) == GLFW.GLFW_PRESS();
	}

	@Override
	public boolean keyTapped(RenderModuleKeyboardInput.KeyCtx keyCtx)
	{
		Boolean value = tappedKeyMap.get(((KeyCtx) keyCtx).keycode());
		return value != null && value;
	}

	public record KeyCtx(int keycode, int scanCode, int action, int mods) implements RenderModuleKeyboardInput.KeyCtx
	{

	}
}

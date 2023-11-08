package io.github.codetoil.curved_spacetime.render.vulkan;

import io.github.codetoil.curved_spacetime.api.engine.Engine;
import io.github.codetoil.curved_spacetime.render.glfw.GLFWWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVulkan;

public class VulkanWindow extends GLFWWindow {
    public VulkanWindow(Engine engine) {
        super(engine);
    }

    @Override
    public boolean isSupported() {
        return GLFWVulkan.glfwVulkanSupported();
    }

    @Override
    protected void throwUnsupportedException() {
        throw new IllegalStateException("Cannot find a compatible Vulkan installable client driver (ICD)");
    }

    /**
     *
     */
    @Override
    protected void setWindowHints() {
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE); // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API); // Do not use either OpenGL nor OpenGL ES
    }
}

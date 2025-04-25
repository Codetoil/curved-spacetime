/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2023 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<br>
 */

package io.codetoil.curved_spacetime.render.glfw;

import io.codetoil.curved_spacetime.api.engine.Engine;
import io.codetoil.curved_spacetime.api.render.Window;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;

public abstract class GLFWWindow extends Window {
    public final GLFWRenderConfig GLFWRenderConfig;
    protected long windowHandle;
    protected int width;
    protected int height;

    protected GLFWWindow(Engine engine)
    {
        super(engine);
        try {
            this.GLFWRenderConfig = new GLFWRenderConfig().load();
            if (this.GLFWRenderConfig.isDirty()) this.GLFWRenderConfig.save();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load GLFW Render Config", ex);
        }
    }

    public void init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !GLFW.glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        assert vidMode != null;
        this.width = vidMode.width();
        this.height = vidMode.height();

        if (!this.doesDriverExist()) {
            throwDriverNotFoundException();
        }

        // Configure GLFW
        setWindowHints();

        // Create the window
        this.windowHandle = GLFW.glfwCreateWindow(620, 480, "Curved Spacetime",
                MemoryUtil.NULL, MemoryUtil.NULL);
        if ( this.windowHandle == MemoryUtil.NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.

        GLFW.glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
            if ( key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE )
                GLFW.glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
    }

    public void loop() {
        // Poll for window events. The key callback above will only be
        // invoked during this call.
        GLFW.glfwPollEvents();
        if (GLFW.glfwWindowShouldClose(this.windowHandle)) {
            this.engine.stop();
        }
    }

    public abstract boolean doesDriverExist();
    protected abstract void throwDriverNotFoundException();

    protected abstract void setWindowHints();

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void showWindow() {
        GLFW.glfwShowWindow(this.windowHandle);
    }

    public void hideWindow() {
        GLFW.glfwDestroyWindow(this.windowHandle);
    }

    public void clean() {
        Callbacks.glfwFreeCallbacks(this.windowHandle);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }
}

package io.codetoil.curved_spacetime.render.glfw.ffm;

import org.lwjgl.system.ffm.*;

import java.lang.foreign.Arena;

import static org.lwjgl.system.ffm.FFM.*;

@FFMPrefix("glfw")
@FFMCharset(FFMCharset.Type.UTF8)
public interface FFMGLFWRenderModuleGLFW
{
	@FFMDefinition("void (* GLFWkeyfun)(GLFWwindow * window, int key, int scancode, int action, int mods);")
	@FunctionalInterface
	interface GLFWkeyfun {
		UpcallBinder<GLFWkeyfun> $ = ffmUpcall(GLFWkeyfun.class);
		void invoke(@FFMPointer long window, int key, int scancode, int action, int mods);
	}

	@FFMDefinition("GLFWkeyfun glfwSetKeyCallback(GLFWwindow * window, GLFWkeyfun cbfun);")
	@FFMPointer long glfwSetKeyCallback(@FFMPointer long window, @FFMNullable @FFMPointer long callback);

	@FFMDefinition("GLFWkeyfun glfwSetKeyCallback(GLFWwindow * window, GLFWkeyfun cbfun);")
	@FFMPointer long glfwSetKeyCallback(Arena arena, @FFMPointer long window, @FFMNullable GLFWkeyfun callback);

	@FFMDefinition("void(* GLFWcursorposfun) (GLFWwindow * window, double xpos, double ypos);")
	@FunctionalInterface
	interface GLFWcursorposfun {
		UpcallBinder<GLFWcursorposfun> $ = ffmUpcall(GLFWcursorposfun.class);
		void invoke(@FFMPointer long window, double xpos, double ypos);
	}

	@FFMDefinition("GLFWcursorposfun glfwSetCursorPosCallback (GLFWwindow *window, GLFWcursorposfun cbfun);")
	@FFMPointer long glfwSetCursorPosCallback(@FFMPointer long window, @FFMNullable @FFMPointer long callback);

	@FFMDefinition("GLFWcursorposfun glfwSetCursorPosCallback (GLFWwindow *window, GLFWcursorposfun cbfun);")
	@FFMPointer long glfwSetCursorPosCallback(Arena arena, @FFMPointer long window, @FFMNullable GLFWcursorposfun callback);


}

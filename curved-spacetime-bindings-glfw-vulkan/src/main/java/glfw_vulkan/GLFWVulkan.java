package glfw_vulkan;

import glfw3.GLFW;
import vulkan.Vulkan;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public class GLFWVulkan
{
	/**
	 * Function descriptor for:
	 * {@snippet lang = c:
	 * VkResult glfwCreateWindowSurface (VkInstance instance, GLFWwindow *window, const VkAllocationCallbacks *allocator, VkSurfaceKHR *surface)
	 *}
	 */
	public static FunctionDescriptor glfwCreateWindowSurface$descriptor()
	{
		return glfwCreateWindowSurface.DESC;
	}

	/**
	 * Downcall method handle for:
	 * {@snippet lang = c:
	 * VkResult glfwCreateWindowSurface (VkInstance instance, GLFWwindow *window, const VkAllocationCallbacks *allocator, VkSurfaceKHR *surface)
	 *}
	 */
	public static MethodHandle glfwCreateWindowSurface$handle()
	{
		return glfwCreateWindowSurface.HANDLE;
	}

	/**
	 * Address for:
	 * {@snippet lang = c:
	 * VkResult glfwCreateWindowSurface (VkInstance instance, GLFWwindow *window, const VkAllocationCallbacks *allocator, VkSurfaceKHR *surface)
	 *}
	 */
	public static MemorySegment glfwCreateWindowSurface$address()
	{
		return glfwCreateWindowSurface.ADDR;
	}

	/**
	 * {@snippet lang = c:
	 * VkResult glfwCreateWindowSurface (VkInstance instance, GLFWwindow *window, const VkAllocationCallbacks *allocator, VkSurfaceKHR *surface)
	 *}
	 **/
	public static MemorySegment glfwCreateWindowSurface(MemorySegment instance, MemorySegment window, MemorySegment allocator,
														MemorySegment surface)
	{
		var mh$ = glfwCreateWindowSurface.HANDLE;
		try
		{
			if (GLFW.TRACE_DOWNCALLS)
			{
				GLFW.traceDowncall("glfwCreateWindowSurface", instance, window, allocator, surface);
			}
			return (MemorySegment) mh$.invokeExact(instance, window, allocator, surface);
		} catch (Error | RuntimeException ex)
		{
			throw ex;
		} catch (Throwable ex$)
		{
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class glfwCreateWindowSurface
	{
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(
				Vulkan.C_INT,
				Vulkan.VkInstance,
				GLFW.C_POINTER,
				Vulkan.C_POINTER,
				Vulkan.C_POINTER
		);

		public static final MemorySegment ADDR = GLFW.SYMBOL_LOOKUP.findOrThrow("glfwCreateWindowSurface");

		public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
	}
}

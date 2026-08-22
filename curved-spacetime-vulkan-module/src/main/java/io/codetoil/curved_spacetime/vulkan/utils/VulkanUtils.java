/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br> it under the terms of the GNU General
 * Public License as published by <br> the Free Software Foundation, either version 3 of the License, or <br> (at your
 * option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br> but WITHOUT ANY WARRANTY; without even the
 * implied warranty of<br> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br> GNU General Public License
 * for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br> along with this program.  If not, see <a
 * href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.vulkan.utils;

import org.lwjgl.vulkan.*;

import java.util.Locale;

/**
 * Helpers shared across the Vulkan modules.
 */
public class VulkanUtils
{
	private VulkanUtils()
	{
		// Utility class
	}

	/**
	 * Identifies the host operating system.
	 * <p>
	 * Used to decide whether the portability extensions apply, since Vulkan on macOS runs over
	 * MoltenVK rather than a conformant driver.
	 *
	 * @return the host operating system, or {@link OSType#OTHER} if it is not recognised
	 */
	public static OSType getOS()
	{
		OSType result;
		String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((os.contains("mac")) || (os.contains("darwin")))
		{
			result = OSType.MACOS;
		} else if (os.contains("win"))
		{
			result = OSType.WINDOWS;
		} else if (os.contains("nux"))
		{
			result = OSType.LINUX;
		} else
		{
			result = OSType.OTHER;
		}

		return result;
	}

	/**
	 * Throws unless a Vulkan call succeeded.
	 * <p>
	 * Every Vulkan entry point returns a {@code VkResult}, and almost none of them are worth
	 * handling individually, so calls are wrapped in this instead. Anything other than
	 * {@code VK_SUCCESS} raises an {@link AssertionError} carrying the numeric result.
	 *
	 * @param err    the {@code VkResult} returned by the call
	 * @param errMsg a description of what was being attempted
	 * @throws AssertionError if {@code err} is anything other than {@code VK_SUCCESS}
	 */
	public static void vkCheck(int err, String errMsg)
	{
		String errCode = "Unmapped Error Code";
		// As provided by Vulkan Docs here: https://docs.vulkan.org/refpages/latest/refpages/source/VkResult.html
		String errDesc = "<No Description>";
		switch (err)
		{
			case VK13.VK_SUCCESS:
				errCode = "VK_SUCCESS";
				errMsg = "Command successfully completed";
			case VK13.VK_NOT_READY:
				errCode = "VK_NOT_READY";
				errMsg = "A fence or query has not yet completed";
			case VK13.VK_TIMEOUT:
				errCode = "VK_TIMEOUT";
				errMsg = "A wait operation has not completed in the specified time";
			case VK13.VK_EVENT_SET:
				errCode = "VK_EVENT_SET";
				errMsg = "An event is signaled";
			case VK13.VK_EVENT_RESET:
				errCode = "VK_EVENT_RESET";
				errMsg = "An event is unsignaled";
			case VK13.VK_INCOMPLETE:
				errCode = "VK_INCOMPLETE";
				errMsg = "A return array was too small for the result";
			case VK13.VK_ERROR_OUT_OF_HOST_MEMORY:
				errCode = "VK_ERROR_OUT_OF_HOST_MEMORY";
				errMsg = "A host memory allocation has failed.";
			case VK13.VK_ERROR_OUT_OF_DEVICE_MEMORY:
				errCode = "VK_ERROR_OUT_OF_DEVICE_MEMORY";
				errMsg = "A device memory allocation has failed.";
			case VK13.VK_ERROR_INITIALIZATION_FAILED:
				errCode = "VK_ERROR_INITIALIZATION_FAILED";
				errMsg = "Initialization of an object could not be completed for implementation-specific reasons.";
			case VK13.VK_ERROR_DEVICE_LOST:
				errCode = "VK_ERROR_DEVICE_LOST";
				errMsg = "The logical or physical device has been lost.";
			case VK13.VK_ERROR_MEMORY_MAP_FAILED:
				errCode = "VK_ERROR_MEMORY_MAP_FAILED";
				errMsg = "Mapping of a memory object has failed.";
			case VK13.VK_ERROR_LAYER_NOT_PRESENT:
				errCode = "VK_ERROR_LAYER_NOT_PRESENT";
				errMsg = "A requested layer is not present or could not be loaded.";
			case VK13.VK_ERROR_EXTENSION_NOT_PRESENT:
				errCode = "VK_ERROR_EXTENSION_NOT_PRESENT";
				errMsg = "A requested extension is not supported.";
			case VK13.VK_ERROR_FEATURE_NOT_PRESENT:
				errCode = "VK_ERROR_FEATURE_NOT_PRESENT";
				errMsg = "A requested feature is not supported.";
			case VK13.VK_ERROR_INCOMPATIBLE_DRIVER:
				errCode = "VK_ERROR_INCOMPATIBLE_DRIVER";
				errMsg =
						"The requested version of Vulkan is not supported by the driver or is otherwise incompatible for" +
								" implementation-specific reasons.";
			case VK13.VK_ERROR_TOO_MANY_OBJECTS:
				errCode = "VK_ERROR_TOO_MANY_OBJECTS";
				errMsg = "Too many objects of the type have already been created.";
			case VK13.VK_ERROR_FORMAT_NOT_SUPPORTED:
				errCode = "VK_ERROR_FORMAT_NOT_SUPPORTED";
				errMsg = "A requested format is not supported on this device.";
			case VK13.VK_ERROR_FRAGMENTED_POOL:
				errCode = "VK_ERROR_FRAGMENTED_POOL";
				errMsg = "A pool allocation has failed due to fragmentation of the pool's memory. This must only be" +
						" returned if no attempt to allocate host or device memory was made to accommodate the" +
						" new allocation. This should be returned in preference to VK_ERROR_OUT_OF_POOL_MEMORY," +
						" but only if the implementation is certain that the pool allocation failure was due to" +
						" fragmentation.";
			case VK13.VK_ERROR_UNKNOWN:
				errCode = "VK_ERROR_UNKNOWN";
				errMsg = "An unknown error has occurred; either the application has provided invalid input, or an" +
						" implementation failure has occurred.";
			case VK13.VK_ERROR_VALIDATION_FAILED:
				errCode = "VK_ERROR_VALIDATION_FAILED";
				errMsg = "A command failed because invalid usage was detected by the implementation or a validation " +
						"layer. This may result in the command not being dispatched to the ICD.";
			case VK13.VK_ERROR_OUT_OF_POOL_MEMORY:
				errCode = "VK_ERROR_OUT_OF_POOL_MEMORY";
				errMsg =
						"A pool memory allocation has failed. This must only be returned if no attempt to allocate host " +
								"or device memory was made to accommodate the new allocation. If the failure was " +
								"definitely due to fragmentation of the pool, VK_ERROR_FRAGMENTED_POOL should be " +
								"returned instead.";
			case VK13.VK_ERROR_INVALID_EXTERNAL_HANDLE:
				errCode = "VK_ERROR_INVALID_EXTERNAL_HANDLE";
				errMsg = "An external handle is not a valid handle of the specified type.";
			case VK13.VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS:
				errCode = "VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS";
				errMsg =
						"A buffer creation or memory allocation failed because the requested address is not available. A" +
								" shader group handle assignment failed because the requested shader group handle" +
								" information is no longer valid.";
			case VK13.VK_ERROR_FRAGMENTATION:
				errCode = "VK_ERROR_FRAGMENTATION";
				errMsg = "A descriptor pool creation has failed due to fragmentation.";
			case VK13.VK_PIPELINE_COMPILE_REQUIRED:
				errCode = "VK_PIPELINE_COMPILE_REQUIRED";
				errMsg =
						"A requested pipeline creation would have required compilation, but the application requested" +
								" compilation to not be performed.";
			case KHRGlobalPriority.VK_ERROR_NOT_PERMITTED_KHR:
				errCode = "VK_ERROR_NOT_PERMITTED";
				errMsg =
						"The driver implementation has denied a request to acquire a priority above the default priority" +
								" (VK_QUEUE_GLOBAL_PRIORITY_MEDIUM_EXT) because the application does not have sufficient" +
								" privileges.";
			case KHRSurface.VK_ERROR_SURFACE_LOST_KHR:
				errCode = "VK_ERROR_SURFACE_LOST_KHR";
				errMsg = "A surface is no longer available.";
			case KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
				errCode = "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR";
				errMsg =
						"The requested window is already in use by Vulkan or another API in a manner which prevents it" +
								" from being used again.";
			case KHRSwapchain.VK_SUBOPTIMAL_KHR:
				errCode = "VK_SUBOPTIMAL_KHR";
				errMsg =
						"A swapchain no longer matches the surface properties exactly, but can still be used to present" +
								" to the surface successfully.";
			case KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR:
				errCode = "VK_ERROR_OUT_OF_DATE_KHR";
				errMsg = "A surface has changed in such a way that it is no longer compatible with the swapchain, and" +
						" further presentation requests using the swapchain will fail. Applications must query" +
						" the new surface properties and recreate their swapchain if they wish to continue" +
						" presenting to the surface.";
			case KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
				errCode = "VK_ERROR_INCOMPATIBLE_DISPLAY_KHR";
				errMsg = "The display used by a swapchain does not use the same presentable image layout, or is" +
						" incompatible in a way that prevents sharing an image.";
			case NVGLSLShader.VK_ERROR_INVALID_SHADER_NV:
				errCode = "VK_ERROR_INVALID_SHADER_NV";
				errMsg =
						"One or more shaders failed to compile or link. More details are reported back to the application" +
								" via VK_EXT_debug_report if enabled.";
			case KHRVideoQueue.VK_ERROR_IMAGE_USAGE_NOT_SUPPORTED_KHR:
				errCode = "VK_ERROR_IMAGE_USAGE_NOT_SUPPORTED_KHR";
				errMsg = "The requested VkImageUsageFlags are not supported.";
			case KHRVideoQueue.VK_ERROR_VIDEO_PICTURE_LAYOUT_NOT_SUPPORTED_KHR:
				errCode = "VK_ERROR_VIDEO_PICTURE_LAYOUT_NOT_SUPPORTED_KHR";
				errMsg = "The requested video picture layout is not supported.";
			case KHRVideoQueue.VK_ERROR_VIDEO_PROFILE_OPERATION_NOT_SUPPORTED_KHR:
				errCode = "VK_ERROR_VIDEO_PROFILE_OPERATION_NOT_SUPPORTED_KHR";
				errMsg = "A video profile operation specified via VkVideoProfileInfoKHR::videoCodecOperation is not" +
						" supported.";
			case KHRVideoQueue.VK_ERROR_VIDEO_PROFILE_FORMAT_NOT_SUPPORTED_KHR:
				errCode = "VK_ERROR_VIDEO_PROFILE_FORMAT_NOT_SUPPORTED_KHR";
				errMsg = "Format parameters in a requested VkVideoProfileInfoKHR chain are not supported.";
			case KHRVideoQueue.VK_ERROR_VIDEO_PROFILE_CODEC_NOT_SUPPORTED_KHR:
				errCode = "VK_ERROR_VIDEO_PROFILE_CODEC_NOT_SUPPORTED_KHR";
				errMsg = "Codec-specific parameters in a requested VkVideoProfileInfoKHR chain are not supported.";
			case KHRVideoQueue.VK_ERROR_VIDEO_STD_VERSION_NOT_SUPPORTED_KHR:
				errCode = "VK_ERROR_VIDEO_STD_VERSION_NOT_SUPPORTED_KHR";
				errMsg = "The specified video Std header version is not supported.";
			case EXTImageDrmFormatModifier.VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT:
				errCode = "VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT";
			case EXTPresentTiming.VK_ERROR_PRESENT_TIMING_QUEUE_FULL_EXT:
				errCode = "VK_ERROR_PRESENT_TIMING_QUEUE_FULL_EXT";
			case EXTFullScreenExclusive.VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT:
				errCode = "VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT";
				errMsg =
						"An operation on a swapchain created with VK_FULL_SCREEN_EXCLUSIVE_APPLICATION_CONTROLLED_EXT" +
								" failed as it did not have exclusive full-screen access. This may occur due to" +
								" implementation-dependent reasons, outside of the application's control.";
			case KHRDeferredHostOperations.VK_THREAD_IDLE_KHR:
				errCode = "VK_THREAD_IDLE_KHR";
				errMsg =
						"A deferred operation is not complete but there is currently no work for this thread to do at" +
								" the time of this call.";
			case KHRDeferredHostOperations.VK_THREAD_DONE_KHR:
				errCode = "VK_THREAD_DONE_KHR";
				errMsg = "A deferred operation is not complete but there is no work remaining to assign to additional" +
						" threads.";
			case KHRDeferredHostOperations.VK_OPERATION_DEFERRED_KHR:
				errCode = "VK_OPERATION_DEFERRED_KHR";
				errMsg = "A deferred operation was requested and at least some of the work was deferred.";
			case KHRDeferredHostOperations.VK_OPERATION_NOT_DEFERRED_KHR:
				errCode = "VK_OPERATION_NOT_DEFERRED_KHR";
				errMsg = "A deferred operation was requested and no operations were deferred.";
			case -1000299000 /*VK_ERROR_INVALID_VIDEO_STD_PARAMETERS_KHR*/:
				errCode = "VK_ERROR_INVALID_VIDEO_STD_PARAMETERS_KHR";
				errMsg =
						"The specified Video Std parameters do not adhere to the syntactic or semantic requirements of" +
								" the used video compression standard, or values derived from parameters according to the" +
								" rules defined by the used video compression standard do not adhere to the capabilities" +
								" of the video compression standard or the implementation.";
			case EXTImageCompressionControl.VK_ERROR_COMPRESSION_EXHAUSTED_EXT:
				errCode = "VK_ERROR_COMPRESSION_EXHAUSTED_EXT";
				errMsg = "An image creation failed because internal resources required for compression are exhausted." +
						" This must only be returned when fixed-rate compression is requested.";
			case EXTShaderObject.VK_INCOMPATIBLE_SHADER_BINARY_EXT:
				errCode = "VK_INCOMPATIBLE_SHADER_BINARY_EXT";
				errMsg = "The provided binary shader code is not compatible with this device.";
			case KHRPipelineBinary.VK_PIPELINE_BINARY_MISSING_KHR:
				errCode = "VK_PIPELINE_BINARY_MISSING_KHR";
				errMsg =
						"The application attempted to create a pipeline binary by querying an internal cache, but the" +
								" internal cache entry did not exist.";
			case KHRPipelineBinary.VK_ERROR_NOT_ENOUGH_SPACE_KHR:
				errCode = "VK_ERROR_NOT_ENOUGH_SPACE_KHR";
				errMsg = "The application did not provide enough space to return all the required data.";
		}
		if (err != VK13.VK_SUCCESS)
		{
			throw new AssertionError(errMsg + ": " + errCode + "[" + err + "]: " + errDesc);
		}
	}

	/**
	 * The host operating systems distinguished by {@link #getOS()}.
	 */
	public enum OSType
	{
		/**
		 * Microsoft Windows.
		 */
		WINDOWS,
		/**
		 * Linux.
		 */
		LINUX,
		/**
		 * An operating system that is none of the others, such as FreeBSD.
		 */
		OTHER,
		/**
		 * Apple macOS, where Vulkan runs over MoltenVK and the portability extensions apply.
		 */
		MACOS
	}
}

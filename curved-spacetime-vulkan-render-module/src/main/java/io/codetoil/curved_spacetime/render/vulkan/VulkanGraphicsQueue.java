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

package io.codetoil.curved_spacetime.render.vulkan;

<<<<<<<<HEAD:curved-spacetime-vulkan-render-module/src/main/java/io/codetoil/curved_spacetime/render/vulkan/VulkanRenderModuleGraphicsModuleQueue.java
		import io.codetoil.curved_spacetime.api.vulkan.VulkanModulePhysicalDevice;
		import io.codetoil.curved_spacetime.api.vulkan.VulkanModuleQueue;
		========
		import io.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
		import io.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
		import io.codetoil.curved_spacetime.vulkan.VulkanQueue;
		>>>>>>>>master:curved-spacetime-vulkan-render-module/src/main/java/io/codetoil/curved_spacetime/render/vulkan/VulkanGraphicsQueue.java
		import org.lwjgl.system.MemoryStack;
		import org.lwjgl.vulkan.KHRSurface;
		import org.lwjgl.vulkan.VK13;
		import org.lwjgl.vulkan.VkQueueFamilyProperties;

		import java.nio.IntBuffer;

		public class VulkanRenderModuleGraphicsModuleQueue extends VulkanModuleQueue
		{
		public VulkanRenderModuleGraphicsModuleQueue(VulkanRenderModuleRenderContext context,int queueFamilyIndex,
		int queueIndex)
		{
		super(context.getContext(),queueFamilyIndex,queueIndex);
		}

		public VulkanRenderModuleGraphicsModuleQueue(VulkanRenderModuleRenderContext context,int queueIndex)
		{
		super(context.getContext(),getGraphicsQueueFamilyIndex(context),queueIndex);
		}

		private static int getGraphicsQueueFamilyIndex(VulkanRenderModuleRenderContext context)
		{
		int result=-1;
		VulkanModulePhysicalDevice vulkanModulePhysicalDevice=
		context.getContext().getLogicalDevice().getPhysicalDevice();
		VkQueueFamilyProperties.Buffer queuePropsBuff=vulkanModulePhysicalDevice.getVkQueueFamilyProps();
		int numQueuesFamilies=queuePropsBuff.capacity();
		for(int index=0;index<numQueuesFamilies;index++)
		{
		VkQueueFamilyProperties props=queuePropsBuff.get(index);
		boolean graphicsQueue=(props.queueFlags()&VK13.VK_QUEUE_GRAPHICS_BIT)!=0;
		if(graphicsQueue)
		{
		result=index;
		break;
		}
		}

		if(result<0)
		{
		throw new RuntimeException("Failed to get graphics Queue family index.");
		}
		return result;
		}

		public static class VulkanRenderModuleGraphicsPresentModuleQueue extends VulkanRenderModuleGraphicsModuleQueue
		{
		public VulkanRenderModuleGraphicsPresentModuleQueue(VulkanRenderModuleSceneRenderContext context,
		int queueIndex)
		{
		super(context.getRenderContext(),getPresentQueueFamilyIndex(context),queueIndex);
		}

		private static int getPresentQueueFamilyIndex(VulkanRenderModuleSceneRenderContext context)
		{
		int index=-1;
		try(MemoryStack stack=MemoryStack.stackPush())
		{
		VulkanModulePhysicalDevice physicalDevice=context.getRenderContext().getContext().getLogicalDevice()
		.getPhysicalDevice();
		VkQueueFamilyProperties.Buffer queuePropsBuff=physicalDevice.getVkQueueFamilyProps();
		int numQueuesFamilies=queuePropsBuff.capacity();
		IntBuffer intBuffer=stack.mallocInt(1);
		for(int i=0;i<numQueuesFamilies;i++)
		{
		KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice.getVkPhysicalDevice(),i,
		context.getSurface().getVkSurface(),intBuffer);
		boolean supportsPresentation=intBuffer.get(0)==VK13.VK_TRUE;
		if(supportsPresentation)
		{
		index=i;
		break;
		}
		}

		if(index<0)
		{
		throw new RuntimeException("Failed to get Presentation Queue family index.");
		}
		return index;
		}
		}
		}
		}

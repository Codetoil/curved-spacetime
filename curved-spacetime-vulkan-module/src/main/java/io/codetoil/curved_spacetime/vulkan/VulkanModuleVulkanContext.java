package io.codetoil.curved_spacetime.vulkan;

import org.lwjgl.PointerBuffer;

import java.util.function.Supplier;

public class VulkanModuleVulkanContext
{
	protected VulkanModuleVulkanInstance vulkanModuleVulkanInstance = null;
	protected VulkanModulePhysicalDevice vulkanModulePhysicalDevice;
	protected VulkanModuleLogicalDevice vulkanModuleLogicalDevice;

	public void init(VulkanModuleEntrypoint entrypoint,
					 Supplier<PointerBuffer> windowExtensionGetter)
	{
		this.vulkanModuleVulkanInstance = new VulkanModuleVulkanInstance(entrypoint, windowExtensionGetter);
		this.vulkanModulePhysicalDevice =
				VulkanModulePhysicalDevice.createPhysicalDevice(this.vulkanModuleVulkanInstance, entrypoint);
		this.vulkanModuleLogicalDevice = new VulkanModuleLogicalDevice(this.vulkanModulePhysicalDevice);
	}

	public void loop()
	{

	}

	public void cleanup()
	{
		this.vulkanModuleLogicalDevice.waitIdle();
		this.vulkanModuleLogicalDevice.cleanup();
		this.vulkanModulePhysicalDevice.cleanup();
		this.vulkanModuleVulkanInstance.cleanup();
	}

	public VulkanModuleVulkanInstance getVulkanInstance()
	{
		return vulkanModuleVulkanInstance;
	}

	public VulkanModulePhysicalDevice getVulkanPhysicalDevice()
	{
		return vulkanModulePhysicalDevice;
	}

	public VulkanModuleLogicalDevice getLogicalDevice()
	{
		return vulkanModuleLogicalDevice;
	}
}

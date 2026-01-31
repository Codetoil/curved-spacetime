package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.MainCallback;

public class VulkanModuleVulkan extends MainCallback
{
	private final VulkanModuleEntrypoint entrypoint;
	protected VulkanModuleVulkanInstance vulkanModuleVulkanInstance = null;
	protected VulkanModulePhysicalDevice vulkanModulePhysicalDevice;
	protected VulkanModuleLogicalDevice vulkanModuleLogicalDevice;

	public VulkanModuleVulkan(MainModuleEngine mainModuleEngine, VulkanModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		this.vulkanModuleVulkanInstance = new VulkanModuleVulkanInstance(this.entrypoint,
				this.entrypoint.getLogger());
		this.vulkanModulePhysicalDevice =
				VulkanModulePhysicalDevice.createPhysicalDevice(this.vulkanModuleVulkanInstance,
						entrypoint, this.entrypoint.getLogger());
		this.vulkanModuleLogicalDevice = new VulkanModuleLogicalDevice(this.vulkanModulePhysicalDevice, this.entrypoint.getLogger());
	}

	@Override
	public void loop()
	{

	}

	@Override
	public void clean()
	{
		this.vulkanModuleLogicalDevice.waitIdle();
		this.vulkanModuleLogicalDevice.cleanup();
		this.vulkanModulePhysicalDevice.cleanup();
		this.vulkanModuleVulkanInstance.cleanup();
	}

	public VulkanModuleVulkanInstance getVulkanModuleVulkanInstance()
	{
		return vulkanModuleVulkanInstance;
	}

	public VulkanModulePhysicalDevice getVulkanModulePhysicalDevice()
	{
		return vulkanModulePhysicalDevice;
	}

	public VulkanModuleLogicalDevice getVulkanModuleLogicalDevice()
	{
		return vulkanModuleLogicalDevice;
	}
}

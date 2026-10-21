package io.codetoil.curved_spacetime.vulkan_ffm;

import io.codetoil.curved_spacetime.MainCallback;
import io.codetoil.curved_spacetime.MainModuleEngine;

public class VulkanFFMModuleVulkan extends MainCallback
{
	private final CurvedSpacetimeVulkanFFMModuleEntrypoint entrypoint;
	protected VulkanFFMModuleVulkanInstance vulkanFFMModuleVulkanInstance = null;
	protected VulkanFFMModulePhysicalDevice vulkanFFMModulePhysicalDevice;
	protected VulkanFFMModuleLogicalDevice vulkanFFMModuleLogicalDevice;

	public VulkanFFMModuleVulkan(MainModuleEngine mainModuleEngine, CurvedSpacetimeVulkanFFMModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		this.vulkanFFMModuleVulkanInstance = new VulkanFFMModuleVulkanInstance(this.entrypoint,
				this.entrypoint.getLogger());
		this.vulkanFFMModulePhysicalDevice =
				VulkanFFMModulePhysicalDevice.createPhysicalDevice(this.vulkanFFMModuleVulkanInstance,
						entrypoint, this.entrypoint.getLogger());
		this.vulkanFFMModuleLogicalDevice =
				new VulkanFFMModuleLogicalDevice(this.vulkanFFMModulePhysicalDevice, this.entrypoint.getLogger());
	}

	@Override
	public void loop()
	{

	}

	@Override
	public void clean()
	{
		this.vulkanFFMModuleLogicalDevice.waitIdle();
		this.vulkanFFMModuleLogicalDevice.cleanup();
		this.vulkanFFMModulePhysicalDevice.cleanup();
		this.vulkanFFMModuleVulkanInstance.cleanup();
	}

	public VulkanFFMModuleVulkanInstance getVulkanModuleVulkanInstance()
	{
		return vulkanFFMModuleVulkanInstance;
	}

	public VulkanFFMModulePhysicalDevice getVulkanModulePhysicalDevice()
	{
		return vulkanFFMModulePhysicalDevice;
	}

	public VulkanFFMModuleLogicalDevice getVulkanModuleLogicalDevice()
	{
		return vulkanFFMModuleLogicalDevice;
	}
}

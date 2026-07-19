package io.codetoil.curved_spacetime.vulkan.ffm;

import io.codetoil.curved_spacetime.MainCallback;
import io.codetoil.curved_spacetime.MainModuleEngine;

public class FFMVulkanModuleVulkan extends MainCallback
{
	private final FFMVulkanModuleEntrypoint entrypoint;
	protected FFMVulkanModuleVulkanInstance ffmVulkanModuleVulkanInstance = null;
	protected FFMVulkanModulePhysicalDevice ffmVulkanModulePhysicalDevice;
	protected FFMVulkanModuleLogicalDevice ffmVulkanModuleLogicalDevice;

	public FFMVulkanModuleVulkan(MainModuleEngine mainModuleEngine, FFMVulkanModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine);
		this.entrypoint = entrypoint;
	}

	public void init()
	{
		this.ffmVulkanModuleVulkanInstance = new FFMVulkanModuleVulkanInstance(this.entrypoint,
				this.entrypoint.getLogger());
		this.ffmVulkanModulePhysicalDevice =
				FFMVulkanModulePhysicalDevice.createPhysicalDevice(this.ffmVulkanModuleVulkanInstance,
						entrypoint, this.entrypoint.getLogger());
		this.ffmVulkanModuleLogicalDevice =
				new FFMVulkanModuleLogicalDevice(this.ffmVulkanModulePhysicalDevice, this.entrypoint.getLogger());
	}

	@Override
	public void loop()
	{

	}

	@Override
	public void clean()
	{
		this.ffmVulkanModuleLogicalDevice.waitIdle();
		this.ffmVulkanModuleLogicalDevice.cleanup();
		this.ffmVulkanModulePhysicalDevice.cleanup();
		this.ffmVulkanModuleVulkanInstance.cleanup();
	}

	public FFMVulkanModuleVulkanInstance getVulkanModuleVulkanInstance()
	{
		return ffmVulkanModuleVulkanInstance;
	}

	public FFMVulkanModulePhysicalDevice getVulkanModulePhysicalDevice()
	{
		return ffmVulkanModulePhysicalDevice;
	}

	public FFMVulkanModuleLogicalDevice getVulkanModuleLogicalDevice()
	{
		return ffmVulkanModuleLogicalDevice;
	}
}

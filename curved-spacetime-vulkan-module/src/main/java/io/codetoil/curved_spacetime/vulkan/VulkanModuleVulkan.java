/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2026
 * Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.vulkan;

import io.codetoil.curved_spacetime.MainCallback;
import io.codetoil.curved_spacetime.MainModuleEngine;

/**
 * Owns the Vulkan objects that outlive any one window: the instance, the physical device, and the
 * logical device.
 * <p>
 * Registered as a {@link MainCallback} so that its lifetime is tied to the engine rather than to a
 * scene. Everything scene- or surface-specific — swap chains, render passes, framebuffers — is
 * built on top of these by the render modules, which reach them through the getters here.
 * <p>
 * Creation order matters and is not negotiable: the instance must exist before a physical device
 * can be selected, and a physical device before a logical one can be created. {@link #clean()}
 * unwinds in the reverse order, after waiting for the device to go idle.
 */
public class VulkanModuleVulkan extends MainCallback
{
	private final VulkanModuleEntrypoint entrypoint;

	/**
	 * The Vulkan instance, created first during {@link #init()}.
	 */
	protected VulkanModuleVulkanInstance vulkanModuleVulkanInstance = null;

	/**
	 * The selected physical device.
	 */
	protected VulkanModulePhysicalDevice vulkanModulePhysicalDevice;

	/**
	 * The logical device created against the selected physical device.
	 */
	protected VulkanModuleLogicalDevice vulkanModuleLogicalDevice;

	/**
	 * Creates the Vulkan callback.
	 * <p>
	 * No Vulkan object is created until {@link #init()} runs on the engine's callback thread.
	 *
	 * @param mainModuleEngine the engine this callback belongs to
	 * @param entrypoint       the Vulkan module's entrypoint, supplying configuration and logger
	 */
	public VulkanModuleVulkan(MainModuleEngine mainModuleEngine, VulkanModuleEntrypoint entrypoint)
	{
		super(mainModuleEngine);
		this.entrypoint = entrypoint;
	}

	/**
	 * Creates the instance, selects a physical device, and creates the logical device.
	 *
	 * @throws RuntimeException if no suitable physical device is available, or if any Vulkan call
	 *                          fails
	 */
	public void init()
	{
		this.vulkanModuleVulkanInstance = new VulkanModuleVulkanInstance(this.entrypoint,
				this.entrypoint.getLogger());
		this.vulkanModulePhysicalDevice =
				VulkanModulePhysicalDevice.createPhysicalDevice(this.vulkanModuleVulkanInstance,
						entrypoint, this.entrypoint.getLogger());
		this.vulkanModuleLogicalDevice =
				new VulkanModuleLogicalDevice(this.vulkanModulePhysicalDevice, this.entrypoint.getLogger());
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

	/**
	 * Returns the Vulkan instance.
	 *
	 * @return the instance, or {@code null} before {@link #init()}
	 */
	public VulkanModuleVulkanInstance getVulkanModuleVulkanInstance()
	{
		return vulkanModuleVulkanInstance;
	}

	/**
	 * Returns the selected physical device.
	 *
	 * @return the physical device, or {@code null} before {@link #init()}
	 */
	public VulkanModulePhysicalDevice getVulkanModulePhysicalDevice()
	{
		return vulkanModulePhysicalDevice;
	}

	/**
	 * Returns the logical device.
	 *
	 * @return the logical device, or {@code null} before {@link #init()}
	 */
	public VulkanModuleLogicalDevice getVulkanModuleLogicalDevice()
	{
		return vulkanModuleLogicalDevice;
	}
}

/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br>
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

/**
 * Vulkan Module of Curved Spacetime
 * <p>
 * Wraps the Vulkan objects that outlive any one window — the instance, physical device, logical
 * device, queues, command pools, and synchronisation primitives — leaving swap chains and render
 * passes to the render modules built on top.
 * <p>
 * Much of this follows Antonio Hernández Bejarano's
 * <a href="https://github.com/lwjglgamedev/vulkanbook">Vulkan book</a>, which is MIT licensed.
 * Several revisions of it contributed, and the class names here mirror the earlier ones, so the
 * book's current {@code PhysDevice} and {@code VkUtils} appear as
 * {@code VulkanModulePhysicalDevice} and {@code VulkanUtils}. {@code Notices.md} records exactly
 * which revisions were used.
 */
module io.codetoil.curved_spacetime.vulkan {
	requires io.codetoil.curved_spacetime;
	requires io.codetoil.curved_spacetime.loader;
	requires java.logging;
	requires org.lwjgl;
	requires org.lwjgl.vulkan;

	exports io.codetoil.curved_spacetime.vulkan;
	exports io.codetoil.curved_spacetime.vulkan.utils;
	exports io.codetoil.curved_spacetime.vulkan.entrypoint;
}
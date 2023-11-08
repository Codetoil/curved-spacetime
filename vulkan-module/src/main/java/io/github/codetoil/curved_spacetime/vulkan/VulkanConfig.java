/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023  Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2023 Antonio Hernández Bejarano<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br>
 * it under the terms of the GNU General Public License as published by <br>
 * the Free Software Foundation, either version 3 of the License, or <br>
 * (at your option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
 * GNU General Public License for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br>
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<br>
 */

package io.github.codetoil.curved_spacetime.vulkan;

import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class VulkanConfig {
    private static final String FILENAME = "vulkan-module.config";
    private boolean dirty = false;

    public VulkanConfig() {

    }

    public VulkanConfig load() throws IOException {
        Properties props = new Properties();

        try (FileReader reader = new FileReader(FILENAME)) {
            props.load(reader);
        } catch (FileNotFoundException ex) {
            Logger.warn("Could not find config file " + FILENAME, ex);
            this.dirty = true;
        }

        return this;
    }

    public void save() throws IOException {
        Properties props = new Properties();

        try (FileWriter writer = new FileWriter(FILENAME)) {
            props.store(writer, "Config for the Vulkan Module.");
        }
        this.dirty = false;
    }

    public boolean isDirty() {
        return this.dirty;
    }
}

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

package io.github.codetoil.curved_spacetime.render.vulkan;

import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class VulkanRenderConfig {
    private static final int DEFAULT_FPS = 60;
    private static final boolean DEFAULT_VSYNC = true;
    private static final int DEFAULT_REQUESTED_IMAGES = 2;
    private static final String FILENAME = "vulkan-render-module.config";
    private int fps;
    private boolean vsync;
    private int requestedImages;
    private boolean dirty = false;

    public VulkanRenderConfig() {}

    public int getFPS() {
        return this.fps;
    }

    public boolean hasVSync()
    {
        return this.vsync;
    }

    public int getRequestedImages() {
        return this.requestedImages;
    }

    public VulkanRenderConfig load() throws IOException {
        Properties props = new Properties();

        try (FileReader reader = new FileReader(FILENAME)) {
            props.load(reader);
        } catch (FileNotFoundException ex) {
            Logger.warn(ex, "Could not find config file " + FILENAME);
            this.dirty = true;
        }

        Object fpsPropValue = props.get("fps");
        if (fpsPropValue != null)
        {
            try {
                this.fps = Integer.parseInt(fpsPropValue.toString());
            } catch (NumberFormatException ex)
            {
                Logger.warn(ex, "Invalid value for key fps: {}, valid bounds [1,1000], resetting to default {}",
                        fpsPropValue, DEFAULT_FPS);
                this.fps = DEFAULT_FPS;
                this.dirty = true;
            }
            if (this.fps < 1 || this.fps > 1000)
            {
                Logger.warn("Invalid value for key fps: {}, valid bounds [1,1000], resetting to default {}",
                        this.fps, DEFAULT_FPS);
                this.fps = DEFAULT_FPS;
                this.dirty = true;
            }
        } else {
            Logger.warn("Could not find required key fps, valid bounds [1,1000], resetting to default {}",
                    DEFAULT_FPS);
            this.fps = DEFAULT_FPS;
            this.dirty = true;
        }

        Object vsyncPropValue = props.get("vsync");
        if (vsyncPropValue != null)
        {
            this.vsync = Boolean.parseBoolean(vsyncPropValue.toString());
        } else {
            Logger.warn("Could not find required key vsync, resetting to default {}",
                    DEFAULT_VSYNC);
            this.vsync = DEFAULT_VSYNC;
            this.dirty = true;
        }

        Object requestedImagesPropValue = props.get("requestedImages");
        if (requestedImagesPropValue != null)
        {
            try {
                this.requestedImages = Integer.parseInt(requestedImagesPropValue.toString());
            } catch (NumberFormatException ex)
            {
                Logger.warn(ex, "Invalid value for key requestedImages: {}, " +
                                "lower bound 2, resetting to default {}",
                        requestedImagesPropValue, DEFAULT_REQUESTED_IMAGES);
                this.requestedImages = DEFAULT_REQUESTED_IMAGES;
                this.dirty = true;
            }
            if (this.requestedImages < 2)
            {
                Logger.warn("Invalid value for key requestedImages: {}, " +
                                "lower bound 2, resetting to default {}",
                        this.requestedImages, DEFAULT_REQUESTED_IMAGES);
                this.requestedImages = DEFAULT_REQUESTED_IMAGES;
                this.dirty = true;
            }
        } else {
            Logger.warn("Could not find required key requestedImages, " +
                            "lower bound 2, resetting to default {}",
                    DEFAULT_REQUESTED_IMAGES);
            this.requestedImages = DEFAULT_REQUESTED_IMAGES;
            this.dirty = true;
        }

        return this;
    }

    public void save() throws IOException {
        Properties props = new Properties();
        props.put("fps", String.valueOf(this.fps));
        props.put("vsync", String.valueOf(this.vsync));
        props.put("requestedImages", String.valueOf(this.requestedImages));

        try (FileWriter writer = new FileWriter(FILENAME)) {
            props.store(writer, "Config for the Vulkan Render Module.");
        }
        this.dirty = false;
    }

    public boolean isDirty() {
        return this.dirty;
    }
}

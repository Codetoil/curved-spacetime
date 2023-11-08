/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023  Anthony Michalek (Codetoil)<br>
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

package io.github.codetoil.curved_spacetime.api.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RendererConfig {
    private static final int DEFAULT_FPS = 60;
    private static final String FILENAME = "renderer.properties";
    private int fps;

    public RendererConfig() {

    }

    public int getFPS() {
        return this.fps;
    }

    public RendererConfig load() throws IOException {
        Properties props = new Properties();

        try (InputStream stream = RendererConfig.class.getResourceAsStream("/" + FILENAME)) {
            if (stream != null)
                props.load(stream);
            this.fps = Integer.parseInt(props.getOrDefault("fps", DEFAULT_FPS).toString());
        }

        return this;
    }
}

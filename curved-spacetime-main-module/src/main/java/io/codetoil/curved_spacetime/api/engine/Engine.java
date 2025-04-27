/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
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

package io.codetoil.curved_spacetime.api.engine;

import io.codetoil.curved_spacetime.api.APIConfig;
import io.codetoil.curved_spacetime.api.render.Renderer;
import io.codetoil.curved_spacetime.api.scene.Scene;

import java.io.IOException;

public class Engine {
    public final APIConfig APIConfig;
    public Renderer renderer;
    public Scene scene;

    public Engine() {
        try {
            this.APIConfig = new APIConfig().load();
            if (this.APIConfig.isDirty()) this.APIConfig.save();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load API Config", ex);
        }
        this.scene = new Scene();
    }

    public void clean() {
        this.renderer.getWindow().hideWindow();
        this.renderer.clean();
    }

    public void stop() {
        this.clean();
    }
}

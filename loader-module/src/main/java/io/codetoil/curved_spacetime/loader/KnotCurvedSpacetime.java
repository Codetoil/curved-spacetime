/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
 * Copyright 2016 FabricMC<br>
 * Copyright 2022-2023 QuiltMC<br>
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

package io.codetoil.curved_spacetime.loader;

import net.fabricmc.api.EnvType;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.util.SystemProperties;

public class KnotCurvedSpacetime {
    public static final EnvType CURVED_SPACETIME = EnvType.valueOf("CURVED_SPACETIME");

    public static void main(String[] args) {
        System.setProperty(SystemProperties.SKIP_MC_PROVIDER, "true");
        System.setProperty(SystemProperties.MODS_DIRECTORY, "modules");
        Knot.launch(args, CURVED_SPACETIME);
    }
}

/*
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023  Anthony Michalek (Codetoil)<br>
 * Copyright 2022, 2023 QuiltMC<br>
 * <br>
 * The following file is part of Curved Spacetime<br>
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

package net.fabricmc.api;

/**
 * Represents a type of environment.
 *
 * <p>A type of environment is a jar file in a <i>Minecraft</i> version's json file's {@code download}
 * subsection, including the {@code client.jar} and the {@code server.jar}.</p>
 *
 * @see Environment
 * @see EnvironmentInterface
 */
public enum EnvType {
     /**
      * Represents the client environment type, in which the {@code client.jar} for a
      * <i>Minecraft</i> version is the main game jar.
      *
      * <p>A client environment type has all client logic (client rendering and integrated
      * server logic), the data generator logic, and dedicated server logic. It encompasses
      * everything that is available on the {@linkplain #SERVER server environment type}.</p>
      */
     CLIENT,
     /**
      * Represents the server environment type, in which the {@code server.jar} for a
      * <i>Minecraft</i> version is the main game jar.
      *
      * <p>A server environment type has the dedicated server logic and data generator
      * logic, which are all included in the {@linkplain #CLIENT client environment type}.
      * However, the server environment type has its libraries embedded compared to the
      * client.</p>
      */
     SERVER,
     CURVED_SPACETIME;
}
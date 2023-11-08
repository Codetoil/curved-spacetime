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

package io.github.codetoil.curved_spacetime.loader;

import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.util.Arguments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CurvedSpacetimeGameProvider implements GameProvider {
    private final GameTransformer transformer = new GameTransformer();

    private final List<Path> gameJars = new ArrayList<>(2);
    private Arguments arguments;

    @Override
    public String getGameId() {
        return "curved_spacetime";
    }

    @Override
    public String getGameName() {
        return "Curved Spacetime";
    }

    @Override
    public String getRawGameVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public String getNormalizedGameVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        return Collections.emptyList();
    }

    @Override
    public String getEntrypoint() {
        return null;
    }

    @Override
    public Path getLaunchDirectory() {
        return Paths.get(".");
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(QuiltLauncher launcher, String[] args) {
        this.arguments = new Arguments();
        arguments.parse(args);

        return true;
    }

    @Override
    public boolean isGameClass(String name) {
        return name.startsWith("io.github.codetoil.curved_spacetime.");
    }

    @Override
    public void initialize(QuiltLauncher launcher) {
        transformer.locateEntrypoints(launcher, gameJars);
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return null;
    }

    @Override
    public void unlockClassPath(QuiltLauncher launcher) {

    }

    @Override
    public void launch(ClassLoader loader) {

    }

    @Override
    public Arguments getArguments() {
        return this.arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }
}

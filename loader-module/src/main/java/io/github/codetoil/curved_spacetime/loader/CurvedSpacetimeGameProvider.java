/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023  Anthony Michalek (Codetoil)<br>
 * Copyright 2016 FabricMC<br>
 * Copyright QuiltMC<br>
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

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.game.GameProviderHelper;
import org.quiltmc.loader.impl.game.LibClassifier;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.metadata.qmj.V1ModMetadataBuilder;
import org.quiltmc.loader.impl.util.Arguments;
import org.quiltmc.loader.impl.util.ExceptionUtil;
import org.quiltmc.loader.impl.util.LoaderUtil;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CurvedSpacetimeGameProvider implements GameProvider {
    private static final String ENTRYPOINT = "io.github.codetoil.curved_spacetime.Main";
    private Arguments arguments;
    private final List<Path> gameJars = new ArrayList<>();
    private final List<Path> miscGameLibraries = new ArrayList<>();

    private final GameTransformer transformer = new GameTransformer();

    @Override
    public String getGameId() {
        return "curved_spacetime_main_module";
    }

    @Override
    public String getGameName() {
        return "Curved Spacetime Main Module";
    }

    @Override
    public String getRawGameVersion() {
        return "0.1.0-SNAPSHOT";
    }

    @Override
    public String getNormalizedGameVersion() {
        return "0.1.0-SNAPSHOT";
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        V1ModMetadataBuilder metadata = new V1ModMetadataBuilder();
        metadata.id = getGameId();
        metadata.group = "builtin";
        metadata.version = Version.of(getNormalizedGameVersion());
        metadata.name = getGameName();
        metadata.description = "Base Module for Curved Spacetime";
        List<Path> paths = new ArrayList<>(gameJars);
        paths.addAll(miscGameLibraries);
        return List.of(new BuiltinMod(paths, metadata.build()));
    }

    @Override
    public String getEntrypoint() {
        return ENTRYPOINT;
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
        return !QuiltLoader.isDevelopmentEnvironment();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(QuiltLauncher launcher, String[] args) {
        this.arguments = new Arguments();
        arguments.parse(args);

        try {
            LibClassifier<CurvedSpacetimeModule> classifier = new LibClassifier<>(CurvedSpacetimeModule.class,
                    KnotCurvedSpacetime.CURVED_SPACETIME, this);
            Path gameJar = GameProviderHelper.getCommonGameJar();
            if (gameJar != null)
            {
                classifier.process(gameJar);
            }

            Set<Path> classpath = new LinkedHashSet<>();

            for (Path path : launcher.getClassPath()) {
                path = LoaderUtil.normalizeExistingPath(path);
                classpath.add(path);
                classifier.process(path);
            }

            gameJar = classifier.getOrigin(CurvedSpacetimeModule.MAIN_MODULE);

            if (gameJar == null) throw new RuntimeException("Game could not be found!");

            gameJars.add(gameJar);

            for (Path path : classifier.getUnmatchedOrigins()) {
                if (!classpath.contains(path)) {
                    miscGameLibraries.add(path);
                }
            }
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
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
        return this.transformer;
    }

    @Override
    public void unlockClassPath(QuiltLauncher launcher) {
        for (Path gameJar : gameJars) {
            launcher.addToClassPath(gameJar);
        }

        for (Path lib : miscGameLibraries) {
            launcher.addToClassPath(lib);
        }
    }

    @Override
    public boolean hasAwtSupport() {
        return !LoaderUtil.hasMacOs();
    }

    @Override
    public void launch(ClassLoader loader) {
        Log.debug(LogCategory.GAME_PROVIDER, "Launching Game");

        try {
            Class<?> c = loader.loadClass("io.github.codetoil.curved_spacetime.Main");
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, (Object) arguments.toArray());
        } catch (InvocationTargetException e) {
            throw new FormattedException("Curved Spacetime has crashed!", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new FormattedException("Failed to start Curved Spacetime", e);
        }
    }

    @Override
    public Arguments getArguments() {
        return this.arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return arguments == null ? new String[0] : arguments.toArray();
    }
}

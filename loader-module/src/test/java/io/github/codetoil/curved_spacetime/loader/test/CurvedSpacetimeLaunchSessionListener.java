/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2024 Anthony Michalek (Codetoil)<br>
 * Copyright FabricMC, QuiltMC<br>
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

package io.github.codetoil.curved_spacetime.loader.test;

import net.fabricmc.api.EnvType;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.util.SystemProperties;

public class CurvedSpacetimeLaunchSessionListener implements LauncherSessionListener {
    static {
        System.setProperty(SystemProperties.SKIP_MC_PROVIDER, "true");
        System.setProperty(SystemProperties.MODS_DIRECTORY, "modules");
        System.setProperty(SystemProperties.DEVELOPMENT, "true");
        System.setProperty(SystemProperties.UNIT_TEST, "true");
    }

    private final ClassLoader classLoader;

    private ClassLoader launcherSessionClassLoader;

    public CurvedSpacetimeLaunchSessionListener() {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        try {
            Knot knot = new Knot(EnvType.valueOf("CURVED_SPACETIME"));
            classLoader = knot.init(new String[]{});
        } finally {
            // Knot.init sets the context class loader, revert it back for now.
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        final Thread currentThread = Thread.currentThread();
        launcherSessionClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        final Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(launcherSessionClassLoader);
    }
}

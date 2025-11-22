/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C)
 * 2023-2025 Anthony Michalek (Codetoil)<br> Copyright 2016 FabricMC<br> Copyright 2022-2023 QuiltMC<br>
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

package io.codetoil.curved_spacetime.loader.quiltmc;

import io.codetoil.curved_spacetime.loader.quiltmc.log.TinyLogHandler;
import net.fabricmc.api.EnvType;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.util.SystemProperties;
import org.quiltmc.loader.impl.util.log.Log;

public class KnotCurvedSpacetime
{
	public static final EnvType CURVED_SPACETIME = EnvType.valueOf("CURVED_SPACETIME");
	public static final TinyLogHandler LOG_HANDLER = new TinyLogHandler();

	static void main(String[] args)
	{
		Log.init(LOG_HANDLER, true);
		if (!System.getProperties().containsValue(SystemProperties.SKIP_MC_PROVIDER))
			System.setProperty(SystemProperties.SKIP_MC_PROVIDER, "true");
		if (!System.getProperties().containsValue(SystemProperties.MODS_DIRECTORY))
			System.setProperty(SystemProperties.MODS_DIRECTORY, "modules");
		if (!System.getProperties().containsValue(SystemProperties.TARGET_NAMESPACE))
			System.setProperty(SystemProperties.TARGET_NAMESPACE, "official");
		Knot.launch(args, KnotCurvedSpacetime.CURVED_SPACETIME);
	}
}

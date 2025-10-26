/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright 2025 QuiltMC<br>
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

package io.codetoil.curved_spacetime.loader;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.impl.game.MappingConfiguration;
import org.quiltmc.loader.impl.lib.mappingio.tree.MappingTreeView;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class MappingConfigurationCurvedSpacetime implements MappingConfiguration
{
	@Override
	public @Nullable MappingTreeView getMappings()
	{
		return null;
	}

	@Override
	public List<String> getNamespaces()
	{
		return List.of();
	}

	@Override
	public String getTargetNamespace()
	{
		return "curved-spacetime";
	}
}

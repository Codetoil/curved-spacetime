/*
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2023-2025 Anthony Michalek (Codetoil)<br>
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

package org.quiltmc.loader.impl.plugin.quilt;

import java.io.IOException;
import java.nio.file.Path;

import org.quiltmc.loader.api.gui.QuiltLoaderIcon;
import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.api.plugin.QuiltPluginContext;
import org.quiltmc.loader.impl.gui.GuiManagerImpl;
import org.quiltmc.loader.impl.metadata.qmj.InternalModMetadata;
import org.quiltmc.loader.impl.plugin.base.InternalModOptionBase;
import org.quiltmc.loader.impl.util.QuiltLoaderInternal;
import org.quiltmc.loader.impl.util.QuiltLoaderInternalType;

@QuiltLoaderInternal(QuiltLoaderInternalType.NEW_INTERNAL)
public class BuiltinModOption extends InternalModOptionBase {

	public BuiltinModOption(QuiltPluginContext pluginContext, InternalModMetadata meta, Path from, Path resourceRoot) {
		super(pluginContext, meta, from, GuiManagerImpl.ICON_JAVA_PACKAGE, resourceRoot, true,
				false);
	}

	@Override
	public boolean needsTransforming() {
		return false;
	}

	@Override
	public QuiltLoaderIcon modTypeIcon() {
		return GuiManagerImpl.ICON_JAVA_PACKAGE;
	}

	@Override
	public QuiltLoaderIcon modCompleteIcon() {
		return GuiManagerImpl.ICON_JAVA_PACKAGE;
	}

	@Override
	protected String nameOfType() {
		return "builtin";
	}

	@Override
	public ModContainerExt convertToMod(Path transformedResourceRoot) {
		if (!transformedResourceRoot.equals(resourceRoot)) {
			try {
				resourceRoot.getFileSystem().close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return new BuiltinModContainer(pluginContext, metadata, from, transformedResourceRoot, needsTransforming());
	}
}

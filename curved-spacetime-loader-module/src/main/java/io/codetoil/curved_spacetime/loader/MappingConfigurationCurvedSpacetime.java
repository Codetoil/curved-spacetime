package io.codetoil.curved_spacetime.loader;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.impl.game.MappingConfiguration;
import org.quiltmc.loader.impl.lib.mappingio.tree.MappingTreeView;

import java.util.List;

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

/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2023-2025 Anthony
 * Michalek (Codetoil)<br> Copyright 2016 FabricMC<br>
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
import org.quiltmc.loader.api.*;
import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.*;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.metadata.qmj.Icons;
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

public class CurvedSpacetimeGameProvider implements GameProvider
{
	private static final String ENTRYPOINT = "io.codetoil.curved_spacetime.Main";
	private Arguments arguments;
	private final List<Path> gameJars = new ArrayList<>();
	private Map<String, List<Path>> gameJarsByNamespace = new HashMap<>();
	private final List<Path> miscGameLibraries = new ArrayList<>();
	private final GameTransformer transformer = new GameTransformer();
	private final MappingConfiguration mappingConfiguration = new MappingConfigurationCurvedSpacetime();

	@Override
	public String getGameId()
	{
		return "curved-spacetime-main-module";
	}

	@Override
	public String getGameName()
	{
		return "Curved Spacetime Main Module";
	}

	@Override
	public String getRawGameVersion()
	{
		return "0.1.0-SNAPSHOT";
	}

	@Override
	public String getNormalizedGameVersion()
	{
		return "0.1.0-SNAPSHOT";
	}

	@Override
	public Collection<BuiltinMod> getBuiltinMods()
	{
		V1ModMetadataBuilder metadata = new V1ModMetadataBuilder();
		metadata.id = getGameId();
		metadata.group = "builtin";
		metadata.version = Version.of(getNormalizedGameVersion());
		metadata.name = getGameName();
		metadata.description = "Base Module for Curved Spacetime";
		metadata.contactInformation.put("homepage", "https://codetoil.io");
		metadata.contactInformation.put("issues", "https://github.com/Codetoil/curved-spacetime/issues");
		metadata.contactInformation.put("sources", "https://github.com/Codetoil/curved-spacetime.git");
		metadata.setIcons(new Icons.Single("assets/curved-spacetime-main-module/icon.png"));
		VersionRange javaRange = VersionRange.ofInterval(Version.of("21"), true, null, false);
		metadata.depends.add(new ModDependency.Only()
		{
			@Override
			public boolean shouldIgnore()
			{
				return false;
			}

			@Override
			public VersionRange versionRange()
			{
				return javaRange;
			}

			@Override
			public ModDependency unless()
			{
				return null;
			}

			@Override
			public String reason()
			{
				return "";
			}

			@Override
			public boolean optional()
			{
				return false;
			}

			@Override
			public ModDependencyIdentifier id()
			{
				return ModDependencyIdentifier.of("", "java");
			}
		});
		List<Path> paths = new ArrayList<>(this.gameJars);
		return Collections.singletonList(new BuiltinMod(paths, metadata.build()));
	}

	@Override
	public List<Path> getGameJars(@Nullable String namespace)
	{
		if (namespace == null)
		{
			return this.gameJarsByNamespace.get(QuiltLauncherBase.getLauncher().getTargetNamespace());
		}
		return this.gameJarsByNamespace.get(namespace);
	}

	@Override
	public String getEntrypoint()
	{
		return CurvedSpacetimeGameProvider.ENTRYPOINT;
	}

	@Override
	public Path getLaunchDirectory()
	{
		if (this.arguments == null)
		{
			return Paths.get(".");
		}

		return getLaunchDirectory(this.arguments);
	}

	@Override
	public MappingConfiguration getMappingConfiguration()
	{
		return this.mappingConfiguration;
	}

	@Override
	public boolean requiresUrlClassLoader()
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public boolean locateGame(QuiltLauncher launcher, String[] args)
	{
		this.arguments = new Arguments();
		this.arguments.parse(args);

		try
		{
			LibClassifier<CurvedSpacetimeModule> classifier =
					new LibClassifier<>(CurvedSpacetimeModule.class, KnotCurvedSpacetime.CURVED_SPACETIME, this);
			Path gameJar = GameProviderHelper.getCommonGameJar();
			if (gameJar != null)
			{
				classifier.process(gameJar);
			}

			// Set<Path> classpath = new LinkedHashSet<>();

			for (Path path : launcher.getClassPath())
			{
				path = LoaderUtil.normalizeExistingPath(path);
				// classpath.add(path);
				classifier.process(path);
			}

			gameJar = classifier.getOrigin(CurvedSpacetimeModule.MAIN_MODULE);

			if (gameJar == null) throw new RuntimeException("Game could not be found!");

			this.gameJars.add(gameJar);

			this.miscGameLibraries.addAll(
					classifier.getUnmatchedOrigins().stream().filter(this.gameJars::contains).toList());
		} catch (IOException e)
		{
			throw ExceptionUtil.wrap(e);
		}

		processArgumentMap(arguments);

		return true;
	}

	private static void processArgumentMap(Arguments argMap)
	{
		Objects.requireNonNull(KnotCurvedSpacetime.CURVED_SPACETIME);
		if (!argMap.containsKey("gameDir"))
		{
			argMap.put("gameDir", getLaunchDirectory(argMap).toAbsolutePath().normalize().toString());
		}
	}

	private static Path getLaunchDirectory(Arguments argMap)
	{
		return Paths.get(argMap.getOrDefault("gameDir", "."));
	}

	@Override
	public boolean isGameClass(String name)
	{
		return name.startsWith("io.codetoil.curved_spacetime.");
	}

	@Override
	public void initialize(QuiltLauncher launcher)
	{
		this.gameJarsByNamespace.put(launcher.getEntrypoint(), Collections.unmodifiableList(this.gameJars));
		this.gameJarsByNamespace = Collections.unmodifiableMap(this.gameJarsByNamespace);
		this.transformer.locateEntrypoints(launcher, QuiltLauncherBase.getLauncher().getTargetNamespace(),
				this.gameJars);
	}

	@Override
	public Arguments getArguments()
	{
		return this.arguments;
	}

	@Override
	public String[] getLaunchArguments(boolean sanitize)
	{
		return this.arguments == null ? new String[0] : this.arguments.toArray();
	}

	@Override
	public GameTransformer getEntrypointTransformer()
	{
		return this.transformer;
	}

	@Override
	public boolean hasAwtSupport()
	{
		return !LoaderUtil.hasMacOs();
	}

	@Override
	public void unlockClassPath(QuiltLauncher launcher)
	{
		for (Path gameJar : this.gameJars)
		{
			launcher.addToClassPath(gameJar);
		}

		for (Path lib : this.miscGameLibraries)
		{
			launcher.addToClassPath(lib);
		}
	}

	@Override
	public void launch(ClassLoader loader)
	{
		Log.debug(LogCategory.GAME_PROVIDER, "Launching Game");

		try
		{
			Class<?> c = loader.loadClass("io.codetoil.curved_spacetime.Main");
			Method m = c.getMethod("main", String[].class);
			m.invoke(null, (Object) this.arguments.toArray());
		} catch (InvocationTargetException e)
		{
			throw new FormattedException("Curved Spacetime has crashed!", e.getCause());
		} catch (ReflectiveOperationException e)
		{
			throw new FormattedException("Failed to start Curved Spacetime", e);
		}
	}
}

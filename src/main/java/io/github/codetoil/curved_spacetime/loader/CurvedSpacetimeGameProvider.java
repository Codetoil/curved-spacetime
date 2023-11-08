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

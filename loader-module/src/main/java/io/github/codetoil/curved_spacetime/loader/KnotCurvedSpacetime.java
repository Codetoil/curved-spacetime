package io.github.codetoil.curved_spacetime.loader;

import net.fabricmc.api.EnvType;
import org.quiltmc.loader.impl.launch.knot.Knot;
import org.quiltmc.loader.impl.util.SystemProperties;

public class KnotCurvedSpacetime {
    public static void main(String[] args) {
        System.setProperty(SystemProperties.SKIP_MC_PROVIDER, "true");
        System.setProperty(SystemProperties.MODS_DIRECTORY, "modules");
        Knot.launch(args, EnvType.valueOf("CURVED_SPACETIME"));
    }
}

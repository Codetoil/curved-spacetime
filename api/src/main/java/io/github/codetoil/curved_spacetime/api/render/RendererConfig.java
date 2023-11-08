package io.github.codetoil.curved_spacetime.api.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RendererConfig {
    private static final int DEFAULT_FPS = 60;
    private static final String FILENAME = "renderer.properties";
    private int fps;

    public RendererConfig() {

    }

    public int getFPS() {
        return this.fps;
    }

    public RendererConfig load() throws IOException {
        Properties props = new Properties();

        try (InputStream stream = RendererConfig.class.getResourceAsStream("/" + FILENAME)) {
            if (stream != null)
                props.load(stream);
            this.fps = Integer.parseInt(props.getOrDefault("fps", DEFAULT_FPS).toString());
        }

        return this;
    }
}

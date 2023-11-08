package io.github.codetoil.curved_spacetime.api.render;

import io.github.codetoil.curved_spacetime.api.engine.Engine;
import io.github.codetoil.curved_spacetime.api.scene.Scene;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public abstract class Renderer {
    protected final RendererConfig rendererConfig;
    protected final Engine engine;
    protected ScheduledExecutorService executor;
    protected ScheduledFuture<?> frameHandler;
    protected final Scene scene;
    protected Window window;

    protected Renderer(Engine engine, Scene scene)
    {
        this.engine = engine;

        try {
            this.rendererConfig = new RendererConfig().load();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load Vulkan Renderer Config", ex);
        }
        this.scene = scene;
    }

    public void clean() {
        this.frameHandler.cancel(true);
        this.executor.shutdown();
        this.window.clean();
    }

    public abstract void render();

    public Window getWindow() {
        return this.window;
    }
}

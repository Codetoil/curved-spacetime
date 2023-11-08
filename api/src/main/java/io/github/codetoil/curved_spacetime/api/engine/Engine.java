package io.github.codetoil.curved_spacetime.api.engine;

import io.github.codetoil.curved_spacetime.api.render.Renderer;
import io.github.codetoil.curved_spacetime.api.scene.Scene;

public class Engine {
    public Renderer renderer;
    public Scene scene;

    public Engine() {
        this.scene = new Scene();
    }

    public void clean() {
        this.renderer.getWindow().hideWindow();
        this.renderer.clean();
    }

    public void stop() {
        this.clean();
    }
}

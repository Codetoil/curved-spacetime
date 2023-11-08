package io.github.codetoil.curved_spacetime.api.render;

import io.github.codetoil.curved_spacetime.api.engine.Engine;

public abstract class Window {
    protected Engine engine;

    protected Window(Engine engine)
    {
        this.engine = engine;
    }

    public abstract void init();

    public abstract void loop();

    public abstract void showWindow();
    public abstract void hideWindow();

    public abstract void clean();
}

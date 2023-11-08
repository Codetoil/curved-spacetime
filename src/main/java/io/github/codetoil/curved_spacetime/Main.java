package io.github.codetoil.curved_spacetime;

import io.github.codetoil.curved_spacetime.api.engine.Engine;
import io.github.codetoil.curved_spacetime.render.vulkan.VulkanRenderer;

public class Main {

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.renderer = new VulkanRenderer(engine, engine.scene);
    }}

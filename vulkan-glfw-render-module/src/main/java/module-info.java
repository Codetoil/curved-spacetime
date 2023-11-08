module io.github.codetoil.curved_spacetime.render.vulkan {
    requires org.tinylog.api;
    requires io.github.codetoil.curved_spacetime.api;
    requires io.github.codetoil.curved_spacetime.vulkan;
    requires io.github.codetoil.curved_spacetime.render.glfw;
    requires org.lwjgl;
    requires org.lwjgl.vulkan;
    requires org.lwjgl.glfw;

    exports io.github.codetoil.curved_spacetime.render.vulkan;
}
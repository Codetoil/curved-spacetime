package io.github.codetoil.curved_spacetime.render.vulkan;

import io.github.codetoil.curved_spacetime.api.engine.Engine;
import io.github.codetoil.curved_spacetime.api.scene.Scene;
import io.github.codetoil.curved_spacetime.api.render.Renderer;
import io.github.codetoil.curved_spacetime.vulkan.VulkanLogicalDevice;
import io.github.codetoil.curved_spacetime.vulkan.VulkanPhysicalDevice;
import io.github.codetoil.curved_spacetime.vulkan.VulkanInstance;
import org.lwjgl.glfw.GLFWVulkan;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VulkanRenderer extends Renderer {
    protected final VulkanInstance vulkanInstance;
    protected final VulkanPhysicalDevice vulkanPhysicalDevice;
    protected final VulkanLogicalDevice vulkanLogicalDevice;
    protected final VulkanGraphicsQueue vulkanGraphicsQueue;
    protected final VulkanSurface vulkanSurface;


    public VulkanRenderer(Engine engine, Scene scene) {
        super(engine, scene);

        this.window = new VulkanWindow(engine);

        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.window.init();

        this.vulkanInstance = new VulkanInstance(true, GLFWVulkan::glfwGetRequiredInstanceExtensions);
        this.vulkanPhysicalDevice = VulkanPhysicalDevice.createPhysicalDevice(vulkanInstance, null);
        this.vulkanLogicalDevice = new VulkanLogicalDevice(this.vulkanPhysicalDevice);
        this.vulkanSurface = new VulkanSurface(vulkanPhysicalDevice, ((VulkanWindow) window).getWindowHandle());
        this.vulkanGraphicsQueue = new VulkanGraphicsQueue(this.vulkanLogicalDevice, 0);

        this.frameHandler = this.executor.scheduleAtFixedRate(this.window::loop,
                1_000 / this.rendererConfig.getFPS(), 1_000 / this.rendererConfig.getFPS(),
                TimeUnit.MILLISECONDS);
        this.window.showWindow();
    }

    public void clean() {
        super.clean();
        this.vulkanSurface.cleanup();
        this.vulkanLogicalDevice.cleanup();
        this.vulkanPhysicalDevice.cleanup();
        this.vulkanInstance.cleanup();
    }

    public void render() {
    }
}

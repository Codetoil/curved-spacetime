package io.codetoil.curved_spacetime.render.vulkan;

import io.codetoil.curved_spacetime.api.engine.Engine;
import io.codetoil.curved_spacetime.api.loader.ModuleInitializer;
import org.quiltmc.loader.api.QuiltLoader;

public class VulkanGLFWRendererModuleEntrypoint implements ModuleInitializer
{
	@Override
	public void onInitialize()
	{
		Engine engine = (Engine) QuiltLoader.getGameInstance();
		assert engine != null;
		engine.renderer = new VulkanRenderer(engine, engine.scene);
	}
}

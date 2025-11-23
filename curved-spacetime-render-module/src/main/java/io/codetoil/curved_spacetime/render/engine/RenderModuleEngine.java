package io.codetoil.curved_spacetime.render.engine;

import com.google.common.collect.Sets;
import io.codetoil.curved_spacetime.MainCallback;
import io.codetoil.curved_spacetime.engine.Engine;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderCallback;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRenderer;
import io.codetoil.curved_spacetime.render.scene_renderer.RenderModuleSceneRendererCallbackSupplier;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class RenderModuleEngine implements MainCallback
{
	protected final Set<RenderModuleSceneRenderer> renderModuleSceneRenderers = new HashSet<>();
	private final Set<RenderModuleSceneRenderCallback> renderEnvironmentCallbacks = Sets.newConcurrentHashSet();

	@Override
	public void init()
	{
		Logger.info("Accumulating Render Environment Callbacks");
		Engine.getInstance().accumulateCallbacks(RenderModuleSceneRendererCallbackSupplier.class,
				this.renderModuleSceneRenderers, this.renderEnvironmentCallbacks);
		Logger.info("Initializing Render Environment Callbacks");
		this.renderEnvironmentCallbacks.forEach(RenderModuleSceneRenderCallback::init);
	}

	@Override
	public void loop()
	{
		this.renderEnvironmentCallbacks.forEach(RenderModuleSceneRenderCallback::loop);
	}

	@Override
	public void clean()
	{
		this.renderModuleSceneRenderers.forEach(this::deregisterRenderEnvironment);
	}

	public void deregisterRenderEnvironment(RenderModuleSceneRenderer renderModuleSceneRenderer)
	{
		this.renderEnvironmentCallbacks.stream()
				.filter(callback ->
						Objects.equals(callback.renderEnviornment(), renderModuleSceneRenderer))
				.toList().forEach(callback -> {
					callback.clean();
					this.renderEnvironmentCallbacks.remove(callback);
				});
		this.renderModuleSceneRenderers.remove(renderModuleSceneRenderer);
	}

	public Set<RenderModuleSceneRenderCallback> registerRenderEnvironmentCallbackAndInit
			(Function<RenderModuleSceneRenderer, RenderModuleSceneRenderCallback> callbackSupplier)
	{
		return Engine.getInstance().registerCallbackAndInit(callbackSupplier, this.renderModuleSceneRenderers,
				this.renderEnvironmentCallbacks, RenderModuleSceneRenderCallback::init);
	}

	public Set<RenderModuleSceneRenderer> getRenderEnvironments()
	{
		return renderModuleSceneRenderers;
	}

	public Set<RenderModuleSceneRenderCallback> getRenderEnvironmentCallbacks()
	{
		return renderEnvironmentCallbacks;
	}
}

package io.codetoil.curved_spacetime.render.engine;

import com.google.common.collect.Sets;
import io.codetoil.curved_spacetime.MainCallback;
import io.codetoil.curved_spacetime.engine.Engine;
import io.codetoil.curved_spacetime.render.render_enviornments.RenderEnviornmentCallback;
import io.codetoil.curved_spacetime.render.render_enviornments.RenderEnviornmentCallbackSupplier;
import io.codetoil.curved_spacetime.render.render_enviornments.RenderEnvironment;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class RenderModuleEngine implements MainCallback
{
	protected final Set<RenderEnvironment> renderEnvironments = new HashSet<>();
	private final Set<RenderEnviornmentCallback> renderEnvironmentCallbacks = Sets.newConcurrentHashSet();

	@Override
	public void init()
	{
		Engine.getInstance().accumulateCallbacks(RenderEnviornmentCallbackSupplier.class,
				this.renderEnvironments, this.renderEnvironmentCallbacks);
		this.renderEnvironmentCallbacks.forEach(RenderEnviornmentCallback::init);
	}

	@Override
	public void loop()
	{
		this.renderEnvironmentCallbacks.forEach(RenderEnviornmentCallback::loop);
	}

	@Override
	public void clean()
	{
		this.renderEnvironments.forEach(this::deregisterRenderEnvironment);
	}

	public void deregisterRenderEnvironment(RenderEnvironment renderEnvironment)
	{
		this.renderEnvironmentCallbacks.stream()
				.filter(callback ->
						Objects.equals(callback.renderEnviornment(), renderEnvironment))
				.toList().forEach(callback -> {
					callback.clean();
					this.renderEnvironmentCallbacks.remove(callback);
				});
		this.renderEnvironments.remove(renderEnvironment);
	}

	public void registerRenderEnvironmentCallbackAndInit
			(Function<RenderEnvironment, RenderEnviornmentCallback> callbackSupplier)
	{
		Engine.getInstance().registerCallbackAndInit(callbackSupplier, this.renderEnvironments,
				this.renderEnvironmentCallbacks, RenderEnviornmentCallback::init);
	}

	public Set<RenderEnvironment> getRenderEnvironments()
	{
		return renderEnvironments;
	}

	public Set<RenderEnviornmentCallback> getRenderEnvironmentCallbacks()
	{
		return renderEnvironmentCallbacks;
	}
}

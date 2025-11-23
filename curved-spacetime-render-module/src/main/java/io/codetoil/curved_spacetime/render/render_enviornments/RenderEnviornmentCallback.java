package io.codetoil.curved_spacetime.render.render_enviornments;

public interface RenderEnviornmentCallback
{

	void init();

	void loop();

	void clean();

	RenderEnvironment renderEnviornment();
}

package io.codetoil.curved_spacetime;

import io.codetoil.curved_spacetime.loader.CurvedSpacetimeLoader;

public class Start
{
	public static void start(String[] args, CurvedSpacetimeLoader loader)
	{
		new MainModuleEngine(loader);
		// TODO implement argument handling
	}
}
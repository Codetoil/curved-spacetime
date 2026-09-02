/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br>
 * <br>
 * This file is part of Curved Spacetime<br>
 * <br>
 * This program is free software: you can redistribute it and/or modify <br> it under the terms of the GNU General
 * Public License as published by <br> the Free Software Foundation, either version 3 of the License, or <br> (at your
 * option) any later version.<br>
 * <br>
 * This program is distributed in the hope that it will be useful,<br> but WITHOUT ANY WARRANTY; without even the
 * implied warranty of<br> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br> GNU General Public License
 * for more details.<br>
 * <br>
 * You should have received a copy of the GNU General Public License<br> along with this program.  If not, see <a
 * href="https://www.gnu.org/licenses/">https://www.gnu.org/licenses/</a>.<br>
 */

package io.codetoil.curved_spacetime.webserver.openapi;

import io.codetoil.curved_spacetime.MainModuleEngine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.webserver.WebserverModuleEntrypoint;
import io.codetoil.curved_spacetime.webserver.entrypoint.WebserverModuleDependentModuleInitializer;

/**
 * Delivers the webserver module's entrypoint to the OpenAPI module.
 * <p>
 * Registered by the OpenAPI module under {@code webserver_module_dependent}. The webserver module
 * invokes this during its own initialization; it locates the OpenAPI module's main entrypoint
 * through the loader and transfers the webserver entrypoint into that module's dependency queue,
 * where the OpenAPI module is blocked waiting for it.
 */
public class WebserverModuleDependentWebserverOpenAPIModuleEntrypoint implements
		WebserverModuleDependentModuleInitializer
{
	/**
	 * Creates the dependent entrypoint.
	 * <p>
	 * Called by the loader; it holds no state of its own.
	 */
	public WebserverModuleDependentWebserverOpenAPIModuleEntrypoint()
	{
	}


	@Override
	public void onInitialize(WebserverModuleEntrypoint webserverModuleEntrypoint)
	{
		try
		{
			MainModuleEngine.getInstance().getCurvedSpacetimeLoader()
					.getEntrypoints("main", ModuleInitializer.class).stream()
					.filter(WebserverOpenAPIModuleEntrypoint.class::isInstance)
					.findFirst().orElseThrow().getDependencyModuleTransferQueue().transfer(webserverModuleEntrypoint);
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}

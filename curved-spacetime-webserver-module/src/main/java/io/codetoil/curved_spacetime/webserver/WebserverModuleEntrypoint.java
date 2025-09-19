/**
 * Curved Spacetime is an easy-to-use modular simulator for General Relativity.<br>
 * Copyright (C) 2025 Anthony Michalek (Codetoil)<br>
 * Copyright (c) 2024 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.webserver;

import com.sun.net.httpserver.HttpServer;
import io.codetoil.curved_spacetime.api.ModuleDependentFlowSubscriber;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.api.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.api.webserver.WebserverModuleDependentModuleInitializer;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;

public class WebserverModuleEntrypoint implements ModuleInitializer
{
	private ModuleConfig config;
	private final Flow.Subscriber<ModuleInitializer> moduleDependentFlowSubscriber
			= new ModuleDependentFlowSubscriber((Collection<ModuleInitializer> _) -> {});

	@Override
	public void onInitialize()
	{
		try
		{
			this.config = new WebserverModuleConfig().load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Vulkan Render Config", ex);
		}

		try
		{ // from https://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
			HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
			server.createContext("/test", exchange -> {
				String response = "This is the response";
				exchange.sendResponseHeaders(200, response.length());
				OutputStream os = exchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			});
			server.setExecutor(null);
			server.start();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load start test server", ex);
		}
		System.out.println("Started test server");
		EntrypointUtil.invoke("webserver_module_dependent", WebserverModuleDependentModuleInitializer.class,
				(WebserverModuleDependentModuleInitializer webserverModuleDependentModuleInitializer) ->
						webserverModuleDependentModuleInitializer.onInitialize(this));
	}

	@Override
	public ModuleConfig getConfig()
	{
		return this.config;
	}

	@Override
	public Subscriber<ModuleInitializer> getModuleDependentFlowSubscriber()
	{
		return this.moduleDependentFlowSubscriber;
	}
}

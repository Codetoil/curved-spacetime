/**
 * Curved Spacetime is a work-in-progress easy-to-use modular simulator for General Relativity.<br> Copyright (C) 2025
 * Anthony Michalek (Codetoil)<br> Copyright (c) 2025 Antonio Hernández Bejarano<br>
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

package io.codetoil.curved_spacetime.spring.web;

import io.codetoil.curved_spacetime.engine.Engine;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleConfig;
import io.codetoil.curved_spacetime.loader.entrypoint.ModuleInitializer;
import io.codetoil.curved_spacetime.spring.web.entrypoint.SpringWebModuleDependentModuleInitializer;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class SpringWebModuleEntrypoint implements ModuleInitializer
{
	private final TransferQueue<ModuleInitializer> dependencyModuleTransferQueue = new LinkedTransferQueue<>();
	private ModuleConfig config;

	@Override
	public void onInitialize()
	{
		try
		{
			this.config = new SpringWebModuleConfig().load();
			if (this.config.isDirty()) this.config.save();
		} catch (IOException ex)
		{
			throw new RuntimeException("Failed to load Spring Web Module Config", ex);
		}

		ReactorHttpHandlerAdapter adapter = getReactorHttpHandlerAdapter();
		HttpServer.create().host("localhost").port(8000).handle(adapter).bindNow();


		try
		{
			Engine.callDependents("spring_web_module_dependent", SpringWebModuleDependentModuleInitializer.class,
					(SpringWebModuleDependentModuleInitializer springWebModuleDependentModuleInitializer) ->
							springWebModuleDependentModuleInitializer.onInitialize(this));
		} catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	private static ReactorHttpHandlerAdapter getReactorHttpHandlerAdapter()
	{
		HttpHandler handler = (ServerHttpRequest request, ServerHttpResponse response) ->
				Mono.create((monoSink) -> {
					if (request.getPath().value().equals("/test"))
					{
						String value = "This is the response";
						byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
						response.setStatusCode(HttpStatusCode.valueOf(200));
						response.getHeaders().setContentLength(bytes.length);
						response.writeAndFlushWith(Mono.create((monoSink1) ->
								monoSink1.success(Mono.create(monoSink2 ->
										monoSink2.success(new NettyDataBufferFactory(ByteBufAllocator.DEFAULT)
												.wrap(bytes))))));
						monoSink.success();
					} else
					{
						response.setStatusCode(HttpStatusCode.valueOf(404));
						monoSink.success();
					}
				});
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
		return adapter;
	}

	@Override
	public ModuleConfig getConfig()
	{
		return this.config;
	}

	@Override
	public TransferQueue<ModuleInitializer> getDependencyModuleTransferQueue()
	{
		return this.dependencyModuleTransferQueue;
	}
}

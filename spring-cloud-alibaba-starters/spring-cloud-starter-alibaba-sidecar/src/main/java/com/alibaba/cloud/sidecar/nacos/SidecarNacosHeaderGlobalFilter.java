/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sidecar.nacos;

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author zain
 * @date 2020/11/13 11:26 上午
 */
public class SidecarNacosHeaderGlobalFilter implements GlobalFilter, Ordered {

	private final String sidecarIp;

	private final Integer port;

	public SidecarNacosHeaderGlobalFilter(String sidecarIp, Integer port) {
		this.sidecarIp = sidecarIp;
		this.port = port;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest().mutate()
				.header("XXX-Origin", sidecarIp + ":" + port).build();
		ServerWebExchange newExchange = exchange.mutate().request(request).build();
		return chain.filter(newExchange);
	}

	@Override
	public int getOrder() {
		return -2;
	}

}

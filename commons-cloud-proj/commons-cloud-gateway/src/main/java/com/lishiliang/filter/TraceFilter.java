package com.lishiliang.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 自定义filter
 */
public class TraceFilter implements GatewayFilter, Ordered {
 

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 120;
	}
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // todo

		return chain.filter(exchange);
	}
	
	

 
}
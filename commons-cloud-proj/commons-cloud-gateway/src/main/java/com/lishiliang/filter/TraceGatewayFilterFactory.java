
package com.lishiliang.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * filter工厂 最终会以类名称为key(如果类名称以GatewayFilterFactory结尾则 只使用前一部分 如TraceGatewayFilterFactory的key为Trace)解析到 RouteDefinitionRouteLocator#gatewayFilterFactories中
 */
@Component
public class TraceGatewayFilterFactory extends AbstractGatewayFilterFactory {

    @Override
    public GatewayFilter apply(Object config) {
        return new TraceFilter();
    }
}

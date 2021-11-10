package com.lishiliang;

import com.lishiliang.filter.TraceFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author
 * @version 1.0
 * @date 2021-11-10 10:05
 * @desc :
 */
@SpringBootApplication
public class GateWayApplication {
    public static void main(String[] args) {

        //#表明gateway开启服务注册和发现的功能，并且spring cloud gateway自动根据服务发现为每一个服务创建了一个router，这个router将以服务名开头的请求路径转发到对应的服务。
        System.setProperty("spring.cloud.gateway.discovery.locator.enabled", "true");
        //#是将请求路径上的服务名配置为小写（因为服务注册的时候，向注册中心注册时将服务名转成大写的了），比如以/service-hi/*的请求路径被路由转发到服务名为service-hi的服务上。
        System.setProperty("spring.cloud.gateway.discovery.locator.lower-case-service-id", "true");

        SpringApplication.run(GateWayApplication.class);
    }

    @Bean
    public RouteLocator gateWayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()
                //p.path 设置为需要拦截的服务名
                //http形式
                //.route(p -> p.path("/elastic-job-facade-impl").filters(f -> f.stripPrefix(1).filter(new TraceFilter())).uri("http://127.0.0.1:8080"))
                //ws(websocket) 形式
                //.route(p -> p.path("/elastic-job-facade-impl/**").filters(f -> f.stripPrefix(1).filter(new TraceFilter())).uri("ws://127.0.0.1:8080"))
                //lb 注册中心形式
                .route(p -> p.path("/elastic-job-facade-impl/**").filters(f -> f.stripPrefix(1).filter(new TraceFilter())).uri("lb://elastic-job-facade-impl"))
                .build();
    }

}

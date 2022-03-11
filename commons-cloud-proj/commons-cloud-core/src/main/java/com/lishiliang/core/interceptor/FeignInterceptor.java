package com.lishiliang.core.interceptor;

import com.lishiliang.core.utils.Context;
import com.lishiliang.core.utils.IpUtils;
import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-10-19 10:07
 * @desc :
 */
@Configuration
public class FeignInterceptor  {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    private String ip = IpUtils.getIp();

    @Autowired(required = false)
    private HttpServletRequest request;

    /**
     * 添加全链路追踪traceId
     * @return
     */
    @Bean
    @ConditionalOnClass({RequestInterceptor.class, Feign.class})
    public RequestInterceptor traceInterceptor() {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header(Context.TRACE_ID, Context.getCurrentContextTraceId());
            }
        };
    }

    /**
     * 约定feign调用标识 header添加feign标识
     * @return
     */
    @Bean
    @ConditionalOnClass({RequestInterceptor.class, Feign.class})
    public RequestInterceptor feignFlagIterceptor() {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("feign", String.format("request %s[%s:%s]", applicationName, ip, serverPort));
            }
        };
    }

    @Bean
    @ConditionalOnClass({RequestInterceptor.class, Feign.class})
    public RequestInterceptor targetUrlIterceptor() {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("targetUrl", (String) request.getAttribute("targetUrl"));
            }
        };
    }
}

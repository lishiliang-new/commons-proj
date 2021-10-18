
/**
 * @Title: XssFilterConfiguration.java
 * @Package:com.lishiliang.framework.web.configuration
 * @desc: TODO
 * @author: lisl
 * @date:2020年7月13日 下午1:59:57
 */

package com.lishiliang.web.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lishiliang.web.filter.XssFilter;

/**
 * @author lisl
 * @desc 阻止XSS攻击
 */
@Configuration
public class XssFilterConfiguration {
    
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistrationBean() {
        
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        registration.setName("XssFilter");
        registration.setOrder(Integer.MAX_VALUE);
        registration.addUrlPatterns("/*"); //添加拦截路径
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put("excludes", "/css/*,/js/*,/image/*"); //添加例外
        registration.setInitParameters(initParameters);
        return registration;
    }
}

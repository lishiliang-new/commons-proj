package com.lishiliang.core.configuration;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.registry.nacos.NacosRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多注册中心之Nacos注册中心配置
 */
@Configuration
@ConditionalOnClass(NacosRegistry.class)
@ConditionalOnExpression("!'${dubbo.registry.nacos.address:}'.equals('')")
public class NacosRegistryConfig {

    @Bean(name = "nacosRegistry")
    @ConditionalOnClass(NacosRegistry.class)
    public RegistryConfig nacosRegistry(@Value("${dubbo.registry.nacos.address}") String nacosAddress) {
        RegistryConfig registry = new RegistryConfig();
        registry.setId("nacosRegistry");
        registry.setAddress(nacosAddress);
        registry.setClient("namingService");
        //是否注册自己(对外暴露)
        registry.setRegister(true);
        registry.setCheck(false);
//        registry.setDefault(true);
        return registry;
    }
}
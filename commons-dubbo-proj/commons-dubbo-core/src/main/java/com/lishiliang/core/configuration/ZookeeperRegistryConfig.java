package com.lishiliang.core.configuration;

import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.registry.zookeeper.ZookeeperRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多注册中心之Zookeeper注册中心配置
 */
@Configuration
@ConditionalOnClass(ZookeeperRegistry.class)
@ConditionalOnExpression("!'${dubbo.registry.zookeeper.address:}'.equals('')")
public class ZookeeperRegistryConfig {


    @Bean(name = "zookeeperRegistry")
    @ConditionalOnClass(ZookeeperRegistry.class)
    public RegistryConfig zookeeperRegistryConfig(@Value("${dubbo.registry.zookeeper.address}") String zookeeperAddress) {
        RegistryConfig registry = new RegistryConfig();
        registry.setId("zookeeperRegistry");
        registry.setAddress(zookeeperAddress);
        registry.setClient("curator");
        //是否注册自己(对外暴露)
        registry.setRegister(true);
        registry.setCheck(false);
        //默认使用的注册中心
        registry.setDefault(true);
        return registry;
    }
}
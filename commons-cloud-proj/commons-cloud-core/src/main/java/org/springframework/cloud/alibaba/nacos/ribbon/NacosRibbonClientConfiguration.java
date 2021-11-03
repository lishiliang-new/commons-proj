////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by Fernflower decompiler)
////
//
//package org.springframework.cloud.alibaba.nacos.ribbon;
//
//import com.netflix.client.config.IClientConfig;
//import com.netflix.loadbalancer.ServerList;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConditionalOnRibbonNacos
//public class NacosRibbonClientConfiguration {
//    public NacosRibbonClientConfiguration() {
//    }
//
//    @Autowired
//    private ServerList ribbonEurekaServerList;
//
//    @Bean
//    @ConditionalOnMissingBean
//    public ServerList<?> ribbonNacosServerList(IClientConfig config, NacosDiscoveryProperties nacosDiscoveryProperties) {
//        NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
//        serverList.initWithNiwsConfig(config);
//        return serverList;
//    }
//}

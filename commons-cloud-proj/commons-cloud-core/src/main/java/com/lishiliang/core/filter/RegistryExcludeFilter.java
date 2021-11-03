package com.lishiliang.core.filter;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-10-27 17:38
 * @desc :
 */
public class RegistryExcludeFilter implements AutoConfigurationImportFilter {

    /**
     * 排除这两个配置  可以实现多注册中心 等效
     * // 多注册中心 需要排除exclude = {AutoServiceRegistrationAutoConfiguration.class}
     * @SpringBootApplication(exclude = {AutoServiceRegistrationAutoConfiguration.class}) 排除后的影响面 需要启动@EnableDiscoveryClient并且AutoServiceRegistrationProperties中的failFast 字段失效
     */
    private static final Set<String> SHOULD_SKIP = new HashSet<>(
        Arrays.asList("org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration",
                "org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration"));


    @Override
    public boolean[] match(String[] classNames, AutoConfigurationMetadata metadata) {

        boolean[] matches = new boolean[classNames.length];

        for (int i = 0; i < classNames.length; i++) {
            matches[i] = !SHOULD_SKIP.contains(classNames[i]);
        }

        return matches;
    }
}
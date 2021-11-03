//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.lishiliang.core.configuration;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.discovery.EurekaClient;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServerList;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.cloud.netflix.ribbon.eureka.DomainExtractingServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


/**
 * 自定义 多注册中心下拉取服务列表
 * 配合启动类 (注意指定的defaultConfiguration一定要是第一个 )@RibbonClients(defaultConfiguration = {CustomizeRibbonClientConfiguration.class,EurekaRibbonClientConfiguration.class, NacosRibbonClientConfiguration.class,EurekaRibbonClientConfiguration.class})
 */
@Configuration
@Lazy
public class CustomizeRibbonClientConfiguration {

    @Value("${ribbon.eureka.approximateZoneFromHostname:false}")
    private boolean approximateZoneFromHostname = false;

    private String serviceId = "client";

    @Autowired
    private PropertiesFactory propertiesFactory;

    @Autowired
    private HttpServletRequest request;

    public CustomizeRibbonClientConfiguration() {

    }


    private IClientConfig getIClientConfig() throws Exception {
        //@see FeignAspect
        this.serviceId = ((String)request.getAttribute("from-to")).split("-to>>>")[1];
        return DefaultClientConfigImpl.Builder.newBuilder(serviceId).build();
    }



    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public ServerList<?> customizeServerList(Provider<EurekaClient> eurekaClientProvider) throws Exception {

        IClientConfig config = getIClientConfig();

        if (this.propertiesFactory.isSet(ServerList.class, this.serviceId)) {
            return (ServerList)this.propertiesFactory.get(ServerList.class, config, this.serviceId);

        } else {
            DiscoveryEnabledNIWSServerList discoveryServerList = new DiscoveryEnabledNIWSServerList(config, eurekaClientProvider);
            return new DomainExtractingServerList(discoveryServerList, config, this.approximateZoneFromHostname);
        }
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public ServerList<?> customizeServerList(NacosDiscoveryProperties nacosDiscoveryProperties) throws Exception {
        NacosServerList serverList = new NacosServerList(nacosDiscoveryProperties);
        serverList.initWithNiwsConfig(getIClientConfig());
        return serverList;
    }


    @Bean
    @ConditionalOnProperty(value = {"ribbon.nacos.enabled"}, matchIfMissing = true)
    @ConditionalOnBean(value = {NacosDiscoveryProperties.class, EurekaClient.class})
    @ConditionalOnMissingBean
    public ServerList<?> customizeServerList(Provider<EurekaClient> eurekaClientProvider, NacosDiscoveryProperties nacosDiscoveryProperties) throws Exception {
        IClientConfig config = getIClientConfig();

        if (this.propertiesFactory.isSet(ServerList.class, this.serviceId)) {
            return (ServerList)this.propertiesFactory.get(ServerList.class, config, this.serviceId);
        } else {

            DiscoveryEnabledNIWSServerList discoveryServerList = new DiscoveryEnabledNIWSServerList(config, eurekaClientProvider);
            DomainExtractingServerList eurekaServerList = new DomainExtractingServerList(discoveryServerList, config, this.approximateZoneFromHostname);

            NacosServerList nacosServerList = new NacosServerList(nacosDiscoveryProperties);
            nacosServerList.initWithNiwsConfig(config);

            CustomizeServerList customizeServerList = new CustomizeServerList(eurekaServerList, nacosServerList);

            return customizeServerList;
        }
    }


    public class CustomizeServerList implements ServerList<Server> {

        private String serviceId;

        private DomainExtractingServerList eurekaServerList;
        private NacosServerList nacosServerList;


        public CustomizeServerList(DomainExtractingServerList eurekaServerList, NacosServerList nacosServerList) {
            this.eurekaServerList = eurekaServerList;
            this.nacosServerList = nacosServerList;
        }

        public List<Server> getInitialListOfServers() {
            List<Server> allInitialServer = new ArrayList<>();
            if (this.eurekaServerList != null) {
                allInitialServer.addAll(this.eurekaServerList.getInitialListOfServers());
            }
            if (this.nacosServerList != null) {
                allInitialServer.addAll(this.nacosServerList.getInitialListOfServers());
            }

            return allInitialServer;
        }

        public List<Server> getUpdatedListOfServers() {
            List<Server> allUpdateServer = new ArrayList<>();
            if (this.eurekaServerList != null) {
                allUpdateServer.addAll(this.eurekaServerList.getInitialListOfServers());
            }
            if (this.nacosServerList != null) {
                allUpdateServer.addAll(this.nacosServerList.getInitialListOfServers());
            }
            return allUpdateServer;
        }


        public String getServiceId() {
            return this.serviceId;
        }

        public void initWithNiwsConfig(IClientConfig iClientConfig) {
            this.serviceId = iClientConfig.getClientName();
        }
    }
}

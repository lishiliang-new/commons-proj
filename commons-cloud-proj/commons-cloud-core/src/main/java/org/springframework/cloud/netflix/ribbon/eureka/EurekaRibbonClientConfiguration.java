//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

/**
 * copy from org.springframework.cloud.netflix.ribbon.eureka.EurekaRibbonClientConfiguration
 */
package org.springframework.cloud.netflix.ribbon.eureka;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext.ContextKey;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList;
import com.netflix.niws.loadbalancer.NIWSDiscoveryPing;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServerList;
import org.springframework.cloud.netflix.ribbon.PropertiesFactory;
import org.springframework.cloud.netflix.ribbon.RibbonClientName;
import org.springframework.cloud.netflix.ribbon.RibbonUtils;
import org.springframework.cloud.netflix.ribbon.ServerIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class EurekaRibbonClientConfiguration {
    private static final Log log = LogFactory.getLog(EurekaRibbonClientConfiguration.class);
    @Value("${ribbon.eureka.approximateZoneFromHostname:false}")
    private boolean approximateZoneFromHostname = false;
    @RibbonClientName
    private String serviceId = "client";
    @Autowired(
        required = false
    )
    private EurekaClientConfig clientConfig;
    @Autowired(
        required = false
    )
    private EurekaInstanceConfig eurekaConfig;
    @Autowired
    private PropertiesFactory propertiesFactory;

    public EurekaRibbonClientConfiguration() {
    }

    public EurekaRibbonClientConfiguration(EurekaClientConfig clientConfig, String serviceId, EurekaInstanceConfig eurekaConfig, boolean approximateZoneFromHostname) {
        this.clientConfig = clientConfig;
        this.serviceId = serviceId;
        this.eurekaConfig = eurekaConfig;
        this.approximateZoneFromHostname = approximateZoneFromHostname;
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing(IClientConfig config) {
        if (this.propertiesFactory.isSet(IPing.class, this.serviceId)) {
            return (IPing)this.propertiesFactory.get(IPing.class, config, this.serviceId);
        } else {
            NIWSDiscoveryPing ping = new NIWSDiscoveryPing();
            ping.initWithNiwsConfig(config);
            return ping;
        }
    }


    @Value("${ribbon.nacos.enabled:true}")
    private boolean ribbonNacosEnabled;


    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonEurekaServerList(IClientConfig config, Provider<EurekaClient> eurekaClientProvider) {
        if (this.propertiesFactory.isSet(ServerList.class, this.serviceId)) {
            return (ServerList)this.propertiesFactory.get(ServerList.class, config, this.serviceId);
        } else {
            DiscoveryEnabledNIWSServerList discoveryServerList = new DiscoveryEnabledNIWSServerList(config, eurekaClientProvider);
            DomainExtractingServerList serverList = new DomainExtractingServerList(discoveryServerList, config, this.approximateZoneFromHostname);
            return serverList;
        }
    }

    @Bean
    @ConditionalOnProperty(value = {"ribbon.nacos.enabled"}, matchIfMissing = true)
    @ConditionalOnBean(NacosDiscoveryProperties.class)
    @ConditionalOnMissingBean
    public ServerList<?> ribbonEurekaServerList(IClientConfig config, Provider<EurekaClient> eurekaClientProvider, NacosDiscoveryProperties nacosDiscoveryProperties) {
        if (this.propertiesFactory.isSet(ServerList.class, this.serviceId)) {
            return (ServerList)this.propertiesFactory.get(ServerList.class, config, this.serviceId);
        } else {
            //默认以eureka注册中心的订阅列表为准 若eureka无该服务则以nacos服务列表 todo 新增其他注册中心继续加
            DiscoveryEnabledNIWSServerList discoveryServerList = new DiscoveryEnabledNIWSServerList(config, eurekaClientProvider);
            DomainExtractingServerList eurekaServerList = new DomainExtractingServerList(discoveryServerList, config, this.approximateZoneFromHostname);
            NacosServerList nacosServerList = new NacosServerList(nacosDiscoveryProperties);
            nacosServerList.initWithNiwsConfig(config);


            MyServerList myServerList = new MyServerList(eurekaServerList, nacosServerList);
            myServerList.initWithNiwsConfig(config);
            return myServerList;
        }
    }

    @Bean
    public ServerIntrospector serverIntrospector() {
        return new EurekaServerIntrospector();
    }

    @PostConstruct
    public void preprocess() {
        String zone = ConfigurationManager.getDeploymentContext().getValue(ContextKey.zone);
        if (this.clientConfig != null && StringUtils.isEmpty(zone)) {
            String availabilityZone;
            if (this.approximateZoneFromHostname && this.eurekaConfig != null) {
                availabilityZone = ZoneUtils.extractApproximateZone(this.eurekaConfig.getHostName(false));
                log.debug("Setting Zone To " + availabilityZone);
                ConfigurationManager.getDeploymentContext().setValue(ContextKey.zone, availabilityZone);
            } else {
                availabilityZone = this.eurekaConfig == null ? null : (String)this.eurekaConfig.getMetadataMap().get("zone");
                if (availabilityZone == null) {
                    String[] zones = this.clientConfig.getAvailabilityZones(this.clientConfig.getRegion());
                    availabilityZone = zones != null && zones.length > 0 ? zones[0] : null;
                }

                if (availabilityZone != null) {
                    ConfigurationManager.getDeploymentContext().setValue(ContextKey.zone, availabilityZone);
                }
            }
        }

        RibbonUtils.initializeRibbonDefaults(this.serviceId);
    }

    public class MyServerList implements ServerList<Server> {

        private String serviceId;

        private DomainExtractingServerList eurekaServerList;
        private NacosServerList nacosServerList;


        public MyServerList(DomainExtractingServerList eurekaServerList, NacosServerList nacosServerList) {
            this.eurekaServerList = eurekaServerList;
            this.nacosServerList = nacosServerList;
        }

        public List<Server> getInitialListOfServers() {
            List<Server> allInitialServer = new ArrayList<>();
            List eurekaInitialListOfServers = this.eurekaServerList.getInitialListOfServers();
            List nacosinitialListOfServers = this.nacosServerList.getInitialListOfServers();
            allInitialServer.addAll(eurekaInitialListOfServers);
            allInitialServer.addAll(nacosinitialListOfServers);
            return allInitialServer;
        }

        public List<Server> getUpdatedListOfServers() {
            List<Server> allUpdateServer = new ArrayList<>();
            List eurekaInitialListOfServers = this.eurekaServerList.getUpdatedListOfServers();
            List nacosinitialListOfServers = this.nacosServerList.getUpdatedListOfServers();
            allUpdateServer.addAll(eurekaInitialListOfServers);
            allUpdateServer.addAll(nacosinitialListOfServers);
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

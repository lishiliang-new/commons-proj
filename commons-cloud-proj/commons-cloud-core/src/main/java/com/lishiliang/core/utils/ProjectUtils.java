package com.lishiliang.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lisl spring各钟勾子函数
 * @desc
 */
@Component
public class ProjectUtils implements  EnvironmentAware, BeanDefinitionRegistryPostProcessor, BeanPostProcessor, ApplicationRunner {

    Logger logger = LoggerFactory.getLogger(ProjectUtils.class);

    private static Environment environment;

    private static Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();

    private static ConfigurableListableBeanFactory beanFactory;

    private boolean beginProcess = true;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        logger.info("\n\t---------环境变量初始化完成--------");
    }

    /**
     * 获取环境参数
     * @return
     */
    public static Environment getEnvironment() {
        return environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            beanDefinitionMap.put(beanDefinitionName, beanDefinition);
            //todo 可通过removeBeanDefinition，重排序,registerBeanDefinition重新对bean的生成顺序排序
            //registry.removeBeanDefinition(beanDefinitionName);
            //registry.registerBeanDefinition(beanDefinitionName, beanDefinition);
        }
        logger.info("\n\t---------beanDefinitionMap实例化完成--------");

    }

    /**
     * 获取bean定义map
     * @return
     */
    public static Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public static ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("\n\t---------{}服务启动完成--------", environment.getProperty("spring.application.name"));
    }





    /**
     * bean 自定义前置处理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beginProcess) {
            //获取所有bean的增强处理器
            List<BeanPostProcessor> beanPostProcessors = ((DefaultListableBeanFactory) beanFactory).getBeanPostProcessors();
            beginProcess = false;
        }
        return bean;
    }

    /**
     * bean 自定义后置处理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

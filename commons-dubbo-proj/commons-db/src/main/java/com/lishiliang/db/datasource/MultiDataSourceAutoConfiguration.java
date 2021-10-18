/**
 * @Title: MultiDataSourceAutoConfiguration.java
 * @Package:com.superq.framework.core.dataSource
 * @desc: TODO
 * @author: lisl
 * @date:2018年4月8日 下午2:21:49
 */

package com.lishiliang.db.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.p6spy.engine.spy.P6DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;


@Configuration
@ConditionalOnMissingClass("com.alibaba.druid.pool.DruidDataSource")
public class MultiDataSourceAutoConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MultiDataSourceAutoConfiguration.class);
    
    @Autowired
    private Environment env;
    
    /**
     * @desc: 主库
     * @return
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
    
        return DataSourceBuilder.create().build();
    }
    
    /**
     * @desc: 从库
     * @return
     */
    @Bean(name = "slaveDataSource")
    @Qualifier("slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
    
        return DataSourceBuilder.create().build();
    }
    
    /**
     * @desc: 主库
     * @param dataSource
     * @return
     */
    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
    
        return new JdbcTemplate(dataSource);
    }
    
    /**
     * @desc: 从库
     * @param dataSource
     * @return
     */
    @Bean(name = "slaveJdbcTemplate")
    public JdbcTemplate slaveJdbcTemplate(@Qualifier("slaveDataSource") DataSource dataSource) {
    
        return new JdbcTemplate(dataSource);
    }
    
    /**
     * @desc: 主库的Mybatis
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "primarySqlSessionFactory")
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier("primaryDataSource") DataSource dataSource) throws Exception {
    
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        
        factory.setMapperLocations(getMapper());
        setConfigLocation(factory);
        
        return factory.getObject();
    }
    
    /**
     * @desc: 主库的Mybatis
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "slaveSqlSessionFactory")
    public SqlSessionFactory slaveSqlSessionFactory(@Qualifier("slaveDataSource") DataSource dataSource) throws Exception {
    
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        
        factory.setMapperLocations(getMapper());
        setConfigLocation(factory);
        
        return factory.getObject();
    }
    
    /**
     * @desc: 主库
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name="primarySqlSessionTemplate")
    public SqlSessionTemplate primarySqlSessionTemplate(@Qualifier("primarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    
    /**
     * @desc: 从库
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name="slaveSqlSessionTemplate")
    public SqlSessionTemplate slaveSqlSessionTemplate(@Qualifier("slaveSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    
    /**
     * @desc: 主库事务
     * @param dataSource
     * @return
     */
    @Bean(name= "transactionManager")
    public DataSourceTransactionManager primaryDataSourceTransactionManager(@Qualifier("primaryDataSource") DataSource dataSource){
        logger.info("阿里Druid数据源创建主库事务管理器");
        
        return new DataSourceTransactionManager(dataSource);
    }
    /**
     * @desc: 事务模板
     * @param transactionManager
     * @return
     */
    @Bean(name = "transactionTemplate")
    public TransactionTemplate primaryTransactionTemplate(@Qualifier("transactionManager") DataSourceTransactionManager transactionManager){
        
        return new TransactionTemplate(transactionManager);
    }
    /**
     * @desc: 读取mybatis的配置文件
     * @param factory
     */
    private void setConfigLocation(SqlSessionFactoryBean factory){
        
        String configLocation = env.getProperty("mybatis.configLocation");
        logger.info("默认数据源读取MyBatis的配置文件{}", configLocation);
        
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        if(!StringUtils.isEmpty(configLocation)){
            factory.setConfigLocation(resourceResolver.getResource(configLocation));
        }else{
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            factory.setConfiguration(configuration);
        }
    }
    
    /**
     * @desc: 根据配置文件获取Mybatis的mapper文件
     * @return
     */
    private Resource[] getMapper() {
    
        String mapperLocations = env.getProperty("mybatis.mapperLocations");
        logger.info("默认数据源读取MyBatis mapper的配置文件{}", mapperLocations);
        
        List<Resource> resources = new ArrayList<Resource>();
        if (!StringUtils.isEmpty(mapperLocations)) {
            ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
            try {
                Resource[] mappers = resourceResolver.getResources(mapperLocations);
                resources.addAll(Arrays.asList(mappers));
            } catch (IOException e) {
                // ignore
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }
    
}


 /**
 * @Title: DataSourceUtil.java 
 * @Package:com.lishiliang.framework.sharding.util
 * @desc: TODO  
 * @author: lisl    
 * @date:2019年9月6日 下午4:35:53    
 */
 
package com.lishiliang.util;

import java.util.Properties;

import javax.sql.DataSource;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import com.lishiliang.db.datasource.ShardingDataSourceAutoConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.p6spy.engine.spy.P6DataSource;

 /**
  * @author lisl
  * @desc 数据源工具
  */
 public class DataSourceUtil {
 
     private static final Logger logger = LoggerFactory.getLogger(ShardingDataSourceAutoConfiguration.class);
     
     /**
      * @author lisl
      * @desc 创建带监控的数据源
      * @param env
      * @param databaseName
      * @param type
      * @return
      */
     public static DataSource createSpyDataSource(Environment env, String databaseName, String type) {
         
         return new P6DataSource(createDruidDataSource(env, databaseName, type));
     }
     /**
      * @author lisl
      * @desc 创建数据源
      * @param env 配置文件信息
      * @param databaseName 数据库
      * @param type 数据源类型：primary，slave
      * @return
      */
     private static DataSource createDruidDataSource(Environment env, String databaseName, String type) {
         
         Properties dataSourceProperties =new Properties();
         dataSourceProperties.put(DruidDataSourceFactory.PROP_INITIALSIZE, env.getProperty("spring.datasource.druid.sharding."+ type +".initial-size"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_MAXACTIVE, env.getProperty("spring.datasource.druid.sharding." + type + ".max-active"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_MAXWAIT, env.getProperty("spring.datasource.druid.sharding."+ type +".max-wait"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, env.getProperty("spring.datasource.druid.sharding."+ type +".time-between-eviction-runs-millis"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, env.getProperty("spring.datasource.druid.sharding."+ type +".validation-query"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, env.getProperty("spring.datasource.druid.sharding."+ type +".test-while-idle"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_TESTONBORROW, env.getProperty("spring.datasource.druid.sharding."+ type +".test-on-borrow"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_TESTONRETURN, env.getProperty("spring.datasource.druid.sharding." + type + ".test-on-return"));  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, env.getProperty("spring.datasource.druid.sharding."+ type +".driver-class-name"));
         dataSourceProperties.put(DruidDataSourceFactory.PROP_POOLPREPAREDSTATEMENTS, "true");  
         dataSourceProperties.put("maxPoolPreparedStatementPerConnectionSize", "20");  
         dataSourceProperties.put(DruidDataSourceFactory.PROP_FILTERS, env.getProperty("spring.datasource.druid.sharding."+ type +".filters"));
         dataSourceProperties.put(DruidDataSourceFactory.PROP_CONNECTIONPROPERTIES, env.getProperty("spring.datasource.druid.sharding."+ type +".connection-properties"));
         
         String url = env.getProperty("spring.datasource.druid.sharding."+ type +".url");
         dataSourceProperties.put(DruidDataSourceFactory.PROP_URL, url + "/"+ databaseName +"?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai");
         dataSourceProperties.put(DruidDataSourceFactory.PROP_USERNAME, env.getProperty("spring.datasource.druid.sharding."+ type +".username"));
         dataSourceProperties.put(DruidDataSourceFactory.PROP_PASSWORD, env.getProperty("spring.datasource.druid.sharding."+ type +".password"));
         
         if(StringUtils.isNotBlank(env.getProperty("spring.datasource.druid.sharding."+ type +".remove-abandoned"))) {
             dataSourceProperties.put(DruidDataSourceFactory.PROP_REMOVEABANDONED, env.getProperty("spring.datasource.druid.sharding."+ type +".remove-abandoned"));
             dataSourceProperties.put(DruidDataSourceFactory.PROP_REMOVEABANDONEDTIMEOUT, env.getProperty("spring.datasource.druid.sharding."+ type +".remove-abandoned-timeout"));
             dataSourceProperties.put(DruidDataSourceFactory.PROP_LOGABANDONED, env.getProperty("spring.datasource.druid.sharding."+ type +".log-abandoned"));
         }
         
         try {
             return DruidDataSourceFactory.createDataSource(dataSourceProperties);
         } catch (Exception e) {
             
             logger.info("创建Sharding数据源失败！失败原因：" + e.getMessage(), e);
             throw new BusinessRuntimeException(ErrorCodes.DATASOURCE_CREATE_ERROR.getCode(), ErrorCodes.DATASOURCE_CREATE_ERROR.getDesc());
         }
     }
 }

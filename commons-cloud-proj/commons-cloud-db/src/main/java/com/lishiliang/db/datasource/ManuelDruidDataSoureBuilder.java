
 /**
 * @Title: ManuelDruidDataSoureConfiguration.java 
 * @Package:com.lishiliang.dubbo.db.datasource
 * @desc: TODO  
 * @author: lisl
 * @date
 */
 
package com.lishiliang.db.datasource;

import java.util.Map;

import javax.sql.DataSource;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;


public class ManuelDruidDataSoureBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ManuelDruidDataSoureBuilder.class);
    
    private ManuelDruidDataSoureBuilder(){}
    
    /**
     * @desc: 获取对象
     * @return
     */
    public static ManuelDruidDataSoureBuilder create(){
        return new ManuelDruidDataSoureBuilder();
    }
    
    /**
     * @desc: 人工创建一个数据库模板
     * @param dataSourceProperties
     * @return
     */
    public JdbcTemplate build(Map<String, Object> dataSourceProperties){
        
        DataSource dataSource = manuelCreateDruidDataSource(dataSourceProperties);
        return manuelCreateJdbcTemplate(dataSource);
    }
    
    /**
     * @desc: 人工创建数据源
     * @param dataSourceProperties
     * @return
     */
    public DataSource manuelCreateDruidDataSource(Map<String, Object> dataSourceProperties){
        try {
            DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(dataSourceProperties);
            dataSource.setBreakAfterAcquireFailure(true);
            dataSource.init();
            
            return dataSource;
        } catch (Exception e) {
            
            logger.error("人工创建数据源失败！" + e.getMessage(), e);
            throw new BusinessRuntimeException(ErrorCodes.DATASOURCE_CREATE_ERROR.getCode(), ErrorCodes.DATASOURCE_CREATE_ERROR.getDesc());
        }
    }
    
    /**
     * @desc: 创建数据库模板
     * @param dataSource
     * @return
     */
    public JdbcTemplate manuelCreateJdbcTemplate(DataSource dataSource){
        
        return new JdbcTemplate(dataSource);
    }
}

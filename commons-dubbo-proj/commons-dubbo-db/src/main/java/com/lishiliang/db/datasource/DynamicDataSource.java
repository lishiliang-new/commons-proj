package com.lishiliang.db.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

/**
 * 多数据源路适配
 * 多数据源路适配,由配置中多个数据源组成，动态按包或自确定注解数据源进行切换
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> dataSourceTypes = new ThreadLocal<String>(){
        @Override
        protected String initialValue(){
            return DynamicDataSource.defaultDataSourceKey;
        }
    };

    //默认的数据源
    public static String defaultDataSourceKey;
    //保存所有可用的数据源
    public static Map<Object, Object> targetDataSources;

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceTypeManager.getType();
    }

    public void setDefaultDataSourceKey(String defaultDataSourceKey) {
        DynamicDataSource.defaultDataSourceKey = defaultDataSourceKey;
    }

    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        DynamicDataSource.targetDataSources = targetDataSources;
    }
}
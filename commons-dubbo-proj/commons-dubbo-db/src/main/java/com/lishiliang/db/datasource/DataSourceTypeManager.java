package com.lishiliang.db.datasource;

/**
 *
 */
public class DataSourceTypeManager {

    private static final ThreadLocal<String> dataSourceType = new ThreadLocal<String>(){
        @Override
        protected String initialValue(){
        return DynamicDataSource.defaultDataSourceKey;
        }
    };

    public static String getType(){
        return dataSourceType.get();
    }

    public static void setType(String type){
        dataSourceType.set(type);
    }

    public static void reset(){
        dataSourceType.set(DynamicDataSource.defaultDataSourceKey);
    }
}
package com.lishiliang;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.p6spy.engine.spy.P6DataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lisl
 * @version 1.0
 */
public class DataSourceUtils {

    private static DataSource dataSource;

    //初始化连接池
    public static DataSource initDataSource (String source){
        try {
            Map<String, String> dataSourceMap = getDataSourceMap(source);
            dataSourceMap.put("url", "jdbc:mysql://192.168.70.153?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true");
            //dataSourceMap.put("driverClassName", "com.mysql.cj.jdbc.Driver");
            dataSourceMap.put("connectionProperties", "config.decrypt=true;config.decrypt.key=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCFOfhbHiUN+jwWMkP00kiMeRS994V2LnKQjf0mcNi0W6RthmZnM8SWHCs6sQPc8Z+bLg6e+tXiUrmK8z/jrZO5w9PZEPskstt6uLVrG0PRLpEWgcbm4Tx2zx97dbIAqhAS0PKOc7E1CM58bRslNoD2CL+/cnhepBkHpg5A0jqt5wIDAQAB;");
            dataSource = new P6DataSource(DruidDataSourceFactory.createDataSource(dataSourceMap));
        } catch (Exception e) {

        }
        return dataSource;
    }

    public static Map<String, String> getConfigMap (){
        Properties properties = new Properties();
        try {
            properties.load(DataSourceUtils.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {

        }
        return (Map) properties;
    }


    public static Map<String, String> getDataSourceMap (String source){
        Map<String, String> configMap = getConfigMap();
        Map<String, String> dataSourceMap = new HashMap<>();
        configMap.forEach((k, v)->{
            String newKey = k.substring(k.lastIndexOf(".") + 1);
            if ((k.indexOf("slave")  != -1 || k.indexOf("primary") != -1)) {
                if (k.indexOf(source) != -1) {
                    dataSourceMap.put(lineToHump(newKey), v);
                }
            } else {
                //dataSourceMap.put(lineToHump(newKey), v);
            }
        });
        return dataSourceMap;
    }
    /**
     * @desc: -线转驼峰
     * @param str
     * @return
     */
    public static String lineToHump(String str) {
        Pattern linePattern = Pattern.compile("-(\\w)");
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}

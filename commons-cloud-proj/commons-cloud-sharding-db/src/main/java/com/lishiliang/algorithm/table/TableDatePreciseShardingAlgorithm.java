
 /**
 * @Title: DateShardingAlgorithm.java
 * @author: lisl
 * @date:2019年8月22日 上午9:19:01    
 */

 package com.lishiliang.algorithm.table;

 import com.alibaba.fastjson.JSON;
 import com.lishiliang.core.utils.Constant;
 import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
 import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import java.util.Collection;

/**
 * @desc 按日期进行分表策略
 */
public class TableDatePreciseShardingAlgorithm implements PreciseShardingAlgorithm<String> {

    private static final Logger logger = LoggerFactory.getLogger(TableDatePreciseShardingAlgorithm.class);
    
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        
        logger.info("按日期分表策略：availableTargetNames={}, shardingValue={}", JSON.toJSONString(availableTargetNames), JSON.toJSONString(shardingValue));
        
        String dateTimeFormat = Constant.DATE_TIME_YYYYMMDDHHMMSS;
        if(shardingValue.getValue().length() == 8) {
            dateTimeFormat = Constant.DATE_TIME_YYYYMMDD;
        }else if(shardingValue.getValue().length() == 10) {
            dateTimeFormat = Constant.DATE_TIME_YYYY_MM_DD;
        }
        
        //日期格式：yyyyMMddHHmmss
        DateTime shardingDate = DateTime.parse(shardingValue.getValue(), DateTimeFormat.forPattern(dateTimeFormat));
        
        for(String availableTargetName : availableTargetNames) {
            if(availableTargetName.endsWith(shardingDate.toString(Constant.DATE_TIME_MM))) {
                
                logger.info("按日期分表策略：最终执行的数据库表：{}", availableTargetName);
                return availableTargetName;
            }
        }
        
        //默认表
        StringBuffer tableName = new StringBuffer();
        tableName.append(availableTargetNames.toArray()[0]);

        logger.info("按日期分表策略：最终执行的数据库表：{}", tableName);
        return tableName.toString();
    }

}

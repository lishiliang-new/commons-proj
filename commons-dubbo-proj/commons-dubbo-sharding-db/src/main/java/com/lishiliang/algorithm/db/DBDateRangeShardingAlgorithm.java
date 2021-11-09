
 /**
 * @Title: DateRangeShardingAlgorithm.java 
 * @Package:com.lishiliang.sharding.algorithm
 * @desc: TODO  
 * @author: lisl
 * @date
 */

 package com.lishiliang.algorithm.db;

 import com.alibaba.fastjson.JSON;
 import com.lishiliang.core.utils.Constant;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
 import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import java.util.Collection;
 import java.util.LinkedHashSet;

/**
 * @desc 按日期进行分表策略（范围查询时策略）
 */
public class DBDateRangeShardingAlgorithm implements RangeShardingAlgorithm<String> {

    private static final Logger logger = LoggerFactory.getLogger(DBDateRangeShardingAlgorithm.class);
    
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
        
        logger.info("按日期分库策略：availableTargetNames={}, shardingValue={}", JSON.toJSONString(availableTargetNames), JSON.toJSONString(shardingValue));
        
        Collection<String> result = new LinkedHashSet<String>();
        
        String dateTimeFormat = Constant.DATE_TIME_YYYYMMDDHHMMSS;
        if(shardingValue.getValueRange().upperEndpoint().length() == 8) {
            dateTimeFormat = Constant.DATE_TIME_YYYYMMDD;
        }else if(shardingValue.getValueRange().upperEndpoint().length() == 10) {
            dateTimeFormat = Constant.DATE_TIME_YYYY_MM_DD;
        }
        
        //日期格式：yyyyMMddHHmmss
        DateTime shardingUpperDate = DateTime.parse(shardingValue.getValueRange().upperEndpoint(), DateTimeFormat.forPattern(dateTimeFormat));
        DateTime shardingLowerDate = DateTime.parse(shardingValue.getValueRange().lowerEndpoint(), DateTimeFormat.forPattern(dateTimeFormat));
        
        DateTime shardingUpperMonth = shardingUpperDate.withDayOfMonth(1);
        DateTime shardingLowerMonth = shardingLowerDate.withDayOfMonth(1);
        
        while(true) {
            if(NumberUtils.toInt(shardingLowerMonth.toString(Constant.DATE_TIME_YYYY)) <=
                    NumberUtils.toInt(shardingUpperMonth.toString(Constant.DATE_TIME_YYYY))) {
                
                for(String availableTargetName : availableTargetNames) {
                    if(availableTargetName.endsWith(shardingLowerMonth.toString(Constant.DATE_TIME_YYYY))) {
                        
                        result.add(availableTargetName.toString());
                    }
                }
                
                shardingLowerMonth = shardingLowerMonth.plusYears(1);
            }else {
                break;
            }
        }
        
        logger.info("按日期分库策略：最终执行的数据库：{}", StringUtils.join(result, ","));
        return result;
    }
}

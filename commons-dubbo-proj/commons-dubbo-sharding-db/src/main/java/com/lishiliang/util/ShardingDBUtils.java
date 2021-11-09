
 /**
 * @Title: ShardingDBUtils.java 
 * @Package:com.lishiliang.sharding.util
 * @desc: TODO  
 * @author: lisl    
 * @date
 */
 
package com.lishiliang.util;

import java.util.List;
import java.util.Set;

import com.lishiliang.core.utils.Constant;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


 /**
  * @author lisl
  * @desc 手动分库分表计算分库分表工具
  */
 public class ShardingDBUtils {
 

     /**
      * @author lisl
      * @return
      */
     public static List<String> generateShardingTables(String beginDate, String endDate) {

         String dbName = "db_";
         String tableName = "table_";
         
         return gernateShardingDatabaseTables(dbName, tableName, beginDate, endDate);
     }
 

     
     /**
      * @author lisl1
      * @desc 使用shardingJDBC无法实现复杂SQL，因此自行分库分表
      * @param dbName 需要分库的库名，不带分片字段
      * @param tableName 需要分表的表名，不带分片字段
      * @param beginDate 分库分表日期yyyyMMdd
      * @param endDate 分库分表日期yyyyMMdd
      * @return 库表列表
      */
     public static List<String> gernateShardingDatabaseTables(String dbName, String tableName, String beginDate, String endDate) {
         
         DateTime beginDateTime = DateTime.parse(beginDate, DateTimeFormat.forPattern(Constant.DATE_TIME_YYYYMMDD));
         DateTime endDateTime = DateTime.parse(endDate, DateTimeFormat.forPattern(Constant.DATE_TIME_YYYYMMDD));
         
         DateTime beginDateMonth = beginDateTime.withDayOfMonth(1);
         DateTime endDateMonth = endDateTime.withDayOfMonth(1);
         
         List<String> dbTableNames = Lists.newArrayList();
         while(NumberUtils.toInt(beginDateMonth.toString(Constant.DATE_TIME_YYYYMM)) 
             <= NumberUtils.toInt(endDateMonth.toString(Constant.DATE_TIME_YYYYMM))) {
             
             dbTableNames.add(dbName + "_" + beginDateMonth.toString(Constant.DATE_TIME_YYYY) + "." + tableName + "_" + beginDateMonth.toString(Constant.DATE_TIME_MM));
             
             beginDateMonth = beginDateMonth.plusMonths(1);
         }
         
         return dbTableNames;
     }
     

     

     
     public static void main(String[] args) {
         
         System.out.println(ShardingDBUtils.gernateShardingDatabaseTables("db_", "table_", "20200109", "20200709"));
     }
 }

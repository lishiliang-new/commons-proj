package com.lishiliang;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lishiliang.algorithm.db.DBDatePreciseShardingAlgorithm;
import com.lishiliang.algorithm.db.DBDateRangeShardingAlgorithm;
import com.lishiliang.algorithm.table.TableDatePreciseShardingAlgorithm;
import com.lishiliang.algorithm.table.TableDateRangeShardingAlgorithm;
import com.lishiliang.db.dao.BaseDao;
import com.p6spy.engine.spy.P6DataSource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * @author
 * @version 1.0
 */
public class ShardingDataSourceUtils {

    private Map<String, String> configMap =  new HashMap<>();

    public static ShardingDataSourceUtils build () {
        return new ShardingDataSourceUtils();
    }
    /**
     * 收单库分表规则
     */
    private static final String SHARDING_TABLE_RULE_POS = "shardingsphere.sharding.pos.tables.rule";
    /**
     * 互支库分表规则
     */
    private static final String SHARDING_TABLE_RULE_CXPAY = "shardingsphere.sharding.cxpay.tables.rule";
    /**
     * 商户渠道数据统计分库分表规则
     */
    private static final String SHARDING_TABLE_RULE_REPORT_MCH = "shardingsphere.sharding.report.mch.tables.rule";
    /**
     * 分库开始年
     */
    private static final String SHARING_TABLE_YEAR_START = "shardingsphere.sharding.tables.actual.year.start";
    /**
     * 分库截止年
     */
    private static final String SHARING_TABLE_YEAR_END = "shardingsphere.sharding.tables.actual.year.end";

    private static final String SHOW_SQL = "shardingsphere.show.sql";
    private static final String QUERY_SIZE = "max.connections.size.per.query";

    private static final String PRIMARY = "primary";
    private static final String SLAVE = "slave";
    private static final Integer DEFAULT_TABLE_YEAR_START = 2010;
    private static final Integer DEFAULT_TABLE_YEAR_END = 2099;

    private static final int START_YEAR = 2018;
    private static final int END_YEAR = 2021;

    /**
     * 收单交易流水分片键
     */
    private static final String SHARDING_POS_COL = "TRADE_DATE_TIME";


    //初始化 shardingJdbcTemplate 属性
    public void initShardingBaseDaoFields(BaseDao... currentDaos) throws SQLException {
        DataSource dataSource = ShardingDataSourceUtils.build().getShardingDataSouce();
        JdbcTemplate shardingJdbcTemplate = new JdbcTemplate(new P6DataSource(dataSource));
        for (BaseDao currentDao : currentDaos) {
            ReflectionTestUtils.setField(currentDao, "shardingJdbcTemplate", shardingJdbcTemplate);
        }
    }


    public DataSource getShardingDataSouce () throws SQLException {

        configMap = DataSourceUtils.getConfigMap();
        String shardingPosTables =  configMap.get("shardingsphere.sharding.pos.tables.rule");
        String shardingCxpayTables =  configMap.get("shardingsphere.sharding.cxpay.tables.rule");
        String shardingReportMchTables =  configMap.get("shardingsphere.sharding.report.mch.tables.rule");

        //数据源
        Map<String, DataSource> allDataSourceMap = Maps.newHashMap();
        String shardingTables = configMap.get(SHARDING_TABLE_RULE_POS);

        allDataSourceMap.putAll(createDataSourceMap(configMap.get(SHARDING_TABLE_RULE_POS)));
        allDataSourceMap.putAll(createDataSourceMap(configMap.get(SHARDING_TABLE_RULE_CXPAY)));
        allDataSourceMap.putAll(createDataSourceMap(configMap.get(SHARDING_TABLE_RULE_REPORT_MCH)));


        //分表分库规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().addAll(getTableRuleConfiguration("2018", "2020", configMap.get(SHARDING_TABLE_RULE_POS)));
        shardingRuleConfig.getTableRuleConfigs().addAll(getTableRuleConfiguration("2018", "2020", configMap.get(SHARDING_TABLE_RULE_CXPAY)));
        shardingRuleConfig.getTableRuleConfigs().addAll(getTableRuleConfiguration("2018", "2020", configMap.get(SHARDING_TABLE_RULE_REPORT_MCH)));


        //绑定表关系
        List<String> posBindingTables = getBindingTables(shardingPosTables);
        if(!posBindingTables.isEmpty()) {

            shardingRuleConfig.getBindingTableGroups().add(StringUtils.join(posBindingTables, ","));
        }
        List<String> cxpayBindingTables = getBindingTables(shardingCxpayTables);
        if(!cxpayBindingTables.isEmpty()) {
            shardingRuleConfig.getBindingTableGroups().add(StringUtils.join(cxpayBindingTables, ","));
        }

        //默认分表策略
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(SHARDING_POS_COL, new TableDatePreciseShardingAlgorithm(), new TableDateRangeShardingAlgorithm()));
        //主从
        shardingRuleConfig.setMasterSlaveRuleConfigs(getMasterSlaveRuleConfigurations());

        Properties properties = new Properties();
        properties.put("sql.show", BooleanUtils.toBoolean( configMap.get(SHOW_SQL)));
        properties.put(QUERY_SIZE, NumberUtils.toInt( configMap.get(QUERY_SIZE), 20));

        return ShardingDataSourceFactory.createDataSource(allDataSourceMap, shardingRuleConfig, properties);
    }

    private Map<String, DataSource> createDataSourceMap(String shardingTables){
        Set<String> shardingDatabaseSet = new HashSet<>();
        if(StringUtils.isNotBlank(shardingTables)) {
            String [] tables = shardingTables.split(",");
            for(String table : tables) {
                String [] tableColumn = table.split("\\.");
                shardingDatabaseSet.add(tableColumn[0]);
            }
        }
        Map<String, DataSource> result = new HashMap<>();
        //创建主数据源
        result.putAll(createDataSourceMap(shardingDatabaseSet, "primary"));
        result.putAll(createDataSourceMap(shardingDatabaseSet, "slave"));
        return result;
    }
    private Map<String, DataSource> createDataSourceMap(Set<String> shardingDatabaseSet, String type){
        int startYear = START_YEAR; //--
        int endYear = END_YEAR; //--
        Map<String, DataSource> result = new HashMap<>();
        for(String databaseName : shardingDatabaseSet) {
            for(int i=startYear; i<= endYear; i++) {
                Map<String, String> dataSourceMap = DataSourceUtils.getDataSourceMap("druid.slave");
                jdbc:mysql://192.168.70.153/db_trade_settle_2018?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai
                dataSourceMap.put("url", "jdbc:mysql://192.168.70.153/"+ databaseName +"_" + i + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai");
                // todo
                dataSourceMap.put("password", "password");
                dataSourceMap.remove("connectionProperties");
                try {
                    result.put(databaseName + "_" + type + "_" + i, DruidDataSourceFactory.createDataSource(dataSourceMap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    private List<TableRuleConfiguration> getTableRuleConfiguration(String startYear, String endYear, String shardingTables){
        if(StringUtils.isNotBlank(shardingTables)) {
            String [] tables = shardingTables.split(",");
            List<TableRuleConfiguration> tableRuleConfigList = new ArrayList<>(tables.length);
            for(String table : tables) {
                String [] tableColumn = table.split("\\.");
                //为什么必须要加此参数才能使用bind特性：https://github.com/apache/incubator-shardingsphere/issues/2645
                String actualDataNodes = "" + tableColumn[0] + "_${"+ startYear + ".."+ endYear +"}." + tableColumn[1] + "_0${1..9}," + "" + tableColumn[0] + "_${"+ startYear +".."+ endYear +"}." + tableColumn[1] + "_${10..12}";
                TableRuleConfiguration configuration = new TableRuleConfiguration(tableColumn[1], actualDataNodes);

                configuration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration(tableColumn[2], new DBDatePreciseShardingAlgorithm(), new DBDateRangeShardingAlgorithm()));//设置分库策略
                configuration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(tableColumn[2], new TableDatePreciseShardingAlgorithm(), new TableDateRangeShardingAlgorithm()));
                tableRuleConfigList.add(configuration);
            }
            return tableRuleConfigList;
        }
        return Lists.newArrayList();
    }
    private List<String> getBindingTables(String shardingTables){
        List<String> bindingTableList = Lists.newArrayList();
        if(StringUtils.isNotBlank(shardingTables)) {
            String [] tables = shardingTables.split(",");
            for(String table : tables) {
                String [] tableColumn = table.split("\\.");
                bindingTableList.add(tableColumn[1]);
            }
        }
        return bindingTableList;
    }
    /**
     * @desc 主从规则
     * @return
     */
    private Collection<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        List<MasterSlaveRuleConfiguration> ruleList = Lists.newArrayList();
        ruleList.addAll(getMasterSlaveRuleConfigurations(configMap.get(SHARDING_TABLE_RULE_POS)));
        ruleList.addAll(getMasterSlaveRuleConfigurations(configMap.get(SHARDING_TABLE_RULE_CXPAY)));
        ruleList.addAll(getMasterSlaveRuleConfigurations(configMap.get(SHARDING_TABLE_RULE_REPORT_MCH)));
        return ruleList;
    }
    private Collection<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations(String shardingTables){
        int startYear = START_YEAR;
        int endYear = END_YEAR;
        List<MasterSlaveRuleConfiguration> ruleList = Lists.newArrayList();
        if(StringUtils.isNotBlank(shardingTables)) {
            String [] tables = shardingTables.split(",");
            Set<String> dabaseSet = new HashSet<>();
            for(String table : tables) {
                String [] tableColumn = table.split("\\.");
                String databaseName = tableColumn[0];
                dabaseSet.add(databaseName);
            }
            for(String databaseName : dabaseSet) {
                for(int i=startYear; i<= endYear; i++) {
                    MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration(databaseName + "_" + i, databaseName + "_" + PRIMARY + "_" + i, Arrays.asList(databaseName + "_" + SLAVE + "_" + i));
                    ruleList.add(masterSlaveRuleConfig);
                }
            }
        }
        return ruleList;
    }

}

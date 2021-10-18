package com.lishiliang.db.datasource;

import com.github.pagehelper.PageInterceptor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lishiliang.algorithm.db.DBDatePreciseShardingAlgorithm;
import com.lishiliang.algorithm.db.DBDateRangeShardingAlgorithm;
import com.lishiliang.algorithm.table.TableDatePreciseShardingAlgorithm;
import com.lishiliang.algorithm.table.TableDateRangeShardingAlgorithm;
import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import com.lishiliang.util.DataSourceUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


/**
 * @author lisl
 * @desc 分库分表数据源
 */
@Configuration
@ConditionalOnClass(value=org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource.class)
public class ShardingDataSourceAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ShardingDataSourceAutoConfiguration.class);

    @Autowired
    private Environment env;

    /**
     * 订单库分表规则
     * #分表规则，表.字段,表.字段,表.字段，如：数据库表T_TRADE_BASE使用字段TRADE_DATE进行分表，T_TRADE_EXTEND使用字段TRADE_DATE进行分表，
     * #则填写为：T_TRADE_BASE.TRADE_DATE,T_TRADE_EXTEND.TRADE_DATE
     * shardingsphere.sharding.order.tables.rule=db_trade.T_TRADE_BASE.TRADE_DATE_TIME,db_trade.T_TRADE_EXTEND.TRADE_DATE_TIME
     */
    private static final String SHARDING_TABLE_RULE_ORDER = "shardingsphere.sharding.order.tables.rule";
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

    /**
     * 分片键
     */
    private static final String SHARDING_ORDER_COL = "TRADE_DATE_TIME";

    /**
     * @desc 订单表分片规则
     * @return
     */
    @Bean(name = "orderTableRuleConfiguration")
    public List<TableRuleConfiguration> getOrderTableRuleConfiguration() {
        String shardingTables = env.getProperty(SHARDING_TABLE_RULE_ORDER);
        String startYear = env.getProperty(SHARING_TABLE_YEAR_START, DEFAULT_TABLE_YEAR_START.toString());
        String endYear = env.getProperty(SHARING_TABLE_YEAR_END, DEFAULT_TABLE_YEAR_END.toString());

        return getTableRuleConfiguration(startYear, endYear, shardingTables);
    }

    
    /**
     * @desc: 主库
     * @return
     * @throws SQLException
     */
    @Bean(name = "shardingDataSource")
    @Qualifier("shardingDataSource")
    public DataSource shardingDataSource(@Qualifier("orderTableRuleConfiguration") List<TableRuleConfiguration> orderTableRuleConfiguration,
                                         @Qualifier("orderDataSourceMap") Map<String, DataSource> orderDataSourceMap
    ) throws SQLException {

        logger.info("Sharding分库分表数据源创建主库数据源");
        String shardingOrderTables = env.getProperty(SHARDING_TABLE_RULE_ORDER);
      

        //数据源
        Map<String, DataSource> allDataSourceMap = Maps.newHashMap();
        allDataSourceMap.putAll(orderDataSourceMap);
       

        if(allDataSourceMap.isEmpty()) {

            logger.error("分库分表数据源为空，请检查配置项！");
            throw new BusinessRuntimeException(ErrorCodes.DATASOURCE_CREATE_ERROR.getCode(), ErrorCodes.DATASOURCE_CREATE_ERROR.getDesc());
        }

        //分表分库规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().addAll(orderTableRuleConfiguration);
      

        //绑定表关系
        List<String> orderBindingTables = getBindingTables(shardingOrderTables);
        if(!orderBindingTables.isEmpty()) {

            shardingRuleConfig.getBindingTableGroups().add(StringUtils.join(orderBindingTables, ","));
        }
       
        
        //默认分表策略
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(SHARDING_ORDER_COL, new TableDatePreciseShardingAlgorithm(), new TableDateRangeShardingAlgorithm()));
        //shardingRuleConfig.setTableRuleConfigs()

        //主从
        shardingRuleConfig.setMasterSlaveRuleConfigs(getMasterSlaveRuleConfigurations());

        Properties properties = new Properties();
        properties.put("sql.show", BooleanUtils.toBoolean(env.getProperty(SHOW_SQL)));
        properties.put(QUERY_SIZE, NumberUtils.toInt(env.getProperty(QUERY_SIZE), 20));

        return ShardingDataSourceFactory.createDataSource(allDataSourceMap, shardingRuleConfig, properties);
    }



    /**
     * @desc 数据源
     * @param primaryDataSource
     * @param slaveDataSource
     * @return
     */
    @Bean(name = "orderDataSourceMap")
    public Map<String, DataSource> createOrderDataSourceMap() {

        String shardingTables = env.getProperty(SHARDING_TABLE_RULE_ORDER);

        return createDataSourceMap(shardingTables);
    }


    /**
     * @desc: 主库
     * @param dataSource
     * @return
     */
    @Bean(name = "shardingJdbcTemplate")
    public JdbcTemplate shardingJdbcTemplate(@Qualifier("shardingDataSource") DataSource dataSource) {

        logger.info("Sharding数据源创建主库JDBC模板");

        return new JdbcTemplate(dataSource);
    }
    /**
     * @desc: 主库事务
     * @param dataSource
     * @return
     */
    @Bean(name= "shardingTransactionManager")
    public DataSourceTransactionManager shardingTransactionManager(@Qualifier("shardingDataSource") DataSource dataSource){
        logger.info("Sharding数据源创建主库事务管理器");

        return new DataSourceTransactionManager(dataSource);
    }
    /**
     * @desc: 事务模板
     * @param transactionManager
     * @return
     */
    @Bean(name = "shardingTransactionTemplate")
    public TransactionTemplate shardingTransactionTemplate(@Qualifier("shardingTransactionManager") DataSourceTransactionManager transactionManager){

        return new TransactionTemplate(transactionManager);
    }

    /**
     * @desc: 主库的Mybatis
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean(name = "shardingSqlSessionFactory")
    public SqlSessionFactory shardingSqlSessionFactory(@Qualifier("shardingDataSource") DataSource dataSource) throws Exception {

        logger.info("Sharding数据源创建主库连接工厂");

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        factory.setMapperLocations(getMapper());

        //配置分页插件
        PageInterceptor pageInterceptor = pageInterceptor();
        factory.setPlugins(new Interceptor[]{pageInterceptor});

        setConfigLocation(factory);
        return factory.getObject();
    }

    /**
     * @desc: 主库
     * @param sqlSessionFactory
     * @return
     */
    @Bean(name="shardingSqlSessionTemplate")
    public SqlSessionTemplate shardingSqlSessionTemplate(@Qualifier("shardingSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * @desc: 注册分页插件
     * @return
     */
    @Bean(name="shardingPageInterceptor")
    public PageInterceptor pageInterceptor(){
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        pageInterceptor.setProperties(properties);

        return pageInterceptor;
    }

    /**
     * @author lisl
     * @desc 获取分片规则
     * @param shardingTables
     * @return
     */
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

    /**
     * @author lisl
     * @desc 获取表关联关系
     * @param shardingTables
     * @return
     */
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
     * @author lisl
     * @desc 创建数据源
     * @param shardingTables
     * @return
     */
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
        result.putAll(createDataSourceMap(shardingDatabaseSet, PRIMARY));
        result.putAll(createDataSourceMap(shardingDatabaseSet, SLAVE));

        return result;
    }

    /**
     * @author lisl
     * @desc 创建数据源
     * @param shardingDatabaseSet
     * @param type
     * @return
     */
    private Map<String, DataSource> createDataSourceMap(Set<String> shardingDatabaseSet, String type){

        int startYear = env.getProperty(SHARING_TABLE_YEAR_START, Integer.class, DEFAULT_TABLE_YEAR_START);
        int endYear = env.getProperty(SHARING_TABLE_YEAR_END, Integer.class, DEFAULT_TABLE_YEAR_END);

        Map<String, DataSource> result = new HashMap<>();
        for(String databaseName : shardingDatabaseSet) {

            for(int i=startYear; i<= endYear; i++) {

                result.put(databaseName + "_" + type + "_" + i, DataSourceUtil.createSpyDataSource(env, databaseName + "_" + i, type));
            }
        }

        return result;
    }

    /**
     * @desc 主从规则
     * @return
     */
    private Collection<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {

        List<MasterSlaveRuleConfiguration> ruleList = Lists.newArrayList();

        ruleList.addAll(getMasterSlaveRuleConfigurations(env.getProperty(SHARDING_TABLE_RULE_ORDER)));

        return ruleList;
    }

    /**
     * @author lisl
     * @desc 获取互支主从配置
     * @return
     */
    private Collection<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations(String shardingTables){

        int startYear = env.getProperty(SHARING_TABLE_YEAR_START, Integer.class, DEFAULT_TABLE_YEAR_START);
        int endYear = env.getProperty(SHARING_TABLE_YEAR_END, Integer.class, DEFAULT_TABLE_YEAR_END);

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

    /**
     * @desc: 读取mybatis的配置文件
     * @param factory
     */
    private void setConfigLocation(SqlSessionFactoryBean factory){

        String configLocation = env.getProperty("mybatis.configLocation");
        logger.info("Sharding数据源读取MyBatis的配置文件{}", configLocation);

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
        logger.info("Sharding数据源读取MyBatis mapper的配置文件{}", mapperLocations);

        List<Resource> resources = new ArrayList<>();
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
    
    public static void main(String[] args) {

    }
}

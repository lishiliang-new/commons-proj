
package com.lishiliang;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.github.pagehelper.PageInterceptor;
import com.lishiliang.db.dao.BaseDao;
import com.lishiliang.model.DataModel;
import com.p6spy.engine.spy.P6DataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author lisl
 * @version 1.0
 */
//@RunWith(MockitoJUnitRunner.class)
public class BaseMockitoTest {

    protected DataSource primaryDataSource;
    protected DataSource slaveDataSource;

    protected JdbcTemplate primaryJdbcTemplate;
    protected JdbcTemplate slaveJdbcTemplate;

    protected DataSource shardingDataSource;
    protected JdbcTemplate shardingJdbcTemplate;

    protected SqlSessionTemplate primarySqlSessionTemplate;
    protected SqlSessionTemplate slaveSqlSessionTemplate;

    protected TransactionTemplate transactionTemplate;

    private ProxyFactory proxyFactory;

//    @Spy
//    protected RestTemplate restTemplate;

    /**
     * 加载 logback配置
     */
    static {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            String rootPath = ClassLoader.getSystemClassLoader().getResource("").getPath().replace("test-classes", "classes");
            configurator.doConfigure(rootPath + "/logback.xml");
        } catch (JoranException e) {

        }
    }

    private StopWatch watch = new StopWatch();

    //初始化基本的连接池 ,JdbcTemplate
    public void init() {

        MockitoAnnotations.initMocks(this);
        watch.start();

        primaryDataSource = primaryDataSource == null ? DataSourceUtils.initDataSource("druid.primary") : primaryDataSource;
        primaryJdbcTemplate = Mockito.spy(new JdbcTemplate(primaryDataSource));
        slaveDataSource = slaveDataSource == null ? DataSourceUtils.initDataSource("druid.slave") : slaveDataSource;
        slaveJdbcTemplate = Mockito.spy(new JdbcTemplate(slaveDataSource));

        primarySqlSessionTemplate = Mockito.spy(new SqlSessionTemplate(createSqlSessionFactory(new P6DataSource(primaryDataSource))));
        slaveSqlSessionTemplate = Mockito.spy(new SqlSessionTemplate(createSqlSessionFactory(new P6DataSource(slaveDataSource))));

        transactionTemplate = Mockito.spy(createTransactionTemplate(primaryDataSource));
    }



    //初始化dao层JdbcTemplate属性
    public void initDaoFields(BaseDao... currentDaos) {

        for (BaseDao currentDao : currentDaos) {
            ReflectionTestUtils.setField(currentDao, "primaryJdbcTemplate", primaryJdbcTemplate);
            ReflectionTestUtils.setField(currentDao, "slaveJdbcTemplate", slaveJdbcTemplate);
            ReflectionTestUtils.setField(currentDao, "primarySqlSessionTemplate", primarySqlSessionTemplate);
            ReflectionTestUtils.setField(currentDao, "slaveSqlSessionTemplate", slaveSqlSessionTemplate);
            ReflectionTestUtils.setField(currentDao, "transactionTemplate", transactionTemplate);
        }

    }


    @After
    public void after() throws SQLException {
        if (primaryDataSource != null) {
            primaryDataSource.getConnection().close();
        }
        if (slaveDataSource != null) {
            slaveDataSource.getConnection().close();
        }
        if (shardingDataSource != null) {
            shardingDataSource.getConnection().close();
        }

        System.out.println(String.format("----------------本次测试耗时[%s]毫秒----------------", watch.getTime()));
    }





    /**
     * 创建SqlSessionFactory
     * @param dataSource
     * @return
     */
    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource) {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        //配置分页插件
        factory.setPlugins(new Interceptor[]{new PageInterceptor()});
        factory.setMapperLocations(getMapper());
        setConfigLocation(factory);

        try {
            return factory.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 指定配置文件
     * @param factory
     */
    private void setConfigLocation(SqlSessionFactoryBean factory){

        String configLocation = "classpath:mybatis.xml";

        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        if(!org.springframework.util.StringUtils.isEmpty(configLocation)){
            factory.setConfigLocation(resourceResolver.getResource(configLocation));
        }else{
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            factory.setConfiguration(configuration);
        }
    }


    /**
     * 获取mapper资源 (最终会被解析成statement语言对象)
     * @return
     */
    private Resource[] getMapper() {

        String mapperLocations = "classpath*:META-INF/mapper/*.xml";

        List<Resource> resources = new ArrayList<Resource>();
        if (!org.springframework.util.StringUtils.isEmpty(mapperLocations)) {
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


    public TransactionTemplate createTransactionTemplate(DataSource dataSource) {
        return new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }


    /**
     * 初始化aop植入
     * @param aspectClass
     */
    protected void initAspectJ(List<Class> aspectClass) {

        Map<Class, Method> pointcutMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.Pointcut.class);
        Map<Class, Method> aroundMethodMap = findMethodByAnnotation(aspectClass, Around.class);
        Map<Class, Method> beforeMethodMap = findMethodByAnnotation(aspectClass, Before.class);
        Map<Class, Method> afterMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.After.class);
        Map<Class, Method> afterReturningMethodMap = findMethodByAnnotation(aspectClass, AfterReturning.class);
        Map<Class, Method> afterThrowingMethodMap = findMethodByAnnotation(aspectClass, AfterThrowing.class);

        //创建一个代理工厂
        ProxyFactory factory = new ProxyFactory();
        for (Map.Entry<Class, Method> entry : pointcutMethodMap.entrySet()) {
            Class key = entry.getKey();
            Method method = entry.getValue();

            //获取 表达式
            Pointcut annotation = AnnotationUtils.findAnnotation(method, Pointcut.class);
            String pointcutExpression = annotation.value();


            //声明一个aspectj切点,一张切面
            AspectJExpressionPointcut cut = new AspectJExpressionPointcut();
            cut.setExpression(pointcutExpression);

            //简单创建Advice的工厂
            SimpleAspectInstanceFactory aspectInstanceFactory = new SimpleAspectInstanceFactory(key);

            if (aroundMethodMap.containsKey(key)) {
                AspectJAroundAdvice aroundAdvice = new AspectJAroundAdvice(aroundMethodMap.get(key), cut, aspectInstanceFactory);
                //使用Pointcut过滤
                Advisor advisor = new DefaultPointcutAdvisor(cut, aroundAdvice);
                factory.addAdvisor(advisor);
            }
            if (beforeMethodMap.containsKey(key)) {
                AspectJMethodBeforeAdvice beforeAdvice = new AspectJMethodBeforeAdvice(beforeMethodMap.get(key), cut, aspectInstanceFactory);
                Advisor advisor = new DefaultPointcutAdvisor(cut, beforeAdvice);
                factory.addAdvisor(advisor);
            }
            if (afterMethodMap.containsKey(key)) {
                AspectJAfterAdvice afterAdvice = new AspectJAfterAdvice(afterMethodMap.get(key), cut, aspectInstanceFactory);
                Advisor advisor = new DefaultPointcutAdvisor(cut, afterAdvice);
                factory.addAdvisor(advisor);
            }
            if (afterReturningMethodMap.containsKey(key)) {
                AspectJAfterReturningAdvice afterReturningAdvice = new AspectJAfterReturningAdvice(afterReturningMethodMap.get(key), cut, aspectInstanceFactory);
                Advisor advisor = new DefaultPointcutAdvisor(cut, afterReturningAdvice);
                factory.addAdvisor(advisor);
            }
            if (afterThrowingMethodMap.containsKey(key)) {
                AspectJAfterThrowingAdvice afterThrowingAdvice = new AspectJAfterThrowingAdvice(afterThrowingMethodMap.get(key), cut, aspectInstanceFactory);
                Advisor advisor = new DefaultPointcutAdvisor(cut, afterThrowingAdvice);
                factory.addAdvisor(advisor);
            }
        }

        this.proxyFactory = factory;

    }

    /**
     * 手动开启代理
     */
    private <T> T  enableProxy(T t) {
        proxyFactory.setTarget(t);
        return (T)proxyFactory.getProxy();
    }

    /**
     * 寻找类中有对应注解的方法
     * @param targets
     * @param annotationClass
     * @return
     */
    private Map<Class, Method> findMethodByAnnotation(List<Class> targets, Class annotationClass) {
        Map<Class, Method> resultMap = new LinkedHashMap<>();
        for (Class target : targets) {
            Method m = (Method) Stream.of(target.getMethods()).filter(method -> AnnotationUtils.findAnnotation(method, annotationClass) != null).findFirst().get();
            resultMap.put(target, m);
        }
        return resultMap;
    }


}

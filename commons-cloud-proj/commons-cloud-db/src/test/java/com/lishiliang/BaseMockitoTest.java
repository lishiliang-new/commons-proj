
package com.lishiliang;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.lishiliang.core.utils.Context;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Pointcut;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lisl
 * @version 1.0
 */
//@RunWith(MockitoJUnitRunner.class)
public class BaseMockitoTest {

    Logger logger = LoggerFactory.getLogger(BaseMockitoTest.class);

    //服务ip列表
    protected Map<String, String> serverIpMap = new HashMap() {{
        put("elastic-job-facade-impl", "127.0.0.1:8081");
    }};

    protected ProxyFactory proxyFactory;

    @Spy
    protected static RestTemplate restTemplate = new RestTemplate();

    static {
//        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//        httpRequestFactory.setConnectionRequestTimeout(3000);
//        httpRequestFactory.setConnectTimeout(3000);
//        httpRequestFactory.setReadTimeout(3000);
//        restTemplate = new RestTemplate(httpRequestFactory);
    }

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


    /**
     * 对feign的模拟
     * @param clients feign对象
     * @throws Exception
     */
    protected void initFeignClient(Object... clients) throws Exception {

        for (Object client : clients) {
            //判断是否是feign客户端
            Class<?> clientClass = client.getClass();
            FeignClient annotation = AnnotationUtils.findAnnotation(clientClass, FeignClient.class);
            if (annotation == null) {
                continue;
            }

            final String ip = serverIpMap.get(annotation.value());
            //获取所有RequestMapping注解的方法
            List<Method> methods = MethodUtils.getMethodsListWithAnnotation(clientClass, RequestMapping.class);
            for (Method method : methods) {
                try {
                    //可变参数 不好处理 暂时先处理5个参数的
                    Class<?>[] pts = method.getParameterTypes();
                    int parameterCount = method.getParameterCount();
                    if (parameterCount == 0) {
                        PowerMockito.when(client, method).withNoArguments().thenAnswer((data)->doAnswer(data, ip));
                    } else if (parameterCount == 1) {
                        PowerMockito.when(client, method).withArguments(any(pts[0])).thenAnswer((data)->doAnswer(data, ip));
                    } else if (parameterCount == 2){
                        PowerMockito.when(client, method).withArguments(any(pts[0]), any(pts[1])).thenAnswer((data)->doAnswer(data, ip));
                    } else if (parameterCount == 3){
                        PowerMockito.when(client, method).withArguments(any(pts[0]), any(pts[1]), any(pts[2])).thenAnswer((data)->doAnswer(data, ip));
                    } else if (parameterCount == 4){
                        PowerMockito.when(client, method).withArguments(any(pts[0]), any(pts[1]), any(pts[2]), any(pts[3])).thenAnswer((data)->doAnswer(data, ip));
                    } else if (parameterCount == 5){
                        PowerMockito.when(client, method).withArguments(any(pts[0]), any(pts[1]), any(pts[2]), any(pts[3]), any(pts[4])).thenAnswer((data)->doAnswer(data, ip));
                    }
                } catch (Exception e) {
                    logger.info(String.format("%s|%s|msg: %s", client.getClass().getName(), method.getName(), e.getMessage()));
                }
            }
        }

    }

    /**
     * feign调用模拟成RestTemplate发送请求
     * @param data
     * @return
     */
    protected Object doAnswer(InvocationOnMock data, String ip) {

        Object[] arguments = data.getArguments();
        Method method = data.getMethod();

        RequestMapping mapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        //指定 服务
        String url = String.format("%s%s/%s","http://", ip, mapping.value()[0]);

        //处理PathVariable类型的请求
        Map<String, Object> variableMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            java.lang.annotation.Annotation[] annotations = parameters[i].getAnnotations();
            for (Annotation annotation : annotations) {
                if (PathVariable.class.getName().equals(annotation.annotationType().getName())) {
                    Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
                    variableMap.put((String) annotationAttributes.get("value"), arguments[i]);
                    break;
                }
            }
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).uriVariables(variableMap);


        //构建请求相关参数
        //构建返回值泛型
        ParameterizedTypeReference<Object> resultTypeReference = ParameterizedTypeReference.forType(method.getGenericReturnType());
        //构建请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("traceId", Context.getCurrentContextTraceId());
        httpHeaders.set("from-to", "from-to");
        //构建请求体
        HttpEntity<Object[]> httpEntity = new HttpEntity<>(data.getArguments(), httpHeaders);
        HttpMethod httpMethod = HttpMethod.POST;
        if (mapping.method() != null && mapping.method().length > 1) {
            httpMethod = HttpMethod.valueOf(mapping.method()[0].name());
        }

        logger.info(" restTemplate start url:{}, requestData:{}, requestHeaders:{}, responseType:{}", builder.toUriString(), data.getArguments(), httpHeaders.toString(), resultTypeReference.toString());
        ResponseEntity response = restTemplate.exchange(builder.toUriString(), httpMethod, httpEntity, resultTypeReference);
        return response.getBody();
    }


    /**
     * 初始化aop植入
     * @param aspectClass
     * @param aspectInstance 切面对象
     */
    protected void initAspectJ(Class aspectClass, Object aspectInstance) {

        Map<Class, Method> pointcutMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.Pointcut.class);
        Map<Class, Method> aroundMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.Around.class);
        Map<Class, Method> beforeMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.Before.class);
        Map<Class, Method> afterMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.After.class);
        Map<Class, Method> afterReturningMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.AfterReturning.class);
        Map<Class, Method> afterThrowingMethodMap = findMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.AfterThrowing.class);

        //创建一个代理工厂
        ProxyFactory factory = new ProxyFactory();
        //首先添加一个拦截链 用于传递参数 (很重要)
        factory.addAdvisor(ExposeInvocationInterceptor.ADVISOR);

        for (Map.Entry<Class, Method> entry : pointcutMethodMap.entrySet()) {
            Class key = entry.getKey();
            Method method = entry.getValue();

            //获取 表达式
            Pointcut annotation = AnnotationUtils.findAnnotation(method, Pointcut.class);
            String pointcutExpression = annotation.value();
            if (StringUtils.isBlank(pointcutExpression)) {
                return;
            }

            //声明一个aspectj切点,一张切面
            AspectJExpressionPointcut cut = new AspectJExpressionPointcut();
            cut.setExpression(pointcutExpression);

            //简单创建Advice的工厂
            SingletonAspectInstanceFactory aspectInstanceFactory = new SingletonAspectInstanceFactory(aspectInstance);

            // 首先需要加入异常拦截器 @see ReflectiveAspectJAdvisorFactory#METHOD_COMPARATOR
            if (afterThrowingMethodMap.get(key) != null) {
                Method m = afterThrowingMethodMap.get(key);
                AspectJAfterThrowingAdvice afterThrowingAdvice = new AspectJAfterThrowingAdvice(afterThrowingMethodMap.get(key), cut, aspectInstanceFactory);

                AfterThrowing afa = AnnotationUtils.findAnnotation(m, AfterThrowing.class);
                Optional.of(afa == null ? null : afa.throwing()).ifPresent(e->afterThrowingAdvice.setThrowingName(e));

                Advisor advisor = new DefaultPointcutAdvisor(cut, afterThrowingAdvice);
                factory.addAdvisor(advisor);
            }

            if (aroundMethodMap.get(key) != null) {
                AspectJAroundAdvice aroundAdvice = new AspectJAroundAdvice(aroundMethodMap.get(key), cut, aspectInstanceFactory);
                //使用Pointcut过滤
                DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(cut, aroundAdvice);
                factory.addAdvisor(advisor);
            }
            if (beforeMethodMap.get(key) != null) {
                AspectJMethodBeforeAdvice beforeAdvice = new AspectJMethodBeforeAdvice(beforeMethodMap.get(key), cut, aspectInstanceFactory);
                Advisor advisor = new DefaultPointcutAdvisor(cut, beforeAdvice);
                factory.addAdvisor(advisor);
            }
            if (afterMethodMap.get(key) != null) {
                AspectJAfterAdvice afterAdvice = new AspectJAfterAdvice(afterMethodMap.get(key), cut, aspectInstanceFactory);
                Advisor advisor = new DefaultPointcutAdvisor(cut, afterAdvice);
                factory.addAdvisor(advisor);
            }

            if (afterReturningMethodMap.get(key) != null) {
                Method m = afterReturningMethodMap.get(key);
                AspectJAfterReturningAdvice afterReturningAdvice = new AspectJAfterReturningAdvice(m, cut, aspectInstanceFactory);

                AfterReturning afa = AnnotationUtils.findAnnotation(m, AfterReturning.class);
                Optional.of(afa == null ? null : afa.returning()).ifPresent(e->afterReturningAdvice.setReturningName(e));

                DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(cut, afterReturningAdvice);
                factory.addAdvisor(advisor);
            }

        }

        this.proxyFactory = factory;

    }


    /**
     * 手动开启代理
     */
    protected  <T> T createProxy(T t) {
        proxyFactory.setTarget(t);
        return (T)proxyFactory.getProxy();
    }

    /**
     * 寻找类中有对应注解的方法
     * @param aspectClass
     * @param annotationClass
     * @return
     */
    private Map<Class, Method> findMethodByAnnotation(Class aspectClass, Class annotationClass) {
        Map<Class, Method> resultMap = new LinkedHashMap<>();
        Method m = (Method) Stream.of(aspectClass.getMethods()).filter(method -> AnnotationUtils.findAnnotation(method, annotationClass) != null).findFirst().orElse(null);
        resultMap.put(aspectClass, m);
        return resultMap;
    }





}

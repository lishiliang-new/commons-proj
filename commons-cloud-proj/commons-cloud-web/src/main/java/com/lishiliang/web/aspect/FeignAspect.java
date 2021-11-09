package com.lishiliang.web.aspect;


import com.lishiliang.core.utils.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lisl
 * @version 1.0
 * @desc :Feign 调用 切面逻辑
 * TODO 需要优化
 */
@Aspect
@Component
public class FeignAspect {

    private static final Logger logger = LoggerFactory.getLogger(FeignAspect.class);

    @Value("${spring.application.name}")
    private String clientName;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private HttpServletRequest request;

    private final Map<Class, String> msgCache = new HashMap<>();

    @Pointcut("@within(org.springframework.cloud.openfeign.FeignClient)")
    public void feignPointcut(){}

    /**
     *
     * @param point
     * @return
     */
    @Around("feignPointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {

        String msg = getFromCache(((MethodSignature) point.getSignature()).getMethod().getDeclaringClass());
        request.setAttribute("from-to", msg);
        logger.info("开始进行远程调用: {}", msg);

        return point.proceed();
    }

    private String getFromCache(Class<?> clazz) {
        String msg = msgCache.get(clazz);
        if (msg == null) {
            FeignClient annotation = AnnotationUtils.findAnnotation(clazz, FeignClient.class);
            if (annotation != null) {
                String provideName = annotation.name();
                msg = String.format("from <<<%s[%s:%s]-to>>>%s", clientName, IpUtils.getIp(), serverPort, provideName);
                msgCache.put(clazz, msg);
            }
        }
        return msg;
    }


}

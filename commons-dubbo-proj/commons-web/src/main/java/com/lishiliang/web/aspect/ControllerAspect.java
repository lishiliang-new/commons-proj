package com.lishiliang.web.aspect;


import com.lishiliang.core.model.DataModel;
import com.lishiliang.core.utils.Builder;
import com.lishiliang.core.utils.Context;
import com.lishiliang.core.utils.Utils;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lisl
 * @version 1.0
 * @desc :Controller切面逻辑
 */
@Aspect
@Component
public class ControllerAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAspect.class);

    @Pointcut("execution(public * com.lishiliang.web.controller..*.*(..))")
    public void controllerPointcut(){}

    /**
     *
     * @param point
     * @return
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {

        //向Context 注入 traceId
        final String traceId = Utils.generateUUIDWithMD5();
        Context.initInheritableThreadLocal(traceId);

        return point.proceed();

    }

    /**
     * 给返回的DataModel添加TRACE_ID
     * @param point
     * @param dataModel
     * @return
     */
    @AfterReturning(returning="dataModel", pointcut="controllerPointcut()")
    public Object afterReturning(JoinPoint point, DataModel dataModel) {
        
        
        Builder<Map<String, Object>> builder = Builder.ofDefault(dataModel.getAdditionalData(), HashMap<String, Object>::new)
                .with(Map::put, Context.TRACE_ID, Context.getCurrentContextTraceId());
//        Context.removeTraceId();
        
        return Builder.of(dataModel).with(DataModel::setAdditionalData, builder.build()).build();
    }

}

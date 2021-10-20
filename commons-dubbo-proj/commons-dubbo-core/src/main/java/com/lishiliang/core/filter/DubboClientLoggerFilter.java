package com.lishiliang.core.filter;

import com.alibaba.fastjson.JSON;
import com.lishiliang.core.utils.Context;
import com.lishiliang.core.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Activate(group = {Constants.CONSUMER}, order = -9999)
public class DubboClientLoggerFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(DubboClientLoggerFilter.class);

    private static final String TRACE_ID = "traceId";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //  获取上下文的traceId 并初始化到RpcContext 以便服务提供方获取(客户端和服务的共享一个RpcContext)
        RpcContext.getContext().setAttachment(TRACE_ID, Context.getCurrentContextTraceId());

        Result result = null;
        Long takeTime = 0L;
        Long startTime = System.currentTimeMillis();

        logger.info("Invoke RPC service. traceId : {}", Context.getCurrentContextTraceId());
        try {
            result = invoker.invoke(invocation);
        } finally {
            takeTime = System.currentTimeMillis() - startTime;
            logger.info(String.format("Result Info:%s|IN:%s|OUT:%s|%dms", invocation.getMethodName(), JSON.toJSONString(invocation.getArguments()), Utils.abbreviate(JSON.toJSONString(result), 1024), takeTime));
        }
        return result;
    }

}

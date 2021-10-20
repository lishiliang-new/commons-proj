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



@Activate(group = {Constants.PROVIDER}, order = -9999)
public class DubboLoggerFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(DubboLoggerFilter.class);

    private static final String TRACE_ID = "traceId";

    /*
     * (non-Javadoc)
     *
     * @param invoker
     *
     * @param invocation
     *
     * @return
     *
     * @throws RpcException
     *
     * @see org.apache.dubbo.rpc.Filter#invoke(org.apache.dubbo.rpc.Invoker, org.apache.dubbo.rpc.Invocation)
     */

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取客服端存入的traceId 初始化到上下文中 (客户端和服务的共享一个RpcContext)
        Context.initInheritableThreadLocal(RpcContext.getContext().getAttachment(TRACE_ID));

        Result result = null;
        Long takeTime = 0L;
        Long startTime = System.currentTimeMillis();

        try {
            result = invoker.invoke(invocation);
        } finally {
            takeTime = System.currentTimeMillis() - startTime;
            logger.info(String.format("Result Info:%s|IN:%s|OUT:%s|%dms", invocation.getMethodName(), JSON.toJSONString(invocation.getArguments()), Utils.abbreviate(JSON.toJSONString(result), 1024), takeTime));
        }
        return result;
    }
}

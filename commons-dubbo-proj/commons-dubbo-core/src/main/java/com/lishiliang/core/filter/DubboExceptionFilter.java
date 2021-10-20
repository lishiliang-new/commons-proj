package com.lishiliang.core.filter;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.ErrorCodes;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.Method;

/**
 * @desc: 自定义Dubbo异常过滤工具(指定了activate的group，则filter升级为系统级，将会自动加入到filte链中执行)
 * @date:2018年12月14日 下午3:18:34
 */
@Activate(group = Constants.PROVIDER)
public class DubboExceptionFilter implements Filter {

    private final Logger logger;

    public DubboExceptionFilter() {

        this(LoggerFactory.getLogger(DubboExceptionFilter.class));
    }

    public DubboExceptionFilter(Logger logger) {

        this.logger = logger;
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        try {
            Result result = invoker.invoke(invocation);
            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                try {
                    Throwable exception = result.getException();

                    // 如果是checked异常，直接抛出
                    if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                        return result;
                    }
                    // 在方法签名上有声明，直接抛出
                    try {
                        Method method = invoker.getInterface().getMethod(invocation.getMethodName(),
                            invocation.getParameterTypes());
                        Class<?>[] exceptionClassses = method.getExceptionTypes();
                        for (Class<?> exceptionClass : exceptionClassses) {
                            if (exception.getClass().equals(exceptionClass)) {
                                return result;
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        return result;
                    }

                    // 未在方法签名上定义的异常，在服务器端打印ERROR日志
                    logger.error("Got unchecked and undeclared exception which called by "
                        + RpcContext.getContext().getRemoteHost() + ". service: " + invoker.getInterface().getName()
                        + ", method: " + invocation.getMethodName() + ", exception: " + exception.getClass().getName()
                        + ": " + exception.getMessage(), exception);

                    // 异常类和接口类在同一jar包里，直接抛出
                    String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                    String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                    if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
                        return result;
                    }
                    // 是JDK自带的异常，直接抛出
                    String className = exception.getClass().getName();
                    if (className.startsWith("java.") || className.startsWith("javax.")) {
                        return result;
                    }
                    // 是Dubbo本身的异常，直接抛出
                    if (exception instanceof RpcException) {
                        RpcException rpcException = (RpcException) exception;
                        // 如果超时异常，暂时抛出BusinessRuntimeException
                        if (rpcException.isTimeout()) {
                            // 否则，包装成RuntimeException抛给客户端
                            return new RpcResult(new BusinessRuntimeException(ErrorCodes.TIMEOUT_ERROR.getCode(),
                                "dubbo rpc invoke time out exception!"));
                        }
                        return result;
                    }

                    if (exception instanceof BusinessRuntimeException) {
                        BusinessRuntimeException runExce = (BusinessRuntimeException) exception;
                        // 否则，包装成RuntimeException抛给客户端
                        return new RpcResult(new BusinessRuntimeException(runExce.getErrCode(), runExce.getErrMsg(),
                            runExce.getAttach()));
                    } else {
                        // 否则，包装成RuntimeException抛给客户端
                        return new RpcResult(new RuntimeException(StringUtils.toString(exception)));
                    }

                } catch (Throwable e) {
                    logger.warn("Fail to ExceptionFilter when called by " + RpcContext.getContext().getRemoteHost()
                        + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                        + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
                    return result;
                }
            }
            return result;
        } catch (RuntimeException e) {
            logger.error(
                "Got unchecked and undeclared exception which called by " + RpcContext.getContext().getRemoteHost()
                    + ". service: " + invoker.getInterface().getName() + ", method: " + invocation.getMethodName()
                    + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }
    }

}

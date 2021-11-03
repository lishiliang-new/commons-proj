package com.lishiliang.core.handler;

import com.alibaba.fastjson.JSONObject;
import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.Constant;
import com.lishiliang.model.ErrorCode;
import feign.Feign;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * feign异常后置处理器
 * (response.status() >= 200 && response.status() < 300) == false 则会进入ErrorDecoder  @see SynchronousMethodHandler#executeAndDecode
 */
@Configuration
@ConditionalOnClass(Feign.class)
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {

        if (response.body() == null) {
            throw new BusinessRuntimeException(Constant.FAIL_CODE, String.format("|%s %s", methodKey, "远程调用异常"));
        }
        JSONObject jsonObject = null;
        try {
            // 这里拿到服务端抛出的异常信息
            String message = Util.toString(response.body().asReader());
            jsonObject = JSONObject.parseObject(message);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        throw  new BusinessRuntimeException(jsonObject.getString("code") , String.format("|%s %s", methodKey, jsonObject.getString("msg")));
    }
}

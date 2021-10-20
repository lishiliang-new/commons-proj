package com.lishiliang.core.exception;

import com.alibaba.fastjson.JSONObject;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * feign异常后置处理器
 * (response.status() >= 200 && response.status() < 300) == false 则会进入ErrorDecoder  @see SynchronousMethodHandler#executeAndDecode
 */
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // 这里拿到服务端抛出的异常信息
            String message = Util.toString(response.body().asReader());
            JSONObject jsonObject = JSONObject.parseObject(message);
            return new BusinessRuntimeException(jsonObject.getString("code") , String.format("|%s %s", methodKey, jsonObject.getString("msg")));

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return decode(methodKey, response);
    }
}

//package com.lishiliang.core.exception;
//
//import feign.FeignException;
//import feign.Response;
//import feign.codec.DecodeException;
//import feign.codec.Decoder;
//
//import java.io.IOException;
//import java.lang.reflect.Type;
//
///**
// * 自定义结果解析器 暂时不使用
// */
//public class FeignResultDecoder implements Decoder {
//
//    @Override
//    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
//
//        if (response.body() == null) {
//            throw new DecodeException(response.status(), "没有返回有效的数据", response.request());
//        }
//        String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
//        //对结果进行转换
//        Result result = (Result) JsonUtil.json2obj(bodyStr, type);
//        //如果返回错误，且为内部错误，则直接抛出异常
//        if (result.getCode() != ResultCode.SUCCESS.code) {
//            if (!result.isUserPrompt()) {
//                throw new DecodeException(response.status(), "接口返回错误：" + result.getMessage(), response.request());
//            }
//        }
//        return result.data;
//    }
//
//}

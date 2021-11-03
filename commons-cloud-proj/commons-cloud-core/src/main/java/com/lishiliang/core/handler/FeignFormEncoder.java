package com.lishiliang.core.handler;

import feign.Feign;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.form.ContentType;
import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * feign form表单参数传递处理
 */
@Configuration
@ConditionalOnClass(Feign.class)
public class FeignFormEncoder extends SpringFormEncoder {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {

        String contentTypeValue = this.getContentTypeValue(template.headers());
        ContentType contentType = ContentType.of(contentTypeValue);
        //不是表单传递不处理
        if (ContentType.URLENCODED != contentType) {
            return;
        }

        if (bodyType instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl) bodyType).getRawType().equals(Map.class)) {
            //处理value为null的数据 不然会被处理成"null"字符串
            Map<String, ?> data = (Map<String, ?>) object;
            Set<String> nullSet = new HashSet<>();
            for (Map.Entry<String, ?> entry : data.entrySet()) {
                if (entry.getValue() == null) {
                    nullSet.add(entry.getKey());
                }
            }
            for (String s : nullSet) {
                data.remove(s);
            }
            super.encode(data, MAP_STRING_WILDCARD, template);
            return;
        } else if (bodyType instanceof ParameterizedTypeImpl){
            super.encode(object, bodyType, template);
            return;
        }
        super.encode(object, bodyType, template);
    }


    private String getContentTypeValue (Map<String, Collection<String>> headers) {

        for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(CONTENT_TYPE_HEADER)) {
                continue;
            }
            for (String contentTypeValue : entry.getValue()) {
                if (contentTypeValue == null) {
                    continue;
                }
                return contentTypeValue;
            }
        }
        return null;
    }
}

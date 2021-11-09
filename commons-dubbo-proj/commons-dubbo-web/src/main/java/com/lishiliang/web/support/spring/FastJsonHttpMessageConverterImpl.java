
 /**
 * @Title: FastJsonHttpMessageConverterImpl.java 
 * @Package:com.lishiliang.web.support.spring
 * @desc: TODO  
 * @author: lisl    
 * @date
 */
 
package com.lishiliang.web.support.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lishiliang.core.utils.JsoupUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.support.spring.FastJsonContainer;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.alibaba.fastjson.support.spring.MappingFastJsonValue;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;

/**
 * @author lisl
 * @desc 过滤byte类型的数据转换
 * @see https://www.cnblogs.com/xtly2012/p/9759641.html
 */
public class FastJsonHttpMessageConverterImpl extends FastJsonHttpMessageConverter {

    /**
     * FastJson转码与文件导出存在冲突，因此，过滤掉byte类型的数据转换
     */
    @Override
    protected boolean supports(Class<?> clazz) {
        if (clazz.equals(byte[].class)) {
            return false;
        }
        return true;
    }

    /**
     * 过滤请求中的xss字符
     */
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        
        InputStream in = inputMessage.getBody();
        //得到请求的json
        String input = IOUtils.toString(in, StandardCharsets.UTF_8);
        String parseJson = JsoupUtils.clean(input);
        
        try {
            //以下代码，从FastJsonHttpMessageConverter类中copy
            return JSON.parseObject(IOUtils.toInputStream(parseJson, StandardCharsets.UTF_8),
                    getFastJsonConfig().getCharset(),
                    type,
                    getFastJsonConfig().getParserConfig(),
                    getFastJsonConfig().getParseProcess(),
                    JSON.DEFAULT_PARSER_FEATURE,
                    getFastJsonConfig().getFeatures());
        } catch (JSONException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex);
        }
    }

    /**
     * 过滤响应中的xss字符
     */
    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        
        ByteArrayOutputStream outnew = new ByteArrayOutputStream();
        try {
            HttpHeaders headers = outputMessage.getHeaders();

            //获取全局配置的filter
            SerializeFilter[] globalFilters = getFastJsonConfig().getSerializeFilters();
            List<SerializeFilter> allFilters = new ArrayList<>(Arrays.asList(globalFilters));

            boolean isJsonp = false;

            //不知道为什么会有这行代码， 但是为了保持和原来的行为一致，还是保留下来
            Object value = strangeCodeForJackson2(object);

            if (value instanceof FastJsonContainer) {
                FastJsonContainer fastJsonContainer = (FastJsonContainer) value;
                PropertyPreFilters filters = fastJsonContainer.getFilters();
                allFilters.addAll(filters.getFilters());
                value = fastJsonContainer.getValue();
            }

            //revise 2017-10-23 ,
            // 保持原有的MappingFastJsonValue对象的contentType不做修改 保持旧版兼容。
            // 但是新的JSONPObject将返回标准的contentType：application/javascript ，不对是否有function进行判断
            if (value instanceof MappingFastJsonValue) {
                if (!StringUtils.isEmpty(((MappingFastJsonValue) value).getJsonpFunction())) {
                    isJsonp = true;
                }
            } else if (value instanceof JSONPObject) {
                isJsonp = true;
            }

            int len = JSON.writeJSONString(outnew, //
                getFastJsonConfig().getCharset(), //
                    value, //
                    getFastJsonConfig().getSerializeConfig(), //
                    //fastJsonConfig.getSerializeFilters(), //
                    allFilters.toArray(new SerializeFilter[allFilters.size()]),
                    getFastJsonConfig().getDateFormat(), //
                    JSON.DEFAULT_GENERATE_FEATURE, //
                    getFastJsonConfig().getSerializerFeatures());

            if (isJsonp) {
                headers.setContentType(APPLICATION_JAVASCRIPT);
            }

            if (getFastJsonConfig().isWriteContentLength()) {
                headers.setContentLength(len);
            }

            //拒绝xss攻击
            String outString = JsoupUtils.clean(outnew.toString());
            IOUtils.write(outString, outputMessage.getBody(), StandardCharsets.UTF_8);
            //outnew.writeTo(outputMessage.getBody());

        } catch (JSONException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        } finally {
            outnew.close();
        }
    }
    
    /**
     * @author lisl
     * @desc 从父类copy而来
     * @param obj
     * @return
     */
    private Object strangeCodeForJackson2(Object obj) {
        if (obj != null) {
            String className = obj.getClass().getName();
            if ("com.fasterxml.jackson.databind.node.ObjectNode".equals(className)) {
                return obj.toString();
            }
        }
        return obj;
    }
}

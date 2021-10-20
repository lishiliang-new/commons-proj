
/**
* @Title: MassageConverConfiguration.java 
* @Package:com.lishiliang.framework.core.configuration
* @desc: TODO  
* @author: lisl    
* @date:2019年9月17日 下午3:02:15    
*/

package com.lishiliang.web.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.lishiliang.web.support.spring.FastJsonHttpMessageConverterImpl;

/**
 * @author lisl
 * @desc 避免Date型数据转换为json变为Long型数据，并且使用fastjson进行数据格式化，配合对象属性注解：@JSONField
 * @see https://blog.csdn.net/CLG_CSDN/article/details/98180387
 */
@Configuration
@ConditionalOnClass(value = com.alibaba.fastjson.JSON.class)
public class MassageConverConfiguration {

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        //需要先定义一个 convert 转换消息的对象;
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverterImpl();

        //添加fastJson 的配置信息，比如：是否要格式化返回的json数据;
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);//输出数据格式化

        //在convert中添加配置信息.
        fastConverter.setFastJsonConfig(fastJsonConfig);
        HttpMessageConverter<?> converter = fastConverter;

        return new HttpMessageConverters(converter);
    }

}

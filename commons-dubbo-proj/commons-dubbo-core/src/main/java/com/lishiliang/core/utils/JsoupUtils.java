
 /**
 * @Title: JsoupUtils.java 
 * @Package:com.lishiliang.core.utils
 * @desc: TODO  
 * @author: lisl    
 * @date
 */

 package com.lishiliang.core.utils;

 import com.alibaba.fastjson.JSON;
 import com.alibaba.fastjson.JSONArray;
 import com.alibaba.fastjson.JSONObject;
 import com.lishiliang.core.exception.BusinessRuntimeException;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.safety.Whitelist;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;

 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.nio.charset.StandardCharsets;



 /**
 * @author lisl
 * @desc 解析html工具类
 */
public class JsoupUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JsoupUtils.class);
    
    private static final Whitelist NONE_WHITELIST = Whitelist.none();

    // 过滤配置
    private static final Document.OutputSettings outputsettings = new Document.OutputSettings();

    /**
     * 过滤XSS字符
     * @param content
     * @return
     */
    public static String clean(String content) {
        return content == null ? null : Jsoup.clean(content, "", NONE_WHITELIST, outputsettings);
    }

    static {
        //过滤配置参数
        outputsettings.prettyPrint(false);
        outputsettings.charset(StandardCharsets.UTF_8);
        
        //读取配置JSON文件
        Resource resource = new ClassPathResource("allow-html.json");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))){
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line.trim());
            }

            JSONObject jsonObject = JSON.parseObject(stringBuilder.toString());

            //允许标签
            JSONArray tags = jsonObject.getJSONArray("allow_tags");
            NONE_WHITELIST.addTags(tags.toArray(new String[tags.size()]));
            LOGGER.debug("允许标签:{}", tags);

            //允许属性
            JSONArray properties = jsonObject.getJSONArray("allow_properties");
            NONE_WHITELIST.addAttributes(":all",properties.toArray(new String[properties.size()]));
            LOGGER.debug("允许属性:{}",properties);

            //允许特殊属性
            JSONObject specialProperties = jsonObject.getJSONObject("special_properties");
            specialProperties.keySet().stream().forEach(tag -> {
                JSONArray attributes = specialProperties.getJSONArray(tag);
                NONE_WHITELIST.addAttributes(tag,attributes.toArray(new String[attributes.size()]));
                LOGGER.debug("允许特殊属性:标签={},属性={}",tag,attributes);
            });

            //允许特殊协议
            JSONObject protocols = jsonObject.getJSONObject("protocols");
            protocols.keySet().stream().forEach(tag -> {
                JSONObject protoObject = protocols.getJSONObject(tag);
                protoObject.keySet().stream().forEach(attr -> {
                    JSONArray protocolValues = protoObject.getJSONArray(attr);
                    NONE_WHITELIST.addProtocols(tag,attr,protocolValues.toArray(new String[protocolValues.size()]));
                    LOGGER.debug("允许特殊协议:标签={},属性={},协议={}",tag,attr,protocolValues);
                });
            });
            
            //固定属性值,非必须的
            JSONObject fixedProperties = jsonObject.getJSONObject("fixed_properties");
            if(fixedProperties != null && !fixedProperties.isEmpty()) {
                fixedProperties.keySet().stream().forEach(tag -> {
                    JSONObject property = fixedProperties.getJSONObject(tag);
                    if(property != null && !property.isEmpty()) {
                        property.keySet().stream().forEach(attr -> {
                            String value = property.getString(attr);
                            NONE_WHITELIST.addEnforcedAttribute(tag, attr, value);
                            LOGGER.debug("强制属性:标签={},属性={},值={}",tag,attr,value);
                        });
                    }
                 });
            }
        } catch (Exception e) {
            LOGGER.error(ErrorCodes.ERROR_ALLOW_HTML_FILE.getDesc(), e);
            throw new BusinessRuntimeException(ErrorCodes.ERROR_ALLOW_HTML_FILE.getCode(), ErrorCodes.ERROR_ALLOW_HTML_FILE.getDesc(), e);
        }
    }
}

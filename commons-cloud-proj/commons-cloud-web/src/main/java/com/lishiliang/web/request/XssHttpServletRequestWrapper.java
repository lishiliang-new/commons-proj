
/**
 * @Title: XssHttpServletRequestWrapper.java
 * @Package:com.lishiliang.web.request
 * @desc: TODO
 * @author: lisl
 * @date
 */

package com.lishiliang.web.request;

import com.lishiliang.core.utils.JsoupUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author lisl
 * @desc 封装默认的request，阻止xss攻击
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    HttpServletRequest orgRequest;
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String CONTENT_TYPE = "Content-Type";
    
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        
        super(request);
        this.orgRequest = request;
    }
    
    /**
     * 覆盖getParameter方法，将参数名和参数值都做xss过滤.
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
     */
    @Override
    public String getParameter(String name) {
        
        name = JsoupUtils.clean(name);
        String value = super.getParameter(name);
        if (StringUtils.isNotBlank(value)) {
            value = JsoupUtils.clean(value);
        }
        return value;
    }
    
    @Override
    public String[] getParameterValues(String name) {
        
        String[] arr = super.getParameterValues(name);
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = JsoupUtils.clean(arr[i]);
            }
        }
        return arr;
    }
    
    /**
     * 覆盖getHeader方法，将参数名和参数值都做xss过滤。<br/>
     * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/>
     * getHeaderNames 也可能需要覆盖
     */
    @Override
    public String getHeader(String name) {
        
        name = JsoupUtils.clean(name);
        String value = super.getHeader(name);
        if (StringUtils.isNotBlank(value)) {
            value = JsoupUtils.clean(value);
        }
        return value;
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        
        // 非json处理
        if (!JSON_CONTENT_TYPE.equalsIgnoreCase(super.getHeader(CONTENT_TYPE))) {
            return super.getInputStream();
        }
        InputStream in = super.getInputStream();
        String body = IOUtils.toString(in, StandardCharsets.UTF_8);
        IOUtils.closeQuietly(in);
        
        // 空串处理直接返回
        if (StringUtils.isBlank(body)) {
            return super.getInputStream();
        }
        
        // xss过滤
        body = JsoupUtils.clean(body);
        return new RequestCachingInputStream(body.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 获取最原始的request
     *
     * @return
     */
    public HttpServletRequest getOrgRequest() {
        
        return orgRequest;
    }
    
    /**
     * 获取最原始的request的静态方法
     *
     * @return
     */
    public static HttpServletRequest getOrgRequest(HttpServletRequest req) {
        
        if (req instanceof XssHttpServletRequestWrapper) {
            return ((XssHttpServletRequestWrapper) req).getOrgRequest();
        }
        return req;
    }
    
    /**
     * <pre>
     * servlet中inputStream只能一次读取，后续不能再次读取inputStream
     * xss过滤body后，重新把流放入ServletInputStream中
     * </pre>
     */
    private static class RequestCachingInputStream extends ServletInputStream {
        
        private final ByteArrayInputStream inputStream;
        
        public RequestCachingInputStream(byte[] bytes) {
            
            inputStream = new ByteArrayInputStream(bytes);
        }
        
        @Override
        public int read() throws IOException {
            
            return inputStream.read();
        }
        
        @Override
        public boolean isFinished() {
            
            return inputStream.available() == 0;
        }
        
        @Override
        public boolean isReady() {
            
            return true;
        }
        
        @Override
        public void setReadListener(ReadListener readListener) {
        
        }
    }
}

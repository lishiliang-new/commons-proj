
/**
 * @Title: XssFilter.java
 * @Package:com.lishiliang.web.filter
 * @desc: TODO
 * @author: lisl
 * @date
 */

package com.lishiliang.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lishiliang.core.utils.Context;
import com.lishiliang.core.utils.IpUtils;
import com.lishiliang.core.utils.SpringUtils;
import org.apache.commons.lang3.StringUtils;

import com.lishiliang.web.request.XssHttpServletRequestWrapper;
import org.springframework.core.env.Environment;

/**
 * @author lisl
 * @desc
 */
public class XssFilter implements Filter {
    
    /**
     * 例外urls
     */
    private List<String> excludeUrls = new ArrayList<>();
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
        String excludeUrlStr = filterConfig.getInitParameter("excludes");
        if (StringUtils.isNotEmpty(excludeUrlStr)) {
            String[] url = excludeUrlStr.split(",");
            Collections.addAll(this.excludeUrls, url);
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //若请求中存在traceId,feign 则代表是内部feign调用 则不进行后续过滤
        if (req.getHeader(Context.TRACE_ID) != null && req.getHeader("feign") != null) {
            Environment env = SpringUtils.getBean(Environment.class);
            resp.setHeader("feign", String.format("response %s[%s:%s]", env.getProperty("spring.application.name"), IpUtils.getIp(), env.getProperty("server.port")));
            chain.doFilter(request, response);
            return;
        }
        
        if (handleExcludeUrls(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }
    
    /**
     * @author lisl
     * @desc 过滤不进行处理的路径
     * @param request
     * @param response
     * @return
     */
    private boolean handleExcludeUrls(HttpServletRequest request, HttpServletResponse response) {
        
        if (excludeUrls.isEmpty()) {
            return false;
        }
        String url = request.getServletPath();
        for (String pattern : excludeUrls) {
            Pattern p = Pattern.compile("^" + pattern);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }
}

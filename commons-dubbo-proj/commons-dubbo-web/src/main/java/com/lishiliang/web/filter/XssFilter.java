
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

import org.apache.commons.lang3.StringUtils;

import com.lishiliang.web.request.XssHttpServletRequestWrapper;

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

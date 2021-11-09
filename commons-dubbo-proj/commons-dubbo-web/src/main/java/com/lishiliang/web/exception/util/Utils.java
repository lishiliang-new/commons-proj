/**
 * @Title: Utils.java
 * @Package:com.lishiliang.store.mgr.web.util
 * @desc: TODO
 * @author: lisl
 * @date
 */

package com.lishiliang.web.exception.util;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @desc: 工具类
 * @author: lisl
 * @date
 */

public class Utils {
    
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    
    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * 
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * 
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     * 
     * 用户真实IP为： 192.168.1.110
     * 
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
    
//        Enumeration<String> headers = request.getHeaderNames();
//        while(headers.hasMoreElements()){
//            String headerName = headers.nextElement();
//            Enumeration<String> headerValues = request.getHeaders(headerName);
//            while(headerValues.hasMoreElements()){
//                logger.info("请求Header：{}，值：{}", headerName, headerValues.nextElement());
//            }
//        }
        
        //Squid 服务代理
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            //Proxy-Client-IP：apache 服务代理
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            //X-Real-IP：nginx服务代理
            ip = request.getHeader("X-Real-IP");
        }
        //还是没有的情况下
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }   
        
        if(StringUtils.isNotBlank(ip) && ip.length() > 6){//ip地址最少的是7位
            if(ip.indexOf(",") >= 0){
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        
        logger.info("获取客户端IP：{}", ip);
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
    
    /**
     * @desc: 文件下载
     * @param file
     * @return
     * @throws IOException
     */
    public static ResponseEntity<byte[]> buildResponseEntity(File file) throws IOException {
        //读取文件
        byte[] body = FileUtils.readFileToByteArray(file);
        
        HttpHeaders headers = new HttpHeaders();
        //设置文件类型
        headers.add("Content-Disposition", "attchement;filename=" + file.getName());
        //设置Http状态码
        HttpStatus statusCode = HttpStatus.OK;
        //返回数据
        return new ResponseEntity<>(body, headers, statusCode);
    }
}

package com.lishiliang.web.exception.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lishiliang.core.exception.BusinessRuntimeException;
import com.lishiliang.core.utils.Constant;
import com.lishiliang.core.utils.Context;
import com.lishiliang.web.exception.util.Utils;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;


@Controller
@RequestMapping("/defaultError")
public class DefaultErrorController extends AbstractErrorController {
    private static final Logger logger = LoggerFactory.getLogger(DefaultErrorController.class);
    
    public static final String ERROR_PATH = "error";
    
    public DefaultErrorController(ErrorAttributes errorAttributes) {
    
        super(errorAttributes);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @return
     * 
     * @see org.springframework.boot.web.servlet.error.ErrorController#getErrorPath()
     */
    @Override
    public String getErrorPath() {
    
        return ERROR_PATH;
    }
    
    /**
     * @desc: 页面同步请求出现异常时，拦截处理该类请求
     * @param request
     * @param response
     * @param modelMap
     * @return
     * @throws IOException 
     */
    @RequestMapping(produces = "text/html")
    public String errorHtml(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws IOException {
        
        //获取用户登录信息
        Exception exception = (Exception) request.getAttribute("exception");
        Object sessionObject = request.getSession().getAttribute(request.getSession().getId());
        String reqUrl = (String) request.getAttribute("url");
        String reqUri = (String) request.getAttribute("uri");
        
        logger.info("页面同步统一异常处理, 请求IP：{}", Utils.getIpAddress(request));
        logger.info("页面同步统一异常处理, 请求URL：{}", reqUrl);
        
        //若是访问API接口时，出现异常，全部以Json格式数据返回请求方
        Pattern p = Pattern.compile("^" + "/api/*");
        Matcher m = p.matcher(reqUri);
        if (m.find()) {
            responseErrorJson(exception, response);
            return null;
        }


        //以普通模式返回请求方
        modelMap.addAttribute(Context.TRACE_ID, Context.getCurrentContextTraceId());
        if(exception instanceof BusinessRuntimeException){
            modelMap.addAttribute(Constant.RETURN_CODE, ((BusinessRuntimeException)exception).getErrCode());
            modelMap.addAttribute(Constant.RETURN_MSG, ((BusinessRuntimeException)exception).getErrMsg());
        }else{
            modelMap.addAttribute(Constant.RETURN_CODE, Constant.FAIL_CODE);
            modelMap.addAttribute(Constant.RETURN_MSG, "系统异常，请联系管理员！");
        }
        
        HttpStatus httpStatus = getStatus(request);
        response.setStatus(httpStatus.value());
        if(sessionObject == null) {
            //若用户未登录
            switch (httpStatus) {
                case NOT_FOUND:
                    break;
                case INTERNAL_SERVER_ERROR:
                    break;
                default:
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
            }
        }else {
            
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        
        return Integer.toString(httpStatus.value());
    }
    
    /**
     * @author lisl
     * @desc 需要以json返回给请求方时
     * @param exception
     * @param response
     * @throws IOException 
     */
    private void responseErrorJson(Exception exception, HttpServletResponse response) throws IOException {
        
        Map<String, Object> body = new HashMap<>();
        body.put(Context.TRACE_ID, Context.getCurrentContextTraceId());

        if(exception instanceof BusinessRuntimeException){
            body.put(Constant.RETURN_CODE, ((BusinessRuntimeException)exception).getErrCode());
            body.put(Constant.RETURN_MSG, ((BusinessRuntimeException)exception).getErrMsg());
        }else{
            body.put(Constant.RETURN_CODE, Constant.FAIL_CODE);
            body.put(Constant.RETURN_MSG, "系统异常，请联系管理员！");
        }
        
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(body));
    }

    /**
     * @desc: 页面Ajax请求异常时，拦截处理此类请求
     * @param request
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping(headers = { "X-Requested-With=XMLHttpRequest" })
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request, ModelMap modelMap) throws Exception {
        
        String reqUrl = (String) request.getAttribute("url");
        
        logger.info("异步请求统一异常处理, 请求IP：{}", Utils.getIpAddress(request));
        logger.info("异步请求统一异常处理, 请求URL：{}", reqUrl);
        
        Map<String, Object> body = new HashMap<>();
        body.put(Context.TRACE_ID, Context.getCurrentContextTraceId());

        HttpStatus status = getStatus(request);
        Exception exception = (Exception) request.getAttribute("exception");
        if(exception instanceof BusinessRuntimeException){
            body.put(Constant.RETURN_CODE, ((BusinessRuntimeException)exception).getErrCode());
            body.put(Constant.RETURN_MSG, ((BusinessRuntimeException)exception).getErrMsg());
        }else{
            body.put(Constant.RETURN_CODE, Constant.FAIL_CODE);
            body.put(Constant.RETURN_MSG, "系统异常，请联系管理员！");
        }
        
        return new ResponseEntity<>(body, status);
    }

    /**
     * @desc: feign请求异常 拦截处理该类请求 通过feignFlagIterceptor约定header为feign的则为内部feign调用 并且设置HttpStatus状态吗为异常状态吗  以便给feign客户端通过FeignErrorDecoder处理
     * (response.status() >= 200 && response.status() < 300) == false 则会进入ErrorDecoder  @see SynchronousMethodHandler#executeAndDecode
     * @param request
     * @param response
     * @param modelMap
     * @return
     * @throws IOException
     */
    @RequestMapping(headers = {"feign"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> errorFeign(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws IOException {

        Exception exception = (Exception) request.getAttribute("exception");
        String reqUrl = (String) request.getAttribute("url");
        String reqUri = (String) request.getAttribute("uri");

        logger.info("feign内部调用统一异常处理, 请求IP：{}", Utils.getIpAddress(request));
        logger.info("feign内部调用统一异常处理, 请求URL：{}", reqUrl);

        Map<String, Object> body = new HashMap<>();
        body.put(Context.TRACE_ID, Context.getCurrentContextTraceId());

        //(response.status() >= 200 && response.status() < 300) == false 则会进入ErrorDecoder  @see SynchronousMethodHandler#executeAndDecode
        //状态码必须设置为异常状态码 否则下游服务捕获不到
        HttpStatus status = getStatus(request);
        if (!status.isError()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if(exception instanceof BusinessRuntimeException){
            body.put(Constant.RETURN_CODE, ((BusinessRuntimeException)exception).getErrCode());
            body.put(Constant.RETURN_MSG, ((BusinessRuntimeException)exception).getErrMsg());
        }else{
            body.put(Constant.RETURN_CODE, Constant.FAIL_CODE);
            body.put(Constant.RETURN_MSG, "系统内部调用异常，请联系管理员！");
        }

        return new ResponseEntity<>(body, status);
    }
}

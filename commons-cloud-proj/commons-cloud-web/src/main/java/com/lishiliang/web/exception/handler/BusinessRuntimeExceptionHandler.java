
 /**
 * @Title: BusinessRuntimeExceptionHandler.java
 * @desc: TODO
 * @author: lisl
 */

package com.lishiliang.web.exception.handler;


 import com.lishiliang.core.exception.BusinessRuntimeException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;

 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;




/**
 * @desc: 公共异常统一处理类
 * @author: lisl
 */
@ControllerAdvice
public class BusinessRuntimeExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(BusinessRuntimeExceptionHandler.class);

    public static final String DEFAULT_ERROR_VIEW = "forward:/defaultError";


    /**
     * @desc: 通用异常处理
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e, HttpServletResponse res) {

        logger.error("默认异常处理：" + e.getMessage(), e);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", req.getRequestURL().toString());
        mav.addObject("uri", req.getRequestURI());
        mav.setViewName(DEFAULT_ERROR_VIEW);

        return mav;
    }
    /**
     * @desc: 业务异常处理
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BusinessRuntimeException.class)
    public ModelAndView serviceBusinessErrorHtmlHandler(HttpServletRequest req, BusinessRuntimeException e) {

        logger.error("自定义异常处理：" + e.getMessage(), e);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.addObject("url", req.getRequestURL().toString());
        mav.addObject("uri", req.getRequestURI());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }



}


 /**
 * @Title: BusinessRuntimeExceptionHandler.java 
 * @Package:com.superq.framework.core.exception   
 * @desc: TODO  
 * @author: lisl
 * @date:2018年4月9日 下午8:34:52    
 */
 
package com.lishiliang.web.exception.handler;


 import com.lishiliang.core.exception.BusinessRuntimeException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.servlet.ModelAndView;

 import javax.servlet.http.HttpServletRequest;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.UndeclaredThrowableException;



 
/**    
 * @desc: 公共异常统一处理类
 * @author: lisl
 * @date:2018年4月9日 下午8:34:52  
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
    @ExceptionHandler(value = UndeclaredThrowableException.class)
    public ModelAndView dubboExceptionHandler(HttpServletRequest req, UndeclaredThrowableException e) {
        
        ModelAndView mav = new ModelAndView();
        logger.error("dubbo异常处理：" + e.getMessage(), e);
        if(e.getUndeclaredThrowable() instanceof InvocationTargetException){
            InvocationTargetException ite = (InvocationTargetException) e.getUndeclaredThrowable();
            logger.error("dubbo异常处理：" + ite.getTargetException().getMessage(), ite.getTargetException());
            
            mav.addObject("exception", ite.getTargetException());
        }else{
            
            mav.addObject("exception", e);
        }
        
        mav.addObject("url", req.getRequestURL().toString());
        mav.addObject("uri", req.getRequestURI());
        mav.setViewName(DEFAULT_ERROR_VIEW);
        return mav;
    }
    
    /**
     * @desc: 通用异常处理
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) {
        
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

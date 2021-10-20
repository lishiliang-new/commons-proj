
 /**
 * @Title: CustomerDateParamHandler.java 
 * @Package:com.lishiliang.framework.dubbo.web.exception.handler
 * @desc: TODO  
 * @author: lisl    
 * @date:2018年12月19日 上午11:42:58    
 */
 
package com.lishiliang.web.exception.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;


 
/**    
 * @desc: 解决SpringMVC Date类型数据绑定参数时的异常（含日期格式、前端输入为空）
 * @author: lisl  
 * @date:2018年12月19日 上午11:42:58  
 */
@ControllerAdvice
public class CustomerDateParamHandler {
    
//    See: https://blog.csdn.net/atgeretg/article/details/79400857
//    不采用下面的方式，避免因前台传入的日期格式不同导致参数绑定异常！
//    @InitBinder
//    protected void initBinder(WebDataBinder binder) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat();
//        
//        CustomDateEditor editor = new CustomDateEditor(dateFormat, true);
//        binder.registerCustomEditor(Date.class, editor);
//    }
}

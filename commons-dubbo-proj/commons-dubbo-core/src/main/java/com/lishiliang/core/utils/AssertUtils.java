
 /**
 * @Title: AssertUtils.java 
 * @Package:com.lishiliang.store.mgr.web.util
 * @desc: TODO  
 * @author: lisl
 * @date:2018年4月3日 下午3:54:07    
 */

 package com.lishiliang.core.utils;


 import com.lishiliang.core.exception.BusinessRuntimeException;
 import org.apache.commons.lang3.StringUtils;

 public class AssertUtils {
    
    /**
     * @desc: 判断必须相等
     * @param left
     * @param right
     * @param errorCode
     * @param errorMsg
     */
    public static void isEqual(String left, String right, String errorCode, String errorMsg){
        if(!left.equals(right)){
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
    
    /**
     * @desc: 判断必须相等
     * @param left
     * @param right
     * @param errorCode
     * @param errorMsg
     */
    public static void isEqual(int left, int right, String errorCode, String errorMsg){
        if(left != right){
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
    
    /**
     * @desc: 判断参数不能为空
     * @param obj
     */
    public static void isNotNull(Object obj){
        isNotNull(obj, ErrorCodes.ERR_PARAM.getCode(), ErrorCodes.ERR_PARAM.getDesc());
    }
    
    /**
     * @desc: 判断参数不能为空
     * @param obj
     * @param errorCode
     * @param errorMsg
     */
    public static void isNotNull(Object obj, String errorCode, String errorMsg){
        if(obj == null){
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
    
    /**
     * @desc: 判断参数必须为空
     * @param obj
     * @param errorCode
     * @param errorMsg
     */
    public static void isNull(Object obj, String errorCode, String errorMsg){
        if(obj != null){
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
    
    /**
     * @desc: 判断参数不能为空
     * @param obj
     */
    public static void isNotBlank(String obj){
        isNotBlank(obj, ErrorCodes.ERR_PARAM.getCode(), ErrorCodes.ERR_PARAM.getDesc());
    }
    
    /**
     * @desc: 判断参数不能为空
     * @param obj
     * @param errorCode
     * @param errorMsg
     */
    public static void isNotBlank(String obj, String errorCode, String errorMsg){
        if(StringUtils.isEmpty(StringUtils.trim(obj))){
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
    
    /**
     * @desc: 判断参数不能为空，并且长度必须在范围内
     * @param obj
     * @param errorCode
     * @param errorMsg
     */
    public static void isNotBlank(String obj, int minLen, int maxLen, String errorCode, String errorMsg){
        if(StringUtils.isEmpty(StringUtils.trim(obj))){
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
        
        if (obj.length() < minLen || obj.length() > maxLen) {
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
    /**
     * 判断某值必须在二个区间之间
     * @param field
     * @param value
     * @param min
     * @param max
     */
    public static void isBetween(int value, String field, int min, int max, String errorCode)
    {
        if (value < min || value > max)
        {
            throw new BusinessRuntimeException(errorCode, String.format("参数[%s]错误", field));
        }
    }
    
    /**
     * 判断某值必须在二个区间之间
     * @param field
     * @param value
     * @param min
     * @param max
     */
    public static void isBetween(long value, String field, int min, long max, String errorCode)
    {
        if (value < min || value > max)
        {
            throw new BusinessRuntimeException(errorCode, String.format("参数[%s]错误", field));
        }
    }
    
    /**
     * @desc: 检查list中是否包含obj，不包含，抛出异常
     * @param obj
     * @param list
     * @param errorCode
     * @param errorMsg
     */
    public static void inStringArrays(String obj, String[] list, String errorCode, String errorMsg) {
        
        for (String object : list) {
            if (object.equals(obj)) {
                return;
            }
        }
        throw new BusinessRuntimeException(errorCode, errorMsg);
    }
    /**
     * @desc: 判断是否是数字
     * @param input
     * @param errorCode
     * @param errorMsg
     */
    public static void isNumber(String input, String errorCode, String errorMsg) {
        
        if (!Regex.matchNumber(input)) {
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }

    /**
     * 断言为true
     * @param condition
     * @param errorCode
     * @param errorMsg
     */
    public static void isTrue(boolean condition, String errorCode, String errorMsg) {

        if (!condition) {
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }

    /**
     * 断言不为true
     * @param condition
     * @param errorCode
     * @param errorMsg
     */
    public static void isNotTrue(boolean condition, String errorCode, String errorMsg) {

        if (!condition) {
            throw new BusinessRuntimeException(errorCode, errorMsg);
        }
    }
}

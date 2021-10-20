package com.lishiliang.core.utils;

/**
 * 常用正则表达式
 * @author  lisl
 * @version  [1.0.0, 2016-10-22]
 * @since  JDK 1.7
 * @see
 */
public class Regex {
    private Regex() {
    
    }
	

    /** cron表达式正则 */
    public static final String CRON = "(((^([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|^([0-9]|[0-5][0-9]) |^(\\* ))((([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|([0-9]|[0-5][0-9]) |(\\* ))((([0-9]|[01][0-9]|2[0-3])(\\,|\\-|\\/){1}([0-9]|[01][0-9]|2[0-3]) )|([0-9]|[01][0-9]|2[0-3]) |(\\* ))((([0-9]|[0-2][0-9]|3[01])(\\,|\\-|\\/){1}([0-9]|[0-2][0-9]|3[01]) )|(([0-9]|[0-2][0-9]|3[01]) )|(\\? )|(\\* )|(([1-9]|[0-2][0-9]|3[01])L )|([1-7]W )|(LW )|([1-7]\\#[1-4] ))((([1-9]|0[1-9]|1[0-2])(\\,|\\-|\\/){1}([1-9]|0[1-9]|1[0-2]) )|([1-9]|0[1-9]|1[0-2]) |(\\* ))(([1-7](\\,|\\-|\\/){1}[1-7])|([1-7])|(\\?)|(\\*)|(([1-7]L)|([1-7]\\#[1-4]))))|(((^([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|^([0-9]|[0-5][0-9]) |^(\\* ))((([0-9]|[0-5][0-9])(\\,|\\-|\\/){1}([0-9]|[0-5][0-9]) )|([0-9]|[0-5][0-9]) |(\\* ))((([0-9]|[01][0-9]|2[0-3])(\\,|\\-|\\/){1}([0-9]|[01][0-9]|2[0-3]) )|([0-9]|[01][0-9]|2[0-3]) |(\\* ))((([0-9]|[0-2][0-9]|3[01])(\\,|\\-|\\/){1}([0-9]|[0-2][0-9]|3[01]) )|(([0-9]|[0-2][0-9]|3[01]) )|(\\? )|(\\* )|(([1-9]|[0-2][0-9]|3[01])L )|([1-7]W )|(LW )|([1-7]\\#[1-4] ))((([1-9]|0[1-9]|1[0-2])(\\,|\\-|\\/){1}([1-9]|0[1-9]|1[0-2]) )|([1-9]|0[1-9]|1[0-2]) |(\\* ))(([1-7](\\,|\\-|\\/){1}[1-7] )|([1-7] )|(\\? )|(\\* )|(([1-7]L )|([1-7]\\#[1-4]) ))((19[789][0-9]|20[0-9][0-9])\\-(19[789][0-9]|20[0-9][0-9])))";

    /**
     * 验证整数
     * @param a
     * @return
     */
    public static boolean matchNumber(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^[0-9]|([1-9]\\d*)$";
            return a.matches(expression);
        }
    }
    
    /**
     * 验证字母
     * @param a
     * @return
     */
    public static boolean matchLetter(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^[A-Za-z]+$";
            return a.matches(expression);
        }
    }
    
    /**
     * 验证大写字母
     * @param a
     * @return
     */
    public static boolean matchUppserLetter(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^[A-Z]+$";
            return a.matches(expression);
        }
    }
    
    /**
     * 验证小写字母
     * @param a
     * @return
     */
    public static boolean matchLowerLetter(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^[a-z]+$";
            return a.matches(expression);
        }
    }
    
    /**
     * 匹配字母和数字和下划线，以字母开头
     * @param a
     * @return
     */
    public static boolean matchLetterNumber(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^[A-Za-z]+[A-Za-z0-9_]*$";
            return a.matches(expression);
        }
    }
    /**
     * 匹配邮箱
     * @param a
     * @return
     */
    public static boolean matchLetterNumber2(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^\\w+((_\\w+)|(\\.))*+$";
            return a.matches(expression);
        }
    }
    
    
    /**
     * 对日期的简单校验
     * @param a
     * @return
     */
    public static boolean matchDate(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^[2-2][0-9]{3}(0[1-9]|1[0-2])" + "(0[0-9]|[12][0-9]|3[01])";
            return a.matches(expression);
        }
    }
    
    /**
     * 验证邮箱
     * @param a
     * @return
     */
    public static boolean matchEmail(String a) {
        if(a == null) {
            return false;
        } else {
            String expression = "^\\w+([-\\.]\\w+)*@\\w+([-\\.]\\w+)*\\.\\w+([-\\.]\\w+)*$";
            return a.matches(expression);
        }
    }


    /**
     * 验证是否匹配正则
     * @param regex
     * @param str
     * @return
     */
    public static boolean matchRegex(String regex, String str) {

        return str.matches(regex);
    }

    
}

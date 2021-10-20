
package com.lishiliang.core.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Utils {

    //去空 去null splitter
    public static final Splitter splitter = Splitter.on(",").trimResults().omitEmptyStrings();
    //去null joiner
    public static final Joiner joiner = Joiner.on(",").skipNulls();

    private static Pattern linePattern = Pattern.compile("_(\\w)");
    private static Pattern humpPattern = Pattern.compile("[A-Z]");
    
    /**
     * @desc: 下划线转驼峰
     * @param str
     * @return
     */
    public static String lineToHump(String str) {
    
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 驼峰转下划线,效率高 
     **/
    public static String humpToLine(String str) {
    
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 首字母转小写
     **/
    public static String toLowerCaseFirstChar(String str) {
    
        if(Character.isLowerCase(str.charAt(0))) {
            return str;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();
        }
    }
    
    /**
     * @desc 获取UUID
     * @return
     */
    public static String generateUUIDWithMD5() {
        
        return DigestUtils.md5Hex(UUID.randomUUID().toString().replaceAll("-", ""));
    }
    
    /**
     * 超过 maxSize 的部分用省略号代替
     * <p>
     * 使用范例：
     * 1 不超过取所有
     * StringUtil.abbreviate("123456789", 11) = "123456789"
     * <p>
     * 2 超过最大长度截取并补充省略号
     * StringUtil.abbreviate("123456789", 3) = "123..."
     * <p>
     * 3 emoji表情被截断则丢弃前面的字符（整个表情）
     * StringUtil.abbreviate("123456789??", 10) = "123456789..."
     *
     * @param originStr 原始字符串
     * @param maxSize   最大长度
     */
    public static String abbreviate(String originStr, int maxSize) {
 
        return abbreviate(originStr, maxSize, null);
    }
 
    /**
     * 超过 maxSize 的部分用省略号代替
     * <p>
     * 使用范例：
     * <p>
     * StringUtil.abbreviate("123456789"", 3, "***") = "123..."
     *
     * @param originStr    原始字符串
     * @param maxSize      最大长度
     * @param abbrevMarker 省略符
     */
    public static String abbreviate(String originStr, int maxSize, String abbrevMarker) {
 
        AssertUtils.isBetween(maxSize, "maxSize", 0, Integer.MAX_VALUE, ErrorCodes.ERR_PARAM.getCode());
 
        if (StringUtils.isEmpty(originStr)) {
            return StringUtils.EMPTY;
        }
 
        String defaultAbbrevMarker = "...";
 
        if (originStr.length() < maxSize) {
            return originStr;
        }
 
        // 截取前maxSize 个字符
        String head = originStr.substring(0, maxSize);
 
        // 最后一个字符是高代理项，则移除掉
        char lastChar = head.charAt(head.length() - 1);
        if (Character.isHighSurrogate(lastChar)) {
            head = head.substring(0, head.length() - 1);
        }
 
        return head + StringUtils.defaultIfEmpty(abbrevMarker, defaultAbbrevMarker);
    }
    /**
     * @desc 按指定起始日期和结束日期，计算在这段时间范围内的周数据
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param pattern 日期的格式
     * @return
     */
    public static List<String[]> getWeeklyByMonth(String beginDate, String endDate, String pattern){
        
        DateTime beginDateTime = DateTime.parse(beginDate, DateTimeFormat.forPattern(pattern));
        DateTime endDateTime = DateTime.parse(endDate, DateTimeFormat.forPattern(pattern));
        
        //查询时间范围内的周的第一天
        DateTime beginWeekFirstTime = beginDateTime.dayOfWeek().withMinimumValue();
        //查询时间范围内的周的最后一天
        DateTime endWeekLastTime = endDateTime.dayOfWeek().withMaximumValue();
        
        List<String[]> weeklyDateList = Lists.newArrayList();
        while(beginWeekFirstTime.isBefore(endWeekLastTime)) {
            
            weeklyDateList.add(new String[] {beginWeekFirstTime.toString(pattern), beginWeekFirstTime.dayOfWeek().withMaximumValue().toString(pattern)});
            beginWeekFirstTime = beginWeekFirstTime.plusDays(7);
        }
        
        return weeklyDateList;
    }

    /**
     *  根据时间段，分割成 当年的第一月和当月时间
     * @param beginDate 需要分割的其实时间
     * @param endDate 需要分割的结束时间
     * @param pattern 时间格式 yyyyMM
     * @return 返回一组按月分割的时间段
     */
    public static List<String[]> getMonthByYear(String beginDate, String endDate, String pattern){

        DateTime beginDateTime = DateTime.parse(beginDate, DateTimeFormat.forPattern(pattern));
        DateTime endDateTime = DateTime.parse(endDate, DateTimeFormat.forPattern(pattern)).plusMonths(1);

        List<String[]> monthDateList = Lists.newArrayList();
        while(beginDateTime.isBefore(endDateTime)) {

            DateTime beginFirstMonthOfYear = beginDateTime.minusMonths(beginDateTime.getMonthOfYear()-1);
            monthDateList.add(new String[] {beginFirstMonthOfYear.toString(Constant.DATE_TIME_YYYYMM), beginDateTime.toString(Constant.DATE_TIME_YYYYMM)});
            beginDateTime = beginDateTime.plusMonths(1);
        }

        return monthDateList;
    }
    
    /**

     * @param key
     * @return
     */
    public static long hash(String key) {
        
        /*
         * 实现一致性哈希算法中使用的哈希函数,使用MD5算法来保证一致性哈希的平衡性
         */
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("no md5 algrithm found");
        }
        md5.reset();
        md5.update(key.getBytes());
        byte[] bKey = md5.digest();
        // 具体的哈希函数实现细节--每个字节 & 0xFF 再移位
        long result = ((long) (bKey[3] & 0xFF) << 24)
            | ((long) (bKey[2] & 0xFF) << 16 | ((long) (bKey[1] & 0xFF) << 8) | (long) (bKey[0] & 0xFF));
        return result & 0xffffffffL;
    }
    
    public static void main(String[] args) {
    
        System.out.println(abbreviate("艾迪康静安寺肯德基阿克苏贷记卡暑假打算看到就", 5));
        System.out.println(DateTime.parse("2020-06-05", DateTimeFormat.forPattern("yyyy-MM-dd")).millisOfDay().withMaximumValue().toString("yyyy-MM-dd HH:mm:ss"));
        
        DateTime beginDate = DateTime.parse("20200501", DateTimeFormat.forPattern("yyyyMMdd"));
        DateTime endDate = DateTime.parse("20200531", DateTimeFormat.forPattern("yyyyMMdd"));
        
        System.out.println(beginDate.dayOfWeek().withMinimumValue().toString("yyyyMMdd"));
        System.out.println(beginDate.dayOfWeek().withMaximumValue().toString("yyyyMMdd"));
        
        List<String[]> weeklyDateList = getWeeklyByMonth("20200501", "20200630", "yyyyMMdd");
        weeklyDateList.forEach(data -> {
            System.out.println(data[0] + "~" + data[1]);
        });
    }
}

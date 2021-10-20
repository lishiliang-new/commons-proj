package com.lishiliang.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author lisl
 * @version 1.0
 * @date 2021-9-26 16:44
 * @desc :
 */
public class DateMapUtils {


    /**
     *
     * @param beiginDate
     * @param endDate
     * @param period 周期
     * @param supplier 填充map的对象
     * @return
     */
    public static <T> Map<String, T> createDateMap(String beiginDate, String endDate, String period, Supplier<T> supplier) {

        Map<String, T> dateMap = new LinkedHashMap<>();

        //补齐时间8位
        beiginDate = StringUtils.rightPad(beiginDate, 8, "01");
        endDate = StringUtils.rightPad(endDate, 8, "01");

        String pattern = null;
        DateTime beiginTime = null;
        DateTime endTime = null;
        switch (period.toLowerCase()) {
            case "day":
                pattern = "yyyyMMdd";
                beiginTime = DateTime.parse(beiginDate, DateTimeFormat.forPattern(pattern));
                endTime = DateTime.parse(endDate, DateTimeFormat.forPattern(pattern));
                initDateMap(beiginTime, endTime, pattern, (DateTime date)->date.plusDays(1), supplier, dateMap);
                break;
            case "month":
                pattern = "yyyyMM";
                beiginDate = DateTime.parse(beiginDate, DateTimeFormat.forPattern("yyyyMMdd")).toString(pattern);
                endDate = DateTime.parse(endDate, DateTimeFormat.forPattern("yyyyMMdd")).toString(pattern);
                beiginTime = DateTime.parse(beiginDate, DateTimeFormat.forPattern(pattern));
                endTime = DateTime.parse(endDate, DateTimeFormat.forPattern(pattern));
                initDateMap(beiginTime, endTime, pattern, (DateTime date)->date.plusMonths(1), supplier, dateMap);
                break;
            case "year":
                pattern = "yyyy";
                beiginDate = DateTime.parse(beiginDate, DateTimeFormat.forPattern("yyyyMMdd")).toString(pattern);
                endDate = DateTime.parse(endDate, DateTimeFormat.forPattern("yyyyMMdd")).toString(pattern);
                beiginTime = DateTime.parse(beiginDate, DateTimeFormat.forPattern(pattern));
                endTime = DateTime.parse(endDate, DateTimeFormat.forPattern(pattern));
                initDateMap(beiginTime, endTime, pattern, (DateTime date)->date.plusYears(1), supplier, dateMap);
                break;
            default:
                break;
        }
        return dateMap;
    }

    /**
     * @param beginTime 起始日期
     * @param endTime 结束日期
     * @param pattern pattern
     * @param dateMap 存储map
     * @param action 计算下一次的时间
     */
    private static <T> void initDateMap(DateTime beginTime, DateTime endTime, String pattern, Function<DateTime, DateTime> action, Supplier<T> supplier, Map<String, T> dateMap) {
        while (!beginTime.isAfter(endTime)) {
            dateMap.put(beginTime.toString(pattern), supplier.get());
            beginTime = action.apply(beginTime);
        }
    }

    public static void main(String[] args) {

        Map<String, Object> dateMap1 = createDateMap("2021", "2025", "year", Object::new);
        Map<String, String> dateMap11 = createDateMap("202101", "202501", "YEAR", String::new);
        Map<String, Object> dateMap12 = createDateMap("20210101", "20250101", "year", Object::new);
        Map<String, DateTime> dateMap2 = createDateMap("202101", "202505", "month", DateTime::new);
        Map<String, Object> dateMap3 = createDateMap("20210101", "20250501", "day", Object::new);
        System.out.println(dateMap1);
        System.out.println(dateMap11);
        System.out.println(dateMap12);
        System.out.println(dateMap2);
        System.out.println(dateMap3);
    }
}

package com.mg.core.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.management.ManagementFactory;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * packageName com.mg.core.utils
 *
 * @author mj
 * @className DateUtil
 * @date 2025/5/27
 * @description TODO
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

    public static final String YYYY = "yyyy";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final List<String> PARSE_PATTERNS = Arrays.asList(
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM");

    /**
     * 获取当前LocalDateTime
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期字符串，默认格式为yyyy-MM-dd
     */
    public static String getDate() {
        return formatNow(YYYY_MM_DD);
    }

    /**
     * 获取当前日期时间字符串，格式为yyyy-MM-dd HH:mm:ss
     */
    public static String getTime() {
        return formatNow(YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 格式化当前时间为指定格式
     */
    public static String formatNow(final String format) {
        return format(LocalDateTime.now(), format);
    }

    /**
     * 格式化LocalDateTime为字符串
     */
    public static String format(final LocalDateTime dateTime, final String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 解析字符串为LocalDateTime
     */
    public static LocalDateTime parse(final String format, final String dateStr) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static String datePath() {
        return formatNow("yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月 如2018-08
     */
    public static String dateMonth() {
        return formatNow("yyyy-MM");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static String dateTime() {
        return formatNow("yyyyMMdd");
    }

    /**
     * 获取服务器启动时间
     */
    public static LocalDateTime getServerStartTime() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 计算相差天数
     */
    public static int differentDays(LocalDateTime date1, LocalDateTime date2) {
        return (int) ChronoUnit.DAYS.between(
                date1.toLocalDate(),
                date2.toLocalDate());
    }

    /**
     * 计算两个时间差
     */
    public static String getTimeDifference(LocalDateTime endTime, LocalDateTime startTime) {
        Duration duration = Duration.between(startTime, endTime);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        return days + "天" + hours + "小时" + minutes + "分钟";
    }

    /**
     * 计算两个日期相差的月数
     */
    public static long monthsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return ChronoUnit.MONTHS.between(
                startDate.toLocalDate(),
                endDate.toLocalDate());
    }

    /**
     * Date 转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate 转 Date
     */
    public static Date toDate(LocalDate localDate) {
        return toDate(localDate.atStartOfDay());
    }

    /**
     * 获取当前日期的指定天数前的日期
     */
    public static String getDaysAgo(int daysAgo) {
        LocalDateTime dateTimeAgo = LocalDateTime.now().minusDays(daysAgo);
        return format(dateTimeAgo, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 格式化ISO 8601时间字符串为指定格式
     */
    public static String formatIsoDateTime(String isoDateTime) {
        OffsetDateTime odt = OffsetDateTime.parse(isoDateTime);
        return format(odt.toLocalDateTime(), YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当天剩余秒数
     */
    public static int secondsUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
        return (int) Duration.between(now, endOfDay).getSeconds();
    }

    /**
     * 尝试多种格式解析日期字符串
     */
    public static LocalDateTime parseFlexible(String dateStr) {
        for (String pattern : PARSE_PATTERNS) {
            try {
                if (pattern.length() == dateStr.length()) {
                    return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
                }
            } catch (Exception ignored) {
                // 尝试下一个格式
            }
        }
        throw new IllegalArgumentException("无法解析日期字符串: " + dateStr);
    }

    /**
     * 根据舆情发布时间范围标识获取对应的时间范围
     * 0:近一小时  1:今天  2:近24小时 3:近七天 4:自定义  5:近三天  6:近三十天 7:近一年 8:近10分钟
     *
     * @param timeRangeFlag 时间范围标识符
     * @return 包含开始时间和结束时间的映射
     */
    public static Map<String, LocalDateTime> getTimeRageByRageFlag(String timeRangeFlag) {
        final LocalDateTime endDateTime = getNow();
        LocalDateTime startDateTime = switch (timeRangeFlag) {
            case "0" -> endDateTime.minusHours(1);       // 近一小时
            case "1" -> endDateTime.toLocalDate().atStartOfDay();  // 今天
            case "2" -> endDateTime.minusDays(1);        // 近24小时
            case "3" -> endDateTime.minusDays(7);        // 近七天
            case "5" -> endDateTime.minusDays(3);        // 近三天
            case "6" -> endDateTime.minusDays(30);      // 近三十天
            case "7" -> endDateTime.minusYears(1);       // 近一年
            case "8" -> endDateTime.minusMinutes(10);    // 近10分钟
            case "9" -> endDateTime.minusMonths(3);      // 近三个月
            case "10" -> endDateTime.minusMonths(6);     // 近半年
            default -> throw new IllegalArgumentException("Invalid time range flag: " + timeRangeFlag);
        };
        return Map.of("start", startDateTime, "end", endDateTime);
    }


    /**
     * 安全格式化日期时间
     *
     * @param dateTime
     * @param pattern
     * @param defaultValue
     * @return
     */
    public static String safeFormat(LocalDateTime dateTime, String pattern, String defaultValue) {
        if (dateTime == null) {
            return defaultValue;
        }
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }
}
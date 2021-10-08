/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @author zane.luo
 */
public class DateUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);
    public final static long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    public final static long MONTH_IN_SECOND = 30 * 24 * 60 * 60;
    public final static long HOUR_IN_MILLIS = 60 * 60 * 1000;
    public final static String YYYY_MM_DD = "yyyy-MM-dd";
    public final static String YYYY_MM = "yyyy-MM";
    public final static String YYYYMM = "yyyyMM";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd.HH.mm.ss";
    public static final String YYYY_MM_DD_HH_MM_SS2 = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";
    public static final String Y4_M2_D2_H2_M2_S2 = "yyyyMMddHHmmss";
    public static final String Y4_M2_D2_H2 = "yyyyMMddHH";
    public static final String YYYY_MM_DDTHH_MM_SS_SSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String Y4_M2_D2 = "yyyyMMdd";

    /**
     * Returns the hour in which the specified time string is placed
     */
    public static int getHour(Long timeInMillis) {
        Date date = new Date(timeInMillis);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Returns the day of the week of the specified time string
     */
    public static int getDay(Long timeInMillis) {
        Date date = new Date(timeInMillis);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Parameters are in no order
     *
     * @return The number of seconds between two times
     */
    public static Long intervalSeconds(long timeInMillis1, long timeInMillis2) {
        return Math.abs(timeInMillis1 - timeInMillis2) / 1000;
    }

    /**
     * Parameters are in no order
     *
     * @return The number of days between two times
     */
    public static Long intervalDays(long timeInMillis1, long timeInMillis2) {
        return intervalDays(timeInMillis1, timeInMillis2, true);
    }

    /**
     * Parameters are in no order
     *
     * @param timeInMillis1 max value
     * @param timeInMillis2 Small values
     * @param isAbs         Absolute value (true: yes,false: no)
     * @return The number of days between two times
     */
    public static Long intervalDays(long timeInMillis1, long timeInMillis2, boolean isAbs) {
        LocalDateTime time1 = LocalDateTime.ofEpochSecond(timeInMillis1 / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime time2 = LocalDateTime.ofEpochSecond(timeInMillis2 / 1000, 0, ZoneOffset.ofHours(8));
        return isAbs ? Duration.between(time1, time2).abs().toDays() : Duration.between(time2, time1).toDays();
    }

    /**
     * Parameters are in no order
     * Parameter format yyyy-MM-dd
     *
     * @return The number of days between two times
     */
    public static Long intervalDays(String timeString1, String timeString2) {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        try {
            return intervalDays(sdf.parse(timeString1).getTime(), sdf.parse(timeString2).getTime());
        } catch (Exception e) {
        }
        return 0L;
    }

    /**
     * Parameters are in no order
     *
     * @return The number of months that differ in time
     */
    public static long intervalMonths(long timeInMillis1, long timeInMillis2) {
        return intervalDays(timeInMillis1, timeInMillis2) / 30;
    }

    /**
     * get the interval days between given time in milliseconds and now.
     *
     * @return Long
     */
    public static Long intervalDaysUntilNow(long currentTime, long timeInMillis) {
        long days = intervalDays(currentTime, timeInMillis);
        return days;
    }

    public static Long intervalDaysUntilNow(String date) {

        if (StringUtils.isEmpty(date)) {
            return -1L;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        long days = 0;
        try {
            days = intervalDays(System.currentTimeMillis(), sdf.parse(date).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    /**
     * check if the time is in max days.
     *
     * @return boolean
     */
    public static boolean isWithinSpecifiedTime(long currentTime, long timeInMillis, long maxDays) {
        Long days = intervalDaysUntilNow(currentTime, timeInMillis);
        return days <= maxDays;
    }

    /**
     * check if the time is in max months.
     *
     * @return boolean
     */
    public static boolean isWithinSpecifiedTime4Month(long currentTime, long timeInMillis, long maxMonths) {
        long months = intervalMonths(currentTime, timeInMillis);
        return months <= maxMonths;
    }

    /**
     * translate milliseconds to date.
     *
     * @return String
     */
    public static String timeInMillisToDate(Long timeInMillis) {
        return timeInMillisToDate(timeInMillis, YYYY_MM_DD);
    }

    public static String timeInMillisToDate(Long timeInMillis, String pattern) {
        Date date = new Date(timeInMillis);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static long stringToTimeInMillis(String dateStr, String pattern) {
        pattern = StringUtil.isEmpty(pattern) ? YYYY_MM_DD_HH_MM_SS2 : pattern;
        try {
            Date date = DateUtils.parseDate(dateStr, pattern);
            return date.getTime();
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
            return -1;
        }
    }

    public static String getCurrentTimeStr(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static long getTimestamp(String timestr) {
        long stamp = 0;
        long time = getTimeFromtFormat(timestr);
        if (time > 0) {
            stamp = time / 1000;
        }

        return stamp;
    }

    /**
     * Convert the time in "2015-01-14T2:11:28.914 +08:00" format to long time
     */
    public static long getTimeFromtFormat(String dateString) {
        if (!StringUtil.isEmpty(dateString)) {
            dateString = dateString.replace("+08:00", "+0800");
            try {
                Date date = DateUtils.parseDate(dateString, YYYY_MM_DDTHH_MM_SS_SSSZ, YYYY_MM_DD_HH_MM_SS2);
                return date.getTime();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return -1;
    }

    /**
     * Prints time text in the specified format
     *
     * @param pattern A format that describes the date and time formats
     */
    public static String toString(Date date, String pattern) {
        if (date == null) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }


    /**
     * Date formatting: YYYY-MM-DD
     */
    public static String toStringYYYY_MM_DD(Date date) {
        return toString(date, YYYY_MM_DD);
    }

    /**
     * Date format: YYYY-MM-DD HH: MM :ss
     */
    public static String toStringYYYY_MM_DD_HH_MM_SS2(Date date) {
        return toString(date, YYYY_MM_DD_HH_MM_SS2);
    }

    /**
     * Date formatting: YYYY-MM-DD 'HH: MM: ss.sssz
     */
    public static String toStringYYYY_MM_DDTHH_MM_SS_SSSZ(Date date) {
        return toString(date, YYYY_MM_DDTHH_MM_SS_SSSZ);
    }

    /**
     * String turn time
     */
    public static Date fromString(String dateStr, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            LOG.error("字符串转 Date 失败：" + dateStr, e);
        }
        return null;
    }

    /**
     * Calculate the start time of the month
     *
     * @return seconds
     */
    public static String getFirstSecondOfMonthString(long currentTimeInMillis, int monthsBefore, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeInMillis);
        calendar.add(Calendar.MONTH, monthsBefore);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return timeInMillisToDate(calendar.getTimeInMillis(), pattern);
    }

    /**
     * Calculate the start time of the month
     *
     * @return seconds
     */
    public static long getFirstSecondOfMonth(long currentTimeInMillis, int monthsBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeInMillis);
        calendar.add(Calendar.MONTH, monthsBefore);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis() / 1000;
    }

    public static long dateToTimeInMillis(String in, String formatString) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        Date date = format.parse(in);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getTimeInMillis();
    }

    /**
     * Gets the current date: time
     *
     * @return String
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS2);
        return sdf.format(new Date());
    }

    /**
     * Calculate the number of days between two dates
     *
     * @param date1 date1
     * @param date2 date2
     * @return int
     */
    public static int daysBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long betweenDays = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(betweenDays));
    }

    /**
     * Take time to convert into friendly and readable text
     *
     * @param spend Time (unit: ms)
     * @author Zane
     */
    public static String spend2PrettyText(long spend) {
        if (spend < 0) {
            throw new IllegalArgumentException("spend can not low then 0");
        }

        if (spend == 0) {
            return "0 ms";
        }


        long days = spend / (1000 * 60 * 60 * 24);
        spend -= days * (1000 * 60 * 60 * 24);

        long hours = spend / (1000 * 60 * 60);
        spend -= hours * (1000 * 60 * 60);

        long mins = spend / (1000 * 60);
        spend -= mins * (1000 * 60);

        long seconds = spend / 1000;

        long ms = spend - seconds * 1000;

        String result = "";
        if (days > 0) {
            result += days + "day ";
        }
        if (hours > 0) {
            result += hours + "hour ";
        }
        if (mins > 0) {
            result += mins + "min ";
        }
        if (seconds > 0) {
            result += seconds + "s ";
        }
        if (ms > 0) {
            result += ms + "ms ";
        }

        result = result.substring(0, result.length() - 1);
        return result;
    }

    /**
     * Gets the date of each day in both time ranges
     *
     * @param timeInMillis1 timeInMillis1
     * @param timeInMillis2 timeInMillis2
     */
    public static List<String> getEverydays(long timeInMillis1, long timeInMillis2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeInMillis1));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date maxDate = new Date(timeInMillis2);
        List<String> days = new ArrayList<>();
        while (calendar.getTime().equals(maxDate) || calendar.getTime().before(maxDate)) {
            days.add(toString(calendar.getTime(), YYYY_MM_DD));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    /**
     * Gets the date of the first day of each month in both time ranges
     *
     * @param timeInMillis1 timeInMillis1
     * @param timeInMillis2 timeInMillis2
     */
    public static List<String> getEveryMonths(long timeInMillis1, long timeInMillis2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeInMillis1));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date maxDate = new Date(timeInMillis2);
        List<String> days = new ArrayList<>();
        while (calendar.getTime().equals(maxDate) || calendar.getTime().before(maxDate)) {
            days.add(toString(calendar.getTime(), YYYY_MM_DD));
            calendar.add(Calendar.MONTH, 1);
        }
        return days;
    }

    private static Date getAfterDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date getAfterMonthFirstDay(Date date, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get previous day date
     */
    public static Date getLastDay(Date date) {
        return getAfterDay(date, -1);
    }

    /**
     * Gets the date of the next day
     */
    public static Date getNextDay(Date date) {
        return getAfterDay(date, 1);
    }

    /**
     * Gets the date of the first day of the previous month
     */
    public static Date getLastMonthFirstDay(Date date) {
        return getAfterMonthFirstDay(date, -1);
    }

    /**
     * Gets the date of the first day of the next month
     */
    public static Date getNextMonthFirstDay(Date date) {
        return getAfterMonthFirstDay(date, 1);
    }

    /**
     * Calculate the number of months between two dates
     *
     * @param date1 date1
     * @param date2 date2
     */
    public static int monthsBetween(Date date1, Date date2) {
        Calendar bef = Calendar.getInstance();
        Calendar aft = Calendar.getInstance();
        try {
            bef.setTime(date1);
            aft.setTime(date2);
            int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
            int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
            return month + result;
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Milliseconds convert to seconds
     */
    public static long convertSecond(long date) {
        long result = 0L;
        String dateStr = String.valueOf(date);
        if (StringUtil.isNotEmpty(dateStr)) {
            if (dateStr.length() == 13) {
                result = Long.parseLong(dateStr) / 1000;
            }
            if (dateStr.length() == 10) {
                result = date;
            }
        }
        return result;
    }

    /**
     * Milliseconds convert to seconds
     */
    public static long convertSecond(String date) {
        if (StringUtil.isEmpty(date)) {
            return 0L;
        }
        return convertSecond(Long.parseLong(date));
    }

    /**
     * Seconds convert to milliseconds
     */
    public static long convertMillisecond(long date) {
        long result = 0L;
        String dateStr = String.valueOf(date);
        if (StringUtil.isNotEmpty(dateStr)) {
            if (dateStr.length() == 13) {
                result = date;
            }
            if (dateStr.length() == 10) {
                result = Long.parseLong(dateStr) * 1000;

            }
        }
        return result;
    }

    /**
     * The long type is converted to Date
     */
    public static Date getDate(long date) {
        long time = convertMillisecond(date);
        if (time == 0L) {
            return null;
        }
        return new Date(time);
    }

    /**
     * Format Jun 1, 2016 3:5:31 PM time to pattern
     */
    public static String formatStringDate(String dateStr, String pattern) {

        if (!StringUtils.isEmpty(dateStr)) {
            try {
                Date date = DateUtils.parseDate(dateStr, Locale.US, "MMM d, yyyy h:m:s a");
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                return sdf.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Determine if the date format is correct
     */
    public static boolean isDatePattern(String dateStr, String pattern) {
        if (StringUtil.isEmpty(dateStr)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        boolean dateFlag = true;
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            dateFlag = false;
            LOG.error("dateStr:{} 不符合时间格式", dateStr);
        }
        return dateFlag;
    }

    public static String addYears(String dateStr, int y) {
        if (StringUtil.isEmpty(dateStr)) {
            return dateStr;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        try {
            Date data = sdf.parse(dateStr);
            Calendar ca = Calendar.getInstance();
            ca.setTime(data);
            ca.add(Calendar.YEAR, y);
            return sdf.format(ca.getTime());
        } catch (Throwable e) {
            LOG.info(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Add months to the current date time
     *
     * @param date   Current time date
     * @param months The number of months added
     * @return Time after increase
     */
    public static Date addMonths(Date date, long months) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return Date.from(localDateTime.plusMonths(months).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Add days to the current date
     *
     * @param date Current time date
     * @param days Days added
     * @return Time after increase
     */
    public static Date addDays(Date date, long days) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return Date.from(localDateTime.plusDays(days).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Increase the current date time by hours
     */
    public static Date addHours(Date date, long hours) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return Date.from(localDateTime.plusHours(hours).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Add minutes to the current date time
     */
    public static Date addMinutes(Date date, long minutes) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return Date.from(localDateTime.plusMinutes(minutes).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert the time in "2015-01-14T2:11:28.914 +08:00" format to long time
     * In the test phase, when I ran the application form into CASS, I found that some data of the application time were blank instead of plus sign (very strange, it was normal when the application form was run separately, and the production did not find this problem).
     */
    public static long getTimeFromtFormat2(String dateString) {
        if (!StringUtil.isEmpty(dateString)) {
            dateString = dateString.replace("+08:00", "+0800").replace(" 08:00", "+0800");
            try {
                Date date = DateUtils.parseDate(dateString, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss");
                return date.getTime();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return -1;
    }

    public static long getTimeLong(LocalDateTime time) {
        return time.toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000;
    }

    /**
     * Check whether the current time is within [startTime, endTime]
     *
     * @param nowTime   The current time
     * @param startTime
     * @param endTime
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        return date.after(begin) && date.before(end);
    }


    /**
     * The local time is changed to UTC time
     *
     * @param localDate Local time
     * @return UTC time
     */
    public static Date localToUtc(Date localDate) {
        long localTimeInMillis = localDate.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(localTimeInMillis);
        // Gets the time offset
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        // Get summer jet lag
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        // Subtracting these differences from the local time yields the UTC time
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        // The obtained time is UTC standard time
        return new Date(calendar.getTimeInMillis());
    }

    /**
     * The String to turn the Date
     *
     * @param str    A string value
     * @param format The default conversion format is YYYY-MM-DD HH: MM: SS
     * @return Error return NULL
     */
    public static Date stringToDate(String str, String format) {
        format = StringUtil.isEmpty(format) ? YYYY_MM_DD_HH_MM_SS2 : format;
        if (!StringUtil.isEmpty(str)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                return sdf.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static String addMonth(String date, String format, int months) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.MONTH, months);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            LOG.error(date + ", error:" + e.getMessage(), e);
        }
        return "";
    }


    /**
     * Determine whether the target date is in a range
     *
     * @param startDate
     * @param endDate
     * @param targetDate
     * @param format
     */
    public static boolean dateBetween(Date startDate, Date endDate, Date targetDate, String format) {
        String startDateStr = DateUtil.toString(startDate, format);
        String endDateStr = DateUtil.toString(endDate, format);
        String targetDateStr = DateUtil.toString(targetDate, format);
        return targetDateStr.compareTo(startDateStr) >= 0 && targetDateStr.compareTo(endDateStr) <= 0;
    }

    /**
     * Formatting date
     *
     * @param dateStr Possible format: YYYY-MM-DD or YYYY-M-D HH: MM or YYYY /MM/ DD or YYYY /M/ D HH: MM (2018-01-08 or 2018-1-8 or 2018/01/08 or 2018/1/8)
     */
    public static Date formatDate(String dateStr) {
        if (StringUtil.isEmpty(dateStr) || dateStr.length() < 8) {
            return null;
        }
        // Gets the year month day section
        String splitChar = dateStr.contains("-") ? "-" : "/";
        String[] ymd = dateStr.trim().split(" ")[0].split(splitChar);
        // The proof is not in year month day format
        if (ymd.length < 3) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, NumberUtils.toInt(ymd[0], 2000));
        calendar.set(Calendar.MONTH, NumberUtils.toInt(ymd[1], 1) - 1);
        calendar.set(Calendar.DATE, NumberUtils.toInt(ymd[2], 1));

        return calendar.getTime();
    }

    /**
     * Calculate the number of months between dates
     *
     * @param startMonth Small date (format: YYYY-MM)
     * @param endMonth   Large date (format: YYYY-MM)
     * @return A difference of months
     */
    public static int intervalMonths(String endMonth, String startMonth) {
        try {
            String startDateStr = startMonth + "-01";
            String endDateStr = endMonth + "-01";
            Date startDate = fromString(startDateStr, YYYY_MM_DD);
            int diffMonth = 0;
            while (startDateStr.compareTo(endDateStr) < 0) {
                diffMonth++;
                startDate = addMonths(startDate, 1);
                startDateStr = toString(startDate, YYYY_MM_DD);
            }

            return diffMonth;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * get the date before given month, like the date before 2 month.
     *
     * @return String
     */
    public static String getDateBeforeMonth(int howmanymonth) {
        Date date = new Date();
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(date));
        int month = Integer.parseInt(new SimpleDateFormat("MM").format(date)) - howmanymonth;
        int day = Integer.parseInt(new SimpleDateFormat("dd").format(date));
        if (month <= 0) {
            int yearFlag = (month * (-1)) / 12 + 1;
            int monthFlag = (month * (-1)) % 12;
            year -= yearFlag;
            month = monthFlag * (-1) + 12;
        } else if (day > 28) {
            if (month == 2) {
                if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) {
                    day = 29;
                } else {
                    day = 28;
                }
            } else if ((month == 4 || month == 6 || month == 9 || month == 11) && day == 31) {
                day = 30;
            }
        }
        String y = year + "";
        String m = "";
        String d = "";
        if (month < 10) {
            m = "0" + month;
        } else {
            m = month + "";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = day + "";
        }

        return y + "-" + m + "-" + d;
    }

    public static Date hexStrToDate(String hexString) {
        if (StringUtils.startsWithIgnoreCase(hexString, "0x")) {
            hexString = StringUtils.substring(hexString, 2);
        }
        return new Date(new BigInteger(hexString, 16).longValue());
    }
}

/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common;

import com.welab.wefe.common.util.StringUtil;

import java.util.Date;

/**
 * Tool class representing duration
 *
 * @author Zane
 */
public class TimeSpan {
    public static final TimeSpan YEAR = TimeSpan.fromYears(1);
    public static final TimeSpan MONTH = TimeSpan.fromMonths(1);
    public static final TimeSpan DAY = TimeSpan.fromDays(1);
    public static final TimeSpan HOUR = TimeSpan.fromHours(1);
    public static final TimeSpan MINUTE = TimeSpan.fromMinute(1);
    public static final TimeSpan MINUTE10 = TimeSpan.fromMinute(10);
    public static final TimeSpan SECOND = TimeSpan.fromSeconds(1);

    private long timespan;

    /**
     * Create a timespan instance based on the number of milliseconds
     */
    public TimeSpan(long timeSpanInMilliseconds) {
        this.timespan = timeSpanInMilliseconds;
    }


    //region add

    public TimeSpan add(TimeSpan timeSpan) {
        this.timespan += timeSpan.toMs();
        return this;
    }

    public TimeSpan addMilliseconds(long milliseconds) {
        this.timespan += milliseconds;
        return this;
    }

    public TimeSpan addSeconds(long seconds) {
        this.addMilliseconds(seconds * 1000);
        return this;
    }

    public TimeSpan addMinutes(long minutes) {
        this.addSeconds(minutes * 60);
        return this;
    }

    public TimeSpan addHours(long hours) {
        this.addMinutes(hours * 60);
        return this;
    }

    public TimeSpan addDays(long days) {
        this.addHours(days * 24);
        return this;
    }

    //endregion


    //region to

    public long toMs() {
        return this.timespan;
    }

    public long toSeconds() {
        return this.timespan / 1000;
    }

    public long toMinutes() {
        return this.toSeconds() / 60;
    }

    public long toHours() {
        return this.toMinutes() / 60;
    }

    public long toDays() {
        return this.toHours() / 24;
    }

    public long toMonths() {
        return this.toDays() / 30;
    }

    public long toYears() {
        return this.toMonths() / 12;
    }

    //endregion


    //region from

    /**
     * Create a timespan based on the number of years (365 days per year)
     */
    public static TimeSpan fromYears(long count) {
        return fromDays(count * 365);
    }

    /**
     * Create a timespan based on the number of months (30 days per month)
     */
    public static TimeSpan fromMonths(long count) {
        return fromDays(count * 30);
    }

    /**
     * Create timespan based on days
     */
    public static TimeSpan fromDays(long count) {
        return fromHours(count * 24);
    }

    /**
     * Create timespan based on hours
     */
    public static TimeSpan fromHours(long count) {
        return fromMinute(count * 60);
    }

    /**
     * Create timespan based on minutes
     */
    public static TimeSpan fromMinute(long count) {
        return fromSeconds(count * 60);
    }

    /**
     * Create timespan based on seconds
     */
    public static TimeSpan fromSeconds(long count) {
        return new TimeSpan(count * 1000);
    }

    /**
     * Create timespan based on milliseconds
     */
    public static TimeSpan fromMs(long count) {
        return new TimeSpan(count);
    }

    /**
     * Create timespan based on time
     */
    public static TimeSpan fromDate(Date date) {
        return fromMs(System.currentTimeMillis() - date.getTime());
    }


    //endregion


    @Override
    public String toString() {
        long milliseconds = Math.abs(this.toMs());
        long days = Convert.toLong(Math.floor(milliseconds / (1000 * 60 * 60 * 24)));
        milliseconds -= days * (1000 * 60 * 60 * 24);

        long hours = Convert.toLong(Math.floor(milliseconds / (1000 * 60 * 60)));
        milliseconds -= hours * (1000 * 60 * 60);

        long mins = Convert.toLong(Math.floor(milliseconds / (1000 * 60)));
        milliseconds -= mins * (1000 * 60);

        long seconds = Convert.toLong(Math.floor(milliseconds / (1000)));
        milliseconds -= seconds * (1000);


        String str = "";
        if (days > 0 || str.length() > 0) {
            str += days + "天";
        }
        if (hours > 0 || str.length() > 0) {
            str += hours + "小时";
        }
        if (mins > 0 || str.length() > 0) {
            str += mins + "分钟";
        }
        if (days == 0 && (seconds > 0 || str.length() > 0)) {
            str += seconds + "秒";
        }

        if (StringUtil.isEmpty(str)) {
            str = this.toMs() + "毫秒";
        }
        return str;
    }

    /**
     * The number of milliseconds in the common time period
     */
    public static class InMs {
        public static final long MINUTE1 = 1 * 60 * 1000;
        public static final long MINUTE2 = 2 * 60 * 1000;
        public static final long MINUTE3 = 3 * 60 * 1000;
        public static final long MINUTE4 = 4 * 60 * 1000;
        public static final long MINUTE5 = 5 * 60 * 1000;
        public static final long MINUTE6 = 6 * 60 * 1000;
        public static final long MINUTE7 = 7 * 60 * 1000;
        public static final long MINUTE8 = 8 * 60 * 1000;
        public static final long MINUTE9 = 9 * 60 * 1000;
        public static final long MINUTE10 = 10 * 60 * 1000;
        public static final long MINUTE15 = 15 * 60 * 1000;
        public static final long MINUTE20 = 20 * 60 * 1000;
        public static final long MINUTE30 = 30 * 60 * 1000;

        public static final long HOUR1 = 1 * 60 * 60 * 1000;
        public static final long HOUR2 = 2 * 60 * 60 * 1000;

        public static final long DAY1 = 1 * 24 * 60 * 60 * 1000;
        public static final long DAY7 = 7 * 24 * 60 * 60 * 1000;
    }

    /**
     * Number of seconds in common time period
     */
    public static class InSecond {
        public static final long MINUTE1 = 1 * 60;
        public static final long MINUTE2 = 2 * 60;
        public static final long MINUTE3 = 3 * 60;
        public static final long MINUTE4 = 4 * 60;
        public static final long MINUTE5 = 5 * 60;
        public static final long MINUTE6 = 6 * 60;
        public static final long MINUTE7 = 7 * 60;
        public static final long MINUTE8 = 8 * 60;
        public static final long MINUTE9 = 9 * 60;
        public static final long MINUTE10 = 10 * 60;
        public static final long MINUTE15 = 15 * 60;
        public static final long MINUTE20 = 20 * 60;
        public static final long MINUTE30 = 30 * 60;

        public static final long HOUR1 = 1 * 60 * 60;
        public static final long HOUR2 = 2 * 60 * 60;

        public static final long DAY1 = 1 * 24 * 60 * 60;
        public static final long DAY7 = 7 * 24 * 60 * 60;
    }
}

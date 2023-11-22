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

import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Convert everything safely
 *
 * @author Zane
 */
public class Convert {

    //region Int

    public static Integer toInt(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Double) {
            return toInt((Double) value);
        }

        if (value instanceof Long) {
            return toInt((Long) value);
        }

        return toInt(String.valueOf(value));
    }

    public static Integer toInt(Double value) {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    public static Integer toInt(Long value) {
        if (value == null) {
            return null;
        }
        return Math.toIntExact(value);
    }

    public static Integer toInt(String value) {
        if (value == null) {
            return null;
        }
        return toInt(Double.parseDouble(value));
    }

    /**
     * Attempt to convert string to int
     *
     * @param defaultValue Return value when conversion fails
     */
    public static int tryToInt(String value, int defaultValue) {
        try {
            return toInt(Double.parseDouble(value));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    //endregion


    //region Double

    public static Double toDouble(String value) {
        if (value == null) {
            return null;
        }
        return Double.parseDouble(value);
    }

    public static Double toDouble(Integer value) {
        if (value == null) {
            return null;
        }
        return new Double(value);
    }

    public static Double toDouble(Long value) {
        if (value == null) {
            return null;
        }
        return new Double(value);
    }

    //endregion

    //region toLong

    public static Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof BigInteger) {
            return ((BigInteger) value).longValue();
        }

        if (value instanceof Double) {
            return toLong((Double) value);
        }

        return toLong(String.valueOf(value));
    }

    public static Long toLong(Double value) {
        if (value == null) {
            return null;
        }

        return value.longValue();
    }

    public static Long toLong(String value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(StringUtil.substringBefore(value, "."));
    }

    //endregion

    //region Boolean

    public static boolean toBoolean(Object obj) {
        return Boolean.parseBoolean(obj.toString());
    }


    //endregion

    //region LocalDateTime

    public static Date toDate(Object value) {
        return TypeUtils.castToDate(value);
    }

    public LocalDateTime toLocalDateTime(Date value) {
        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    //endregion


    //region Date

    public Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date to string
     * <p>
     * format string：yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static String toString(Date date) {
        return DateUtil.toString(date, DateUtil.YYYY_MM_DDTHH_MM_SS_SSSZ);
    }

    /**
     * r
     * Convert date string to date
     * <p>
     * Null is returned when the conversion fails
     */
    public static Date toDate(String value, String format) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date toDate(UUID uuid) {
        return new Date((uuid.timestamp() - 122192928000000000L) / 10000L);
    }

    //endregion


    //region Array

    public static <T> T[] toArray(Class<T> componentType, List<T> list) {
        if (list == null) {
            return null;
        }

        T[] array = (T[]) Array.newInstance(componentType, list.size());
        list.toArray(array);
        return array;
    }

    //endregion


}

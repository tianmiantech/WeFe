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

import java.util.regex.Pattern;

/**
 * Used for various calibrations
 *
 * @author zane.luo
 */
public class Validator {

    private static final Pattern MATCH_UNSIGNED_INTEGER_PATTERN = Pattern.compile("^\\d+$");
    private final static Pattern MATCH_BOOLEAN_PATTERN = Pattern.compile("^true$|^false$|^0$|^1$", Pattern.CASE_INSENSITIVE);

    public static boolean isBoolean(Object value) {
        if (value instanceof Boolean) {
            return true;
        }
        return MATCH_BOOLEAN_PATTERN.matcher(value.toString()).find();
    }

    public static boolean isLong(Object value) {
        if (value instanceof Long) {
            return true;
        }
        try {
            Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(Object value) {
        if (value instanceof Double) {
            return true;
        }
        try {
            Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    public static boolean isInteger(Object value) {
        if (value instanceof Integer) {
            return true;
        }
        try {
            Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    /**
     * Is it an unsigned integer
     */
    public static boolean isUnsignedInteger(Object value) {

        if (value instanceof Integer) {
            return (Integer) value >= 0;
        }

        return MATCH_UNSIGNED_INTEGER_PATTERN.matcher(String.valueOf(value)).find();
    }
}

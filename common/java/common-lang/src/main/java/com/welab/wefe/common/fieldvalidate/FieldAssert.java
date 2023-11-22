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

package com.welab.wefe.common.fieldvalidate;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * Tool class, used to check the value of the input field
 *
 * @author Zane
 */
public class FieldAssert {

    public static boolean isPhoneNumber(String string) {
        if (string == null) {
            return false;
        }

        String reg = "^\\d{11}$";
        return Pattern.matches(reg, string);
    }

    /**
     * Is this a valid name
     */
    public static boolean isPersonalName(String string) {
        if (string == null) {
            return false;
        }

        String reg = "^[·\u4e00-\u9fa5]{2,10}$";
        return Pattern.matches(reg, string);
    }

    /**
     * Is it a valid mailbox
     */
    public static boolean isEmail(String string) {
        if (string == null) {
            return false;
        }

        String reg = "^[\\w-\\.]+@([\\w-]+\\.)+[a-z]{2,3}$";
        return Pattern.matches(reg, string);
    }

    /**
     * Is it a valid QQ number
     */
    public static boolean isQqNumber(String string) {
        if (string == null) {
            return false;
        }

        String reg = "^\\d{5,12}$";
        return Pattern.matches(reg, string);
    }

    /**
     * Is it a valid company name
     */
    public static boolean isCompanyName(String str) {
        if (StringUtils.isBlank(str) || str.length() > 50) {
            return false;
        }

        String reg = "[a-zA-z\u4e00-\u9fa5]+[a-zA-z0-9\u4e00-\u9fa5\\(\\)\\[\\]\\.\\,\\&]*";
        return Pattern.matches(reg, str);
    }

    /**
     * Is it valid ID number?
     */
    public static boolean isCnid(String str) {
        if (str == null) {
            return false;
        }
        if (str.length() != 15 && str.length() != 18) {
            return false;
        }
        String regex;
        if (str.length() == 15) {
            regex = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        } else {
            regex = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}(\\d|X|x)$";
        }
        return Pattern.matches(regex, str);
    }

    /**
     * Check if the string is empty
     */
    public static boolean isNullOrEmpty(Object str) {
        return str == null || str.toString().isEmpty();
    }

    /**
     * Check whether the string is empty or empty
     */
    public static boolean isNullOrWhitespace(Object str) {
        return str == null || str.toString().isEmpty() || str.toString().replace(" ", "").length() == 0;
    }

    /**
     * The forbidden string is an empty string
     */
    public static void notNullOrEmpty(Object string, String parameterName) {
        if (string == null || string.toString().length() == 0) {
            throw new IllegalArgumentException("“" + parameterName + "” is not allowed to be null or empty.");
        }
    }

    /**
     * Prohibit strings from being empty or empty
     */
    public static void notNullOrWhitespace(Object string, String parameterName) {
        if (isNullOrWhitespace(string)) {
            throw new IllegalArgumentException("“" + parameterName + "” is not allowed to be null or whitespace.");
        }
    }

    /**
     * Prohibit object from being null
     */
    public static void notNull(Object object, String parameterName) {
        if (object == null) {
            throw new IllegalArgumentException("“" + parameterName + "” is not allowed to be null.");
        }
    }
}

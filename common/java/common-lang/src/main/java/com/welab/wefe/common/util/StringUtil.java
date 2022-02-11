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

package com.welab.wefe.common.util;

import com.welab.wefe.common.function.CharFunction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zane.luo
 */
public class StringUtil extends StringUtils {
    protected static final Logger LOG = LoggerFactory.getLogger(StringUtil.class);
    /**
     * Matching Chinese characters
     */
    private static final Pattern PATTERN_MATCH_CHINESE_CHARACTER = Pattern.compile("[\\u4e00-\\u9fa5]");
    /**
     * Match underscore
     */
    private static final Pattern PATTERN_MATCH_UNDERLINE_CHARACTER = Pattern.compile("_(\\w)");

    private static final Pattern MATCH_PHONENUMBER = Pattern.compile("^((\\+86)|(86))?1[3456789]\\d{9}$");

    /**
     * 将列表拼接为用逗号分隔的字符串
     */
    public static String joinByComma(final Iterable<?> iterable) {
        if (iterable == null) {
            return "";
        }
        return join(iterable, ",");
    }


    /**
     * Compares whether two strings are the same, ignoring the mask part.
     *
     * @author Zane
     */
    public final static boolean equalsIgnoreMask(String str1, String str2) {
        // Exclude cases where both values are null
        if (str1 == str2) {
            return true;
        }
        // Exclude the case where one of them is null
        if (str1 == null || str2 == null) {
            return false;
        }

        if (str1.length() != str2.length()) {
            return false;
        }

        for (int i = 0; i < str1.length(); i++) {
            char a = str1.charAt(i);
            char b = str2.charAt(i);

            if (a == '*' || b == '*') {
                continue;
            }

            if (a != b) {
                return false;
            }
        }
        return true;
    }

    /**
     * Full corner character to half corner character（DBC case）
     * <quote>
     * The full space is 12288, and the half space is 32
     * The mapping between half Angle (33-126) and full Angle (65281-65374) of other characters is 65248
     * </quote>
     *
     * @author Zane
     */
    public static String ToDbc(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }


    /**
     * Check whether a character is a Chinese character
     */
    public static boolean isChineseCharacter(char c) {
        Matcher matcher = PATTERN_MATCH_CHINESE_CHARACTER.matcher(String.valueOf(c));
        return matcher.matches();
    }


    /**
     * Underline to hump
     */
    public static String underLineCaseToCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        str = str.toLowerCase();
        Matcher matcher = PATTERN_MATCH_UNDERLINE_CHARACTER.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * The hump is underlined
     */
    public static String camelCaseToUnderLineCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }


    /**
     * Deletes the specified character at the beginning and end of the text
     */
    public static String trim(String input, char... chars) {
        if (isEmpty(input)) {
            return input;
        }

        CharFunction<Boolean> needTrimFunc = (c) -> {
            for (char item : chars) {
                if (item == c) {
                    return true;
                }
            }
            return false;
        };

        int start = 0;
        int end = input.length();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (needTrimFunc.apply(c)) {
                start = i + 1;
                continue;
            }

            break;
        }

        if (start > end) {
            return "";
        }

        for (int i = input.length() - 1; i > start; i--) {
            char c = input.charAt(i);
            if (needTrimFunc.apply(c)) {
                end = i;
                continue;
            }
            break;
        }

        if (start > end) {
            return "";
        }

        return input.substring(start, end);

    }

    /**
     * Converts arbitrary variable names to underscore format
     */
    public static String stringToUnderLineLowerCase(String str) {
        if (isEmpty(str)) {
            return "";
        }
        // Remove first and last invisible characters
        str = str.trim();

        // Eliminate symbol
        str = str.replaceAll("[~!@#$%^&*()-=+]", "");

        // Remove successive Spaces
        while (str.contains("  ")) {
            str = str.replace("  ", " ");
        }

        // Replace Spaces with underscores
        str = str.replace(" ", "_");

        // The hump is underlined
        str = camelCaseToUnderLineCase(str);
        str = trim(str, '_');
        return str;
    }


    /**
     * Truncate the string by its byte length
     */
    public static String substringByByteLength(String input, int maxLength) {
        try {
            return substringByByteLength(input, maxLength, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return input;
    }

    /**
     * Truncate the string by its byte length
     */
    public static String substringByByteLength(String input, int maxLength, String charsetName) throws UnsupportedEncodingException {
        if (input == null) {
            return null;
        }

        byte[] inputBytes = input.getBytes(charsetName);
        if (inputBytes.length <= maxLength) {
            return input;
        }

        byte[] resultBytes = new byte[maxLength];

        System.arraycopy(inputBytes, 0, resultBytes, 0, maxLength);

        String result = new String(resultBytes, charsetName);

        int resLen = result.length();
        if (input.substring(0, resLen).getBytes(charsetName).length > maxLength) {
            result = input.substring(0, resLen - 1);
        }

        return result;

    }

    /**
     * Intercepts the specified length at the end of the string
     */
    public static String substringRightLength(String str, int length) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        if (str.length() < length || length < 0) {
            return str;
        }
        return str.substring(str.length() - length);
    }


    /**
     * Splits a string into an array and removes empty elements.
     */
    public static List<String> splitWithoutEmptyItem(String str, String regex) {

        List<String> list = new ArrayList<>();

        if (isEmpty(str)) {
            return list;
        }

        String[] strings = str.split(regex);

        for (String item : strings) {
            if (isNotEmpty(item)) {
                list.add(item);
            }
        }

        return list;
    }

    /**
     * Map median value: empty string -> NULL
     */
    public static Map<String, Object> parserMapStringEmptyValueAsNull(Map<String, Object> paramMap) {
        if (paramMap == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if ("".equals(entry.getValue())) {
                entry.setValue(null);
            }
        }
        return paramMap;
    }

    /**
     * Check the mobile phone number format
     */
    public static boolean checkPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.length() > 10 && phoneNumber.length() < 15) {
            Matcher matcher = MATCH_PHONENUMBER.matcher(phoneNumber);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * If it is empty, it is whitespace
     */
    public static String isEmptyToBlank(String str) {
        return isEmpty(str) || "null".equals(str) ? " " : str;
    }

    /**
     * Remove two Spaces, if blank, go to ""
     */
    public static String strTrim(String str) {
        return StringUtil.isEmpty(str) ? " " : str.trim();
    }

    /**
     * Remove two Spaces
     */
    public static String strTrim2(String str) {
        return StringUtil.isEmpty(str) ? str : str.trim();
    }

    /**
     * MD5 encryption is performed on the incoming string
     *
     * @return Encrypting MD5 string
     */
    public final static String md5(String s) throws Exception {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
                'E', 'F'};
        byte[] strTemp = s.getBytes();
        MessageDigest mdTemp = MessageDigest.getInstance("MD5");
        mdTemp.update(strTemp);
        byte[] md = mdTemp.digest();
        int j = md.length;
        char[] str = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

}

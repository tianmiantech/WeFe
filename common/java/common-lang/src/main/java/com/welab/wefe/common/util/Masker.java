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

/**
 * 对敏感信息进行掩码处理
 *
 * @author zane
 * @date 2022/1/4
 */
public class Masker {
    /**
     * 为手机号加上掩码
     * <p>
     * before: 138800000088
     * after: 1388*****88
     * <p>
     * before: +8613880000088
     * after: +861388*****88
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        if (phoneNumber.length() < 11) {
            return phoneNumber;
        }

        String left = StringUtil.left(phoneNumber, phoneNumber.length() - 7);
        String right = StringUtil.right(phoneNumber, 2);
        return left + "*****" + right;

    }

    private static void testPhoneNumber() {
        String str1 = "13880000088";
        String str2 = "+8613880000088";
        String str3 = "12345678901234567890";

        System.out.println(maskPhoneNumber("123"));
        System.out.println(maskPhoneNumber(str1));
        System.out.println(maskPhoneNumber(str2));
        System.out.println(maskPhoneNumber(str3));
    }


    /**
     * 为邮箱加上掩码
     * <p>
     * before: Abc-hel@qq.com
     * after: Abc****@qq.com
     */
    public static String maskEmail(String email) {
        if (email == null) {
            return null;
        }

        int index = email.indexOf('@');

        if (index <= 0) {
            return email;
        }
        // 默认 @ 符号左边打4位掩码
        int maskLength = 4;

        // 如果 @ 左边不够4位，就少打一点。
        if (index <= maskLength) {
            maskLength = index / 2;
        }

        // 不能全部都打上掩码，至少要露一位明文出来。
        if (maskLength == index) {
            maskLength = 0;
        }

        String left = email.substring(0, index - maskLength);
        String right = StringUtil.substringAfter(email, "@");
        StringBuilder result = new StringBuilder(email.length());
        result.append(left);
        for (int i = 0; i < maskLength; i++) {
            result.append("*");
        }
        result
                .append('@')
                .append(right);
        return result.toString();
    }

    private static void testMaskEmail() {
        String str1 = "hello";
        String str2 = "Abc-hel@qq.com";
        String str3 = "Abc@qq.com";
        String str4 = "Ab@qq.com";
        String str5 = "A@qq.com";
        String str6 = "@qq.com";

        System.out.println(maskEmail(str1));
        System.out.println(maskEmail(str2));
        System.out.println(maskEmail(str3));
        System.out.println(maskEmail(str4));
        System.out.println(maskEmail(str5));
        System.out.println(maskEmail(str6));
    }

    public static void main(String[] args) {
        testMaskEmail();
    }
}

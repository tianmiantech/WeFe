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

package com.welab.wefe.common;

import java.util.regex.Pattern;

/**
 * Used for various calibrations
 *
 * @author zane.luo
 */
public class Validator {

    private static final Pattern MATCH_INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern MATCH_UNSIGNED_INTEGER_PATTERN = Pattern.compile("^\\d+$");

    /**
     * Is it an integer
     */
    public static boolean isInteger(Object value) {

        if (value instanceof Integer) {
            return true;
        }

        return MATCH_INTEGER_PATTERN.matcher(String.valueOf(value)).find();
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

    public static void main(String[] args) {
        System.out.println(isInteger(null));
        System.out.println(isInteger(-1));
        System.out.println(isInteger(0));
        System.out.println(isInteger(1));

        System.out.println();

        System.out.println(isUnsignedInteger(null));
        System.out.println(isUnsignedInteger(-1));
        System.out.println(isUnsignedInteger(0));
        System.out.println(isUnsignedInteger(1));

    }
}

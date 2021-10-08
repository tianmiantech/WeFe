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

/**
 * Configuration.java
 * <p>
 * Copyright 2016 WeLab Holdings, Inc. All rights reserved.
 * WELAB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.welab.wefe.configuration;

import org.apache.commons.configuration.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author yuxin.zhang
 */
public class Configurations {
    private static Configuration config = ConfigurationManager.getConfig();

    private Configurations() {
    }

    public static boolean isEmpty() {
        return config.isEmpty();
    }

    public static boolean containsKey(String key) {
        return config.containsKey(key);
    }

    public static Object getProperty(String key) {
        return config.getProperty(key);
    }

    public static Iterator<String> getKeys(String prefix) {
        return config.getKeys(prefix);
    }

    public static Iterator<String> getKeys() {
        return config.getKeys();
    }

    public static Properties getProperties(String key) {
        return config.getProperties(key);
    }

    public static boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public static byte getByte(String key) {
        return config.getByte(key);
    }

    public static byte getByte(String key, byte defaultValue) {
        return config.getByte(key, defaultValue);
    }

    public static Byte getByte(String key, Byte defaultValue) {
        return config.getByte(key, defaultValue);
    }

    public static double getDouble(String key) {
        return config.getDouble(key);
    }

    public static double getDouble(String key, double defaultValue) {
        return config.getDouble(key, defaultValue);
    }

    public static Double getDouble(String key, Double defaultValue) {
        return config.getDouble(key, defaultValue);
    }

    public static float getFloat(String key) {
        return config.getFloat(key);
    }

    public static float getFloat(String key, float defaultValue) {
        return config.getFloat(key, defaultValue);
    }

    public static Float getFloat(String key, Float defaultValue) {
        return config.getFloat(key, defaultValue);
    }

    public static int getInt(String key) {
        return config.getInt(key);
    }

    public static int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    public static Integer getInteger(String key, Integer defaultValue) {
        return config.getInteger(key, defaultValue);
    }

    public static long getLong(String key) {
        return config.getLong(key);
    }

    public static long getLong(String key, long defaultValue) {
        return config.getLong(key, defaultValue);
    }

    public static Long getLong(String key, Long defaultValue) {
        return config.getLong(key, defaultValue);
    }

    public static short getShort(String key) {
        return config.getShort(key);
    }

    public static short getShort(String key, short defaultValue) {
        return config.getShort(key, defaultValue);
    }

    public static Short getShort(String key, Short defaultValue) {
        return config.getShort(key, defaultValue);
    }

    public static BigDecimal getBigDecimal(String key) {
        Object value = getProperty(key);
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return config.getBigDecimal(key);
    }

    public static BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        Object value = getProperty(key);
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return config.getBigDecimal(key, defaultValue);
    }

    public static BigInteger getBigInteger(String key) {
        return config.getBigInteger(key);
    }

    public static BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return config.getBigInteger(key, defaultValue);
    }

    public static String getString(String key) {
        return config.getString(key);
    }

    public static String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static String[] getStringArray(String key) {
        return config.getStringArray(key);
    }

    public static List<Object> getList(String key) {
        return config.getList(key);
    }

    public static List<Object> getList(String key, List<?> defaultValue) {
        return config.getList(key, defaultValue);
    }

}

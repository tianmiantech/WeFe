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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Entity class parameter validity check tool class
 *
 * @author Zane
 */
public class FieldValidateUtil {
    private static final String[] XSS_KEYWORDS = {">", "<"};

    /**
     * Normalize the value of the field
     */
    public static void standardize(Object obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Set<Field> totalFields = ClassUtils.listFields(clazz, true);

        for (Field field : totalFields) {
            field.setAccessible(true);
            Object value = field.get(obj);

            if (value != null) {
                Check standardField = field.getDeclaredAnnotation(Check.class);
                if (standardField != null) {
                    StandardFieldType standardFieldType = standardField.type();
                    if (!standardFieldType.equals(StandardFieldType.NONE) && standardFieldType.needStandardize()) {
                        field.set(obj, standardFieldType.standardize(value));
                    }
                }
            }


        }
    }

    /**
     * Check and standardize the field validity of the entity according to the notes on the field.
     */
    public static void checkAndStandardize(Object obj) throws StatusCodeWithException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Set<Field> totalFields = ClassUtils.listFields(clazz, true);

        for (Field field : totalFields) {

            Check check = field.getDeclaredAnnotation(Check.class);

            if (check == null) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(obj);

            /**
             * ********** require **********
             */
            boolean emptyIsNotOk = check.require() && (value == null || "".equals(value));

            if (emptyIsNotOk) {
                String message = String.format("%s can not be empty!", field.getName());
                if (StringUtil.isNotEmpty(check.messageOnEmpty())) {
                    message = check.messageOnEmpty();
                }
                throw new StatusCodeWithException(message, StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (value == null || StringUtils.isEmpty(value.toString())) {
                continue;
            }

            String valueStr = value.toString();

            /**
             * ********** standardize **********
             */
            if (!check.type().equals(StandardFieldType.NONE)) {

                StandardFieldType standardFieldType = check.type();
                if (!standardFieldType.check(value)) {
                    throw new StatusCodeWithException(getInvalidMessage(check, field, value), StatusCode.PARAMETER_VALUE_INVALID);
                } else if (standardFieldType.needStandardize()) {
                    field.set(obj, standardFieldType.standardize(value));
                }
            }

            /**
             * ********** regex **********
             */
            if (StringUtils.isNotEmpty(check.regex())) {
                if (!Pattern.matches(check.regex(), value.toString())) {
                    throw new StatusCodeWithException(getInvalidMessage(check, field, value), StatusCode.PARAMETER_VALUE_INVALID);
                }

            }

            checkReactionaryKeyword(check, field.getName(), value);
            checkBlockXss(check, field.getName(), value);

            // 对 string 字段进行防止 sql 注入处理
            if (value instanceof String && check.blockSqlInjection()) {
                field.set(
                        obj,
                        valueStr
                                .replace(",", "，")
                                .replace("\"", "“")
                                .replace("'", "’")
                                .replace("#", "")
                                .replace("-", "")
                                .replace("%", "")
                                .replace("<", "")
                                .replace("\\", "")
                                .replace("/", "")
                );
            }

        }
    }

    /**
     * 检查输入是否包含反动关键字
     */
    private static void checkReactionaryKeyword(Check check, String fieldName, Object value) throws StatusCodeWithException {
        if (!check.blockReactionaryKeyword()) {
            return;
        }

        // String valueStr = JSON.toJSONString(value, LoggerSerializeConfig.instance());
        String valueStr = value.toString();
        String keyword = ReactionaryKeywords.match(valueStr);
        if (StringUtil.isNotEmpty(keyword)) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException(fieldName + " 包含不允许的输入：" + keyword);
        }
    }

    /**
     * 检查输入是否包含 xss 关键字
     */
    private static void checkBlockXss(Check check, String fieldName, Object value) throws StatusCodeWithException {
        if (!check.blockXss()) {
            return;
        }

        String valueStr = value.toString();
        for (String keyword : XSS_KEYWORDS) {
            if (valueStr.contains(keyword)) {
                StatusCode
                        .PARAMETER_VALUE_INVALID
                        .throwException(fieldName + " 包含不安全的输入：" + keyword);
            }
        }

    }

    /**
     * Message when the generated value verification is illegal
     */
    private static String getInvalidMessage(Check check, Field field, Object value) {
        String message = String.format("invalid %s: %s", field.getName(), value);
        if (StringUtil.isNotEmpty(check.messageOnInvalid())) {
            message = check.messageOnInvalid();
        }
        return message;
    }
}

/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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
package com.welab.wefe.common.fieldvalidate.secret;

import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zane
 * @date 2022/7/13
 */
public class SecretUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Security.class);
    /**
     * Secret 字段的全局缓存
     */
    private static final Map<Class, Map<String, Secret>> SECRET_FIELD_MAP = new HashMap<>();

    /**
     * 获取指定字段的 @Secret 注解
     */
    public static Secret getAnnotation(Class clazz, String fieldName) {
        if (!SECRET_FIELD_MAP.containsKey(clazz)) {
            extractSecret(clazz);
        }

        return SECRET_FIELD_MAP.get(clazz).get(fieldName);
    }

    /**
     * 反射 Class，并缓存其中所有包含 @Secret 的字段。
     */
    public synchronized static void extractSecret(Class clazz) {
        if (SECRET_FIELD_MAP.containsKey(clazz)) {
            return;
        }
        long start = System.currentTimeMillis();
        // 对 clazz 中的所有字段进行检查，并缓存包含 @Secret 的字段
        HashMap<String, Secret> secretFieldsMap = new HashMap<>(0);

        Set<Field> fields = ClassUtils.listFields(clazz, false);
        for (Field field : fields) {
            Secret secret = field.getAnnotation(Secret.class);

            // 仅储存包含 @Secret 的字段
            if (secret != null) {
                String rawName = field.getName();

                // 同时兼容下划线和驼峰命名
                String underLineCaseName = StringUtil.camelCaseToUnderLineCase(rawName);
                String cameCaseName = StringUtil.underLineCaseToCamelCase(rawName);

                secretFieldsMap.put(underLineCaseName, secret);
                secretFieldsMap.put(cameCaseName, secret);
            }
        }
        SECRET_FIELD_MAP.put(clazz, secretFieldsMap);
        // 观察下性能，以后会删掉。
        System.out.println("extractSecret spend:" + (System.currentTimeMillis() - start) + " " + clazz.getName());
    }
}

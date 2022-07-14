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

import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

/**
 * @author zane
 * @date 2022/7/13
 */
public class SecretUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Security.class);

    public static Secret getAnnotation(Class clazz, String fieldName) {
        Secret secret = null;
        try {
            secret = clazz
                    .getDeclaredField(
                            // 兼容下划线命名
                            StringUtil.underLineCaseToCamelCase(fieldName)
                    )
                    .getAnnotation(Secret.class);
        } catch (NoSuchFieldException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return secret;
        }

        return secret;
    }
}

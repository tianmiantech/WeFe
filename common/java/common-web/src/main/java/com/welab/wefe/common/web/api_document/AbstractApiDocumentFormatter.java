/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.web.api_document;

import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.Api;

import java.lang.reflect.Modifier;
import java.util.TreeMap;

/**
 * @author zane
 * @date 2021/12/3
 */
public abstract class AbstractApiDocumentFormatter {
    private static TreeMap<String, Class<?>> APIS_MAP = new TreeMap<>();

    static {
        ReflectionsUtil
                .getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, Api.class)
                .stream()
                .filter(x -> !Modifier.isAbstract(x.getModifiers()))
                .forEach(x -> {
                    Api annotation = x.getAnnotation(Api.class);
                    String key = StringUtil
                            .trim(annotation.path(), '/', '\\')
                            .toLowerCase();
                    APIS_MAP.put(key, x);
                });
    }

    public String format() {

    }
}

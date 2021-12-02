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
package com.welab.wefe.common.web.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.NoneApiInput;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author zane
 * @date 2021/10/27
 */
public class ApiListMarkdownFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiListMarkdownFormatter.class);

    public static String format(List<Class<?>> list) {

        StringBuilder str = new StringBuilder(1024);

        list
                .stream()
                .filter(x -> !Modifier.isAbstract(x.getModifiers()))
                .filter(x -> {
                    for (String key : Arrays.asList("derived_data_set", "chat", "online_demo", "serving", "tianmiantech", "test/")) {
                        if (x.getAnnotation(Api.class).path().contains(key)) {
                            return false;
                        }
                    }

                    return true;
                })
                .sorted(Comparator.comparing(x -> StringUtil.trim(x.getAnnotation(Api.class).path(), '/')))
                .forEach(x -> getApiInfo(str, x));

        return str.toString();

    }

    /**
     * @param clazz API class
     */
    private static String getApiInfo(StringBuilder str, Class<?> clazz) {
        Api api = clazz.getAnnotation(Api.class);
        if (api == null) {
            return null;
        }

        String title = StringUtil.trim(api.path(), '/') + "(" + api.name() + ")";
        str.append("## " + title + System.lineSeparator());

        if (StringUtil.isNotEmpty(api.desc())) {
            str.append("API 简介：" + api.desc() + System.lineSeparator() + "<br>" + System.lineSeparator());
        }

        // Gets a list of generic types for the API
        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
        }

        Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();

        // Gets the input and output types
        Class<?> inputClass = null, outputClass = null;
        for (Type type : types) {
            try {
                Class<?> tClazz;
                if (type instanceof ParameterizedTypeImpl) {
                    tClazz = ((ParameterizedTypeImpl) type).getRawType();
                } else if (type instanceof TypeVariableImpl) {
                    tClazz = null;
                } else {
                    tClazz = (Class<?>) type;
                }

                if (tClazz != null && AbstractApiInput.class.isAssignableFrom(tClazz)) {
                    inputClass = tClazz;
                } else {
                    outputClass = tClazz;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }


        buildApiParamsJObject(str, "Input", inputClass);

        str.append(System.lineSeparator());


        buildApiParamsJObject(str, "Output", outputClass);

        str
                .append(System.lineSeparator())
                .append("<br>")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        return str.toString();
    }


    /**
     * Generate API parameter documentation
     */
    private static void buildApiParamsJObject(StringBuilder str, String title, Class<?> clazz) {

        if (clazz == null || clazz == NoneApiInput.class || clazz == NoneApiOutput.class) {
            return;
        }


        Set<Field> fields = ClassUtils.listFields(clazz);

        str.append("**" + title + ":**<br>").append(System.lineSeparator());
        str.append("|name|type|comment|require|" + System.lineSeparator());
        str.append("|---|---|---|---|" + System.lineSeparator());


        for (Field field : fields) {
            String name = StringUtil.stringToUnderLineLowerCase(field.getName());

            // Skip fields that are not printed
            JSONField jsonAnnotation = field.getAnnotation(JSONField.class);
            if (jsonAnnotation != null && !jsonAnnotation.serialize()) {
                continue;
            }

            String type = ClassUtils.getFieldTypeName(field);

            String comment = "";
            Boolean require = null;
            Check annotation = field.getAnnotation(Check.class);
            if (annotation != null) {
                if (annotation.hiddenForFrontEnd()) {
                    continue;
                }

                if (StringUtil.isNotEmpty(annotation.name())) {
                    comment = annotation.name();
                }
                require = annotation.require();
            }

            str.append("|" + name + "|" + type + "|" + comment + "|" + (require == null ? "" : String.valueOf(require)) + "|" + System.lineSeparator());
        }
    }
}

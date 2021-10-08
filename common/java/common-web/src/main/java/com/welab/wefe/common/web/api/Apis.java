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

package com.welab.wefe.common.web.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Api(path = "apis", name = "获取 api 列表", login = false)
public class Apis extends AbstractNoneInputApi<Apis.Output> {

    private static List<JObject> API_LIST = new ArrayList<>();

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        if (CollectionUtils.isEmpty(API_LIST)) {

            List<Class<?>> list = ReflectionsUtil.getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, Api.class);

            API_LIST = list
                    .stream()
                    .filter(x -> !Modifier.isAbstract(x.getModifiers()))
                    .map(this::getApiInfo)
                    .sorted((a, b) -> {
                        assert a != null;
                        return a.getString("path").compareToIgnoreCase(b.getString("path"));
                    })
                    .collect(Collectors.toList());

        }

        return success(Output.of(API_LIST.size(), API_LIST));

    }

    /**
     * @param clazz API classes
     */
    private JObject getApiInfo(Class<?> clazz) {
        Api api = clazz.getAnnotation(Api.class);
        if (api == null) {
            return null;
        }


        JObject json = JObject.create()
                .append("path", StringUtil.trim(api.path(), '/', '\\'));

        if (StringUtil.isNotEmpty(api.name())) {
            json.append("name", api.name());
        }

        if (StringUtil.isNotEmpty(api.desc())) {
            json.append("desc", api.desc());
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
                } else {
                    tClazz = (Class<?>) type;
                }

                if (AbstractApiInput.class.isAssignableFrom(tClazz)) {
                    inputClass = tClazz;
                } else {
                    outputClass = tClazz;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        json.append("input", buildApiParamsJObject(inputClass));
        json.append("output", buildApiParamsJObject(outputClass));

        return json;
    }


    /**
     * Generate API parameter documentation
     */
    private static List<JObject> buildApiParamsJObject(Class<?> clazz) {

        if (clazz == null || clazz == NoneApiInput.class || clazz == NoneApiOutput.class) {
            return null;
        }


        Set<Field> fields = ClassUtils.listFields(clazz);
        List<JObject> list = new ArrayList<>();

        for (Field field : fields) {
            String name = StringUtil.stringToUnderLineLowerCase(field.getName());

            // Skip fields that are not printed
            JSONField jsonAnnotation = field.getAnnotation(JSONField.class);
            if (jsonAnnotation != null && !jsonAnnotation.serialize()) {
                continue;
            }

            JObject param = JObject.create();
            param.put("name", name);
            String type = field.getType().getCanonicalName();
            if (type.contains(".")) {
                type = StringUtil.substringAfterLast(field.getType().getCanonicalName(), ".");
            }
            param.put("type", type);

            Check annotation = field.getAnnotation(Check.class);
            if (annotation != null) {
                if (annotation.hiddenForFrontEnd()) {
                    continue;
                }

                if (StringUtil.isNotEmpty(annotation.name())) {
                    param.put("comment", annotation.name());
                }
                param.put("require", annotation.require());
            }

            list.add(param);
        }
        return list;
    }


    public static class Output extends AbstractApiOutput {
        public int size;
        public List<JObject> list;

        public static Output of(int size, List<JObject> list) {
            Output output = new Output();
            output.size = size;
            output.list = list;
            return output;
        }
    }
}

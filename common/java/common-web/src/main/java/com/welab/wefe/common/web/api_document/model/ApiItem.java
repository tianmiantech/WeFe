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
package com.welab.wefe.common.web.api_document.model;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.NoneApiInput;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author zane
 * @date 2021/12/3
 */
public class ApiItem {
    public Api annotation;
    public Class<?> apiClass;
    public String path;
    public String id;
    public String name;
    public String desc;
    private final String group;
    public ApiParam input;
    public ApiParam output;

    public ApiItem(Class<?> apiClass) {
        this.apiClass = apiClass;
        this.annotation = apiClass.getAnnotation(Api.class);
        this.path = StringUtil
                .trim(annotation.path(), '/', '\\')
                .replace("\\", "/")
                .toLowerCase();
        this.id = this.path.replace("/", "-");
        this.name = annotation.name();
        this.desc = annotation.desc();
        this.group = StringUtil.substringBefore(path, "/");

        // Gets a list of generic types for the API
        while (!(apiClass.getGenericSuperclass() instanceof ParameterizedType)) {
            apiClass = apiClass.getSuperclass();
        }

        Type[] types = ((ParameterizedType) apiClass.getGenericSuperclass()).getActualTypeArguments();

        // Gets the input and output types
        for (Type type : types) {
            Class<?> tClazz;
            if (type instanceof ParameterizedTypeImpl) {
                tClazz = ((ParameterizedTypeImpl) type).getRawType();
            } else if (type instanceof TypeVariableImpl) {
                tClazz = null;
            } else {
                tClazz = (Class<?>) type;
            }

            if (tClazz == null) {
                continue;
            }

            if (AbstractApiInput.class.isAssignableFrom(tClazz)) {
                if (tClazz == NoneApiInput.class) {
                    this.input = null;
                } else {
                    this.input = new ApiParam(tClazz);
                }
            } else {
                if (tClazz == NoneApiOutput.class) {
                    this.output = null;
                } else {
                    this.output = new ApiParam(tClazz);
                }
            }
        }
    }

    public String group() {
        return group;
    }
}

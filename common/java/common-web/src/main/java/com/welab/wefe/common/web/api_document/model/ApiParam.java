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
package com.welab.wefe.common.web.api_document.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zane
 * @date 2021/12/3
 */
public class ApiParam {
    public Class<?> clazz;
    public String paramTypeName;
    public List<ApiParamField> fields = new ArrayList<>();

    public ApiParam(Class<?> clazz) {
        this.clazz = clazz;
        this.paramTypeName = clazz.getSimpleName();

        for (Field field : ClassUtils.listFields(clazz)) {
            // Skip fields that are not printed
            JSONField jsonAnnotation = field.getAnnotation(JSONField.class);
            if (jsonAnnotation != null && !jsonAnnotation.serialize()) {
                continue;
            }

            Check checkAnnotation = field.getAnnotation(Check.class);
            if (checkAnnotation != null) {
                if (checkAnnotation.donotShow()) {
                    continue;
                }
            }


            fields.add(new ApiParamField(field));
        }
    }

}

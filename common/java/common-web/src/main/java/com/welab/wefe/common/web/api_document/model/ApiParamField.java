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
package com.welab.wefe.common.web.api_document.model;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author zane
 * @date 2021/12/3
 */
public class ApiParamField {
    public String name;
    public String desc = "";
    public String regex = "";
    public String typeName = "";
    public String comment = "";
    public String require = "";
    public boolean isList;

    public ApiParamField(Field field) {
        name = StringUtil.stringToUnderLineLowerCase(field.getName());

        typeName = StringUtil.substringAfterLast(field.getType().getCanonicalName(), ".");
        isList = field.getType().equals(List.class);
        if (isList) {
            Type listFieldGenericType = ClassUtils.getListFieldGenericType(field);
            typeName = "List<" + ClassUtils.getTypeSimpleName(listFieldGenericType.getClass()) + ">";
        }

        Check annotation = field.getAnnotation(Check.class);
        if (annotation == null) {
            return;
        }


        comment = annotation.name() + "";
        require = annotation.require() + "";
        desc = annotation.desc() + "";
        regex = annotation.regex() + "";
    }

}

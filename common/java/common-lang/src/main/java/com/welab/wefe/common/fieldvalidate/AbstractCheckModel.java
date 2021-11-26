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

package com.welab.wefe.common.fieldvalidate;


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Zane
 */
public abstract class AbstractCheckModel {

    public void checkAndStandardize() throws StatusCodeWithException {
        try {
            FieldValidateUtil.checkAndStandardize(this);

            for (Field field : ClassUtils.listFields(this.getClass())) {
                Class<?> type = field.getType();
                Type fieldGenericType = field.getGenericType();
                field.setAccessible(true);

                if (AbstractCheckModel.class.isAssignableFrom(type)) {
                    Object value = field.get(this);
                    if (value != null) {
                        ((AbstractCheckModel) value).checkAndStandardize();
                    }
                } else if ("List".equals(type.getSimpleName())) {
                    Class<?> tClass = ClassUtils.getGenericClass(fieldGenericType.getClass(), 0);
                    if (tClass != null && AbstractCheckModel.class.isAssignableFrom(tClass)) {
                        Object list = field.get(this);
                        if (list != null) {
                            for (Object item : (List) list) {
                                ((AbstractCheckModel) item).checkAndStandardize();
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void standardize() {
        try {
            FieldValidateUtil.standardize(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

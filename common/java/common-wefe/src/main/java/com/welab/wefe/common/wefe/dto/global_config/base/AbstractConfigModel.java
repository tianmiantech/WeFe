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
package com.welab.wefe.common.wefe.dto.global_config.base;


import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.wefe.dto.global_config.GlobalConfigFlag;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zane
 * @date 2022/5/27
 */
public abstract class AbstractConfigModel extends AbstractCheckModel {

    private static Map<String, Class<? extends AbstractConfigModel>> MODEL_CLASSES;

    /**
     * 反射获取所有 ConfigModel
     */
    static {
        List<Class<?>> classes = ReflectionsUtil.getClassesWithAnnotation(
                GlobalConfigFlag.class.getPackage().getName(),
                ConfigModel.class
        );

        MODEL_CLASSES = new HashMap<>();
        for (Class<?> clazz : classes) {
            ConfigModel annotation = clazz.getAnnotation(ConfigModel.class);
            MODEL_CLASSES.put(annotation.group(), (Class<? extends AbstractConfigModel>) clazz);
        }
    }

    public static Class<? extends AbstractConfigModel> getModelClass(String group) {
        return MODEL_CLASSES.get(group);
    }


    public static Collection<Class<? extends AbstractConfigModel>> getModelClasses() {
        return MODEL_CLASSES.values();
    }
}

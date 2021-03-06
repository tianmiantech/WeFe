/**
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

package com.welab.wefe.common.web.config;

import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.Api;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Naming of beans with custom API annotations
 *
 * @author Zane
 **/
public class ApiBeanNameGenerator extends AnnotationBeanNameGenerator {

    public static final List<String> API_LIST = new ArrayList<>();

    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {

        Class<?> clazz = ClassUtils.createClassFromName(definition.getBeanClassName());

        Api api = clazz.getAnnotation(Api.class);

        if (api == null || ClassUtils.isAbstract(clazz)) {
            return super.buildDefaultBeanName(definition);
        } else {

            List<String> pathList = new ArrayList<>();

            do {
                clazz = clazz.getSuperclass();
                Api superApi = clazz.getAnnotation(Api.class);

                if (superApi == null) {
                    continue;
                }
                pathList.add(0, StringUtil.trim(superApi.path().toLowerCase(), '/', '\\'));
            }
            while (!clazz.equals(Object.class));

            pathList.add(StringUtil.trim(api.path().toLowerCase(), '/', '\\'));
            String apiName = StringUtil.join(pathList, "/");
            API_LIST.add(apiName);
            return apiName;
        }

    }

}

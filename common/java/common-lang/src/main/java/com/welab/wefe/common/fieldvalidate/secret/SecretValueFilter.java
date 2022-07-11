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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 隐藏实体中的 @Secret 字段
 *
 * @author zane
 * @date 2022/4/27
 */
public class SecretValueFilter implements ValueFilter {
    private static final Logger LOG = LoggerFactory.getLogger(SecretValueFilter.class);

    public static final SecretValueFilter instance = new SecretValueFilter();

    private SecretValueFilter() {
    }

    @Override
    public Object process(Object object, String name, Object value) {

        Secret secret = null;
        try {
            secret = object.getClass()
                    .getDeclaredField(StringUtil.underLineCaseToCamelCase(name))
                    .getAnnotation(Secret.class);
        } catch (NoSuchFieldException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            return value;
        }
        if (secret == null) {
            return value;
        }

        return secret.maskStrategy().get(value);
    }

    public static class TestModel {
        public String username = "username";
        @Secret(maskStrategy = MaskStrategy.BLOCK)
        public String password = "password";
    }

    public static void main(String[] args) {
        TestModel model = new TestModel();
        System.out.println(JSON.toJSONString(model, SecretValueFilter.instance));
    }
}

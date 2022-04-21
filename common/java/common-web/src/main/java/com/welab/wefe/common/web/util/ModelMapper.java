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

package com.welab.wefe.common.web.util;

import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity class mapping tool
 *
 * @author Zane
 */
public class ModelMapper {
    /**
     * 根据官方文档，modelmapper 是线程安全的。
     * http://modelmapper.org/user-manual/faq/
     */
    private static final org.modelmapper.ModelMapper MAPPER = new org.modelmapper.ModelMapper();

    static {
        MAPPER.getConfiguration().setFullTypeMatchingRequired(true);
        MAPPER.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        MAPPER.getConfiguration().setMethodAccessLevel(Configuration.AccessLevel.PUBLIC);
    }

    private ModelMapper() {
    }

    public static <T> T map(Object source, Class<T> destinationType) {

        if (source == null) {
            return null;
        }

        return MAPPER.map(source, destinationType);

    }

    public static <T> List<T> maps(List<?> list, Class<T> destinationType) {
        return list
                .stream()
                .map(x -> map(x, destinationType))
                .collect(Collectors.toList());
    }
}

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

package com.welab.wefe.common.data.mysql.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Jervis
 **/
public class PageUtils {

    /**
     * Obtain the page turning object according to a single object (it is convenient to unify the object when querying according to the ID)
     */
    public static <T> Page<T> getPage(T pojo) {
        List<T> list = new ArrayList<>();
        list.add(pojo);
        return new PageImpl<>(list);
    }

    /**
     * Obtain the page turning object according to a single object (it is convenient to unify the object when querying according to the ID)
     */
    public static <T> Page<T> getPage(Optional<T> pojo) {
        return pojo.map(PageUtils::getPage).orElseGet(Page::empty);
    }
}

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

package com.welab.wefe.common.data.mysql.repository;

import com.welab.wefe.common.constant.Constant;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author Jervis
 **/
public interface MyJpaRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    default Pageable getPageable(int page, int size) {
        return getPageable(page, size, "created_time");
    }

    default Pageable getPageableForAtQuery(int page, int size) {
        return getPageable(page, size, Constant.CREATED_TIME);
    }

    default String getLikedString(String source) {
        return "%" + source + "%";
    }

    default Pageable getPageable(int page, int size, String sortBy) {
        page = page < 0 ? 0 : page;
        size = (size < 10 || size > 1000) ? 10 : size;

        return PageRequest.of(page, size, Sort.by(Sort.Order.desc(sortBy)));
    }
}

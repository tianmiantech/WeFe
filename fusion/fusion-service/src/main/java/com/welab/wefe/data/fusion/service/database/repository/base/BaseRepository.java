/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.data.fusion.service.database.repository.base;


import com.welab.wefe.data.fusion.service.dto.base.PagingInput;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Zane
 * @date 2020/5/20
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Gets a piece of data based on the specified field
     * @param key
     * @param value
     * @param clazz
     * @return
     */
    T findOne(String key, Object value, Class<T> clazz);

    /**
     * Gets a piece of data based on the specified field
     * @param key
     * @param value
     * @param clazz
     * @return
     */
    long count(String key, Object value, Class<T> clazz);

    /**
     * Update a field by Id
     * @param id
     * @param key
     * @param value
     * @param clazz
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    int updateById(String id, String key, Object value, Class<T> clazz);

    /**
     * Update multiple fields by Id
     */
    @Transactional(rollbackFor = Exception.class)
    int updateById(String id, Map<String, Object> updateParams, Class<T> clazz);

    /**
     * Update a field by Id and decide whether to include the updatedBy field based on business needs
     */
    @Transactional(rollbackFor = Exception.class)
    int updateById(String id, String key, Object value, Class<T> clazz, boolean hasUpdatedBy);

    /**
     * Paging query
     */
    PagingOutput<T> paging(@Nullable Specification<T> queryCondition, PagingInput pagingInput);

    /**
     * Paging queries and converting POJOs to Dtos
     *
     * @param <OUT> DTO
     */
    <OUT> PagingOutput<OUT> paging(@Nullable Specification<T> queryCondition, PagingInput pagingInput, Class<OUT> outputClass);


    /**
     * Default paging object
     * @param input
     * @return
     */
    default Pageable getDefaultPageable(PagingInput input) {
        return PageRequest
                .of(
                        input.getPageIndex(),
                        input.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdTime")
                );
    }

    /**
     * Based on native SQL queries
     * @param sql
     * @return
     */
    List<Object[]> query(String sql);

    /**
     * Class entities are returned based on native SQL queries
     * @param sql
     * @param clazz
     * @return
     */
    List<T> queryByClass(String sql, Class<T> clazz);

}


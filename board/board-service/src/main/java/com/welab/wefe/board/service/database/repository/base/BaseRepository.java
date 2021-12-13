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

package com.welab.wefe.board.service.database.repository.base;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
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
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 根据指定字段获取一条数据
     */
    T findOne(String key, Object value, Class<T> clazz);

    /**
     * 根据指定字段查询数据行数
     */
    long count(String key, Object value, Class<T> clazz);

    /**
     * 根据 Id 更新一个字段
     */
    @Transactional
    int updateById(String id, String key, Object value, Class<T> clazz);

    /**
     * 根据 Id 更新多个字段
     */
    @Transactional
    int updateById(String id, Map<String, Object> updateParams, Class<T> clazz);

    /**
     * 根据 Id 更新一个字段, 根据业务需要决定是否包括更新updatedBy字段
     */
    @Transactional
    int updateById(String id, String key, Object value, Class<T> clazz, boolean hasUpdatedBy);

    /**
     * 分页查询
     */
    PagingOutput<T> paging(@Nullable Specification<T> queryCondition, PagingInput pagingInput);

    /**
     * 分页查询，并将 POJO 转换为 DTO
     *
     * @param <OUT> DTO 类型
     */
    <OUT> PagingOutput<OUT> paging(@Nullable Specification<T> queryCondition, PagingInput pagingInput, Class<OUT> outputClass);


    /**
     * 默认分页对象
     */
    default Pageable getDefaultPageable(PagingInput input) {
        return PageRequest
                .of(
                        input.getPageIndex(),
                        input.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdTime")
                );
    }

    @Transactional(rollbackFor = Exception.class)
    int deleteByQuery(String sql, Class<?> clazz);

    /**
     * 根据原生sql查询
     */
    List<Object[]> query(String sql);

    /**
     * 根据原生sql查询,返回class实体
     */
    List<T> queryByClass(String sql, Class<T> clazz);

}


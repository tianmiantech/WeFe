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

package com.welab.wefe.serving.service.database.serving.repository.base;


import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Zane
 */
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private final EntityManager entityManager;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public T findOne(String key, Object value, Class<T> clazz) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(clazz);
        Root<T> root = query.from(clazz);


        query
                .select(root)
                .where(cb.equal(root.get(key), value));

        List<T> list = entityManager
                .createQuery(query)
                .setMaxResults(1)
                .getResultList();

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public long count(String key, Object value, Class<T> clazz) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(clazz);

        query
                .select(cb.count(root))
                .where(cb.equal(root.get(key), value));

        return entityManager
                .createQuery(query)
                .getSingleResult();
    }

    @Override
    public int updateById(String id, String key, Object value, Class<T> clazz) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(clazz);
        Root<T> root = update.from(clazz);

        update
                .set(root.get(key), value)
                .set(root.get("updatedTime"), new Date())
                .set(root.get("updatedBy"), CurrentAccount.id())
                .where(cb.equal(root.get("id"), id));

        return entityManager
                .createQuery(update)
                .executeUpdate();
    }

    @Override
    public PagingOutput<T> paging(@Nullable Specification<T> queryCondition, PagingInput pagingInput) {

        Page<T> page = findAll(queryCondition, getDefaultPageable(pagingInput));
        List<T> content = page.getContent();
        return PagingOutput.of(
                page.getTotalElements(),
                content
        );
    }

    @Override
    public <OUT> PagingOutput<OUT> paging(@Nullable Specification<T> queryCondition, PagingInput pagingInput, Class<OUT> outputClass) {

        Page<T> page = findAll(queryCondition, getDefaultPageable(pagingInput));
        List<T> content = page.getContent();
        return PagingOutput.of(
                page.getTotalElements(),
                content,
                outputClass
        );
    }

    @Override
    public List<Object[]> query(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    @Override
    public List<T> queryByClass(String sql, Class<T> clazz) {
        Query query = entityManager.createNativeQuery(sql, clazz);
        return query.getResultList();
    }
}

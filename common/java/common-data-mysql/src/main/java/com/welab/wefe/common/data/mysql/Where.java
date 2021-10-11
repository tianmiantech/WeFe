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

package com.welab.wefe.common.data.mysql;

import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.util.StringUtil;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Used to generate query criteria using and concatenation
 *
 * @author Zane
 */
public class Where {

    public static Where create() {
        return new Where();
    }

    private List<Item> where = new ArrayList<>();


    private Where() {
    }

    /**
     * Splice like clauses and automatically add% before and after value, such as% value%.
     */
    public Where contains(String name, String value) {

        if (StringUtil.isNotEmpty(value)) {
            where.add(new Item("contains", name, value));
        }
        return this;
    }

    /**
     * Splice like clauses and automatically add% and comma before and after value, such as%, value,%.
     * Used for inclusion checking when the target field is multiple elements separated by commas
     */
    public Where containsItem(String name, String value) {
        if (StringUtil.isNotEmpty(value)) {
            where.add(new Item("contains", name, "," + value + ","));
        }
        return this;
    }

    /**
     * Splice = clause. When value is empty, the generation of this clause will be skipped.
     *
     * @param name  Field name
     * @param value Field value
     */
    public Where equal(String name, Object value) {
        return equal(name, value, true);
    }

    /**
     * Splice = clause
     *
     * @param name               Field name
     * @param value              Field value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause when value is null
     */
    public Where equal(String name, Object value, boolean skipWhenValueEmpty) {

        if (skipWhenValueEmpty && (value == null || "".equals(value))) {
            return this;
        }

        where.add(new Item("equal", name, value));
        return this;
    }

    /**
     * Splice= Clause. When value is empty, the generation of this clause will be skipped.
     *
     * @param name  Field name
     * @param value Field value
     */
    public Where notEqual(String name, Object value) {
        return notEqual(name, value, true);
    }

    /**
     * Splice < clause. When value is empty, the generation of this clause will be skipped
     *
     * @param name  Field name
     * @param value Field value
     */
    public Where lessThan(String name, Object value) {
        return lessThan(name, value, true);
    }

    /**
     * Splice the < = clause. When value is empty, the generation of this clause will be skipped
     *
     * @param name  Field name
     * @param value Field value
     */
    public Where lessThanOrEqualTo(String name, Object value) {
        return lessThanOrEqualTo(name, value, true);
    }

    /**
     * Splice > clause. When value is empty, the generation of this clause will be skipped
     *
     * @param name  Field name
     * @param value Field value
     */
    public Where greaterThan(String name, Object value) {
        return greaterThan(name, value, true);
    }

    /**
     * Splice > = clause. When value is empty, the generation of this clause will be skipped
     *
     * @param name  Field name
     * @param value Field value
     */
    public Where greaterThanOrEqualTo(String name, Object value) {
        return greaterThanOrEqualTo(name, value, true);
    }

    /**
     * Specify time period
     */
    public Where betweenAndDate(String name, Long startTime, Long endTime) {
        if (startTime != null && startTime > 0) {
            greaterThanOrEqualTo(name, new Date(startTime), true);
        }
        if (endTime != null && endTime > 0) {
            lessThanOrEqualTo(name, new Date(endTime), true);
        }
        return this;
    }

    /**
     * Splice= clause
     *
     * @param name               Field name
     * @param value              Field value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause when value is null
     */
    public Where notEqual(String name, Object value, boolean skipWhenValueEmpty) {

        if (skipWhenValueEmpty && (value == null || "".equals(value))) {
            return this;
        }

        where.add(new Item("notEqual", name, value));
        return this;
    }

    /**
     * Splice < clause
     *
     * @param name               Field name
     * @param value              Field value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause when value is null
     */
    public Where lessThan(String name, Object value, boolean skipWhenValueEmpty) {
        if (skipWhenValueEmpty && (value == null || "".equals(value))) {
            return this;
        }

        where.add(new Item("lessThan", name, value));
        return this;
    }

    /**
     * Splice < = clause
     *
     * @param name               Field name
     * @param value              Field value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause when value is null
     */
    public Where lessThanOrEqualTo(String name, Object value, boolean skipWhenValueEmpty) {
        if (skipWhenValueEmpty && (value == null || "".equals(value))) {
            return this;
        }

        where.add(new Item("lessThanOrEqualTo", name, value));
        return this;
    }

    /**
     * Splice > clause
     *
     * @param name               Field name
     * @param value              Field value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause when value is null
     */
    public Where greaterThan(String name, Object value, boolean skipWhenValueEmpty) {
        if (skipWhenValueEmpty && (value == null || "".equals(value))) {
            return this;
        }

        where.add(new Item("greaterThan", name, value));
        return this;
    }

    /**
     * Concatenate >= clause
     *
     * @param name
     * @param value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause if value is null
     */
    public Where greaterThanOrEqualTo(String name, Object value, boolean skipWhenValueEmpty) {
        if (skipWhenValueEmpty && (value == null || "".equals(value))) {
            return this;
        }

        where.add(new Item("greaterThanOrEqualTo", name, value));
        return this;
    }

    /**
     * Concatenate the in clause. If value is null, the generation of the clause is skipped.
     *
     * @param name
     * @param value
     */
    public Where in(String name, List<Object> value) {
        return in(name, value, true);
    }

    /**
     * Splice in clause
     *
     * @param name
     * @param value
     * @param skipWhenValueEmpty Whether to skip the generation of this clause if value is null
     */
    public Where in(String name, List<Object> value, boolean skipWhenValueEmpty) {

        if (skipWhenValueEmpty && (value == null || value.isEmpty())) {
            return this;
        }

        where.add(new Item("in", name, value));
        return this;
    }

    /**
     * Concatenates the orderBy clause, which is skipped when value is null.
     *
     * @param name  The field name
     * @param value value
     */
    public Where orderBy(String name, OrderBy value) {
        if (value == null) {
            return this;
        }

        where.add(new Item("orderBy", name, value));
        return this;
    }

    public <T> Specification<T> build(Class<T> clazz) {

        Specification<T> queryCondition = (root, query, cb) -> {

            List<Predicate> list = new ArrayList<>();

            for (Item item : where) {

                switch (item.operator) {
                    case "contains":
                        list.add(cb.like(root.get(item.name), "%" + item.value + "%"));
                        break;
                    case "equal":
                        if (item.value == null) {
                            list.add(cb.isNull(root.get(item.name)));
                        } else {
                            list.add(cb.equal(root.get(item.name), item.value));
                        }

                        break;
                    case "notEqual":
                        if (item.value == null) {
                            list.add(cb.isNotNull(root.get(item.name)));
                        } else {
                            list.add(cb.notEqual(root.get(item.name), item.value));
                        }

                        break;
                    case "in":
                        CriteriaBuilder.In<Object> in = cb.in(root.get(item.name));
                        for (Object o : ((List) item.value)) {
                            in.value(o);
                        }
                        list.add(in);
                        break;
                    case "lessThan":
                        if (item.value instanceof Date) {
                            list.add(cb.lessThan(root.get(item.name), (Date) item.value));
                        } else {
                            list.add(cb.lessThan(root.get(item.name), item.value.toString()));
                        }

                        break;
                    case "lessThanOrEqualTo":
                        if (item.value instanceof Date) {
                            list.add(cb.lessThanOrEqualTo(root.get(item.name), (Date) item.value));
                        } else {
                            list.add(cb.lessThanOrEqualTo(root.get(item.name), item.value.toString()));
                        }
                        break;
                    case "greaterThan":
                        if (item.value instanceof Date) {
                            list.add(cb.greaterThan(root.get(item.name), (Date) item.value));
                        } else {
                            list.add(cb.greaterThan(root.get(item.name), item.value.toString()));
                        }

                        break;
                    case "greaterThanOrEqualTo":
                        if (item.value instanceof Date) {
                            list.add(cb.greaterThanOrEqualTo(root.get(item.name), (Date) item.value));
                        } else {
                            list.add(cb.greaterThanOrEqualTo(root.get(item.name), item.value.toString()));
                        }

                        break;
                    case "orderBy":
                        switch ((OrderBy) item.value) {
                            case asc:
                                query.orderBy(new OrderImpl(root.get(item.name)));
                                break;
                            case desc:
                                query.orderBy(new OrderImpl(root.get(item.name)).reverse());
                                break;
                            default:
                                break;
                        }
                    default:
                }
            }


            return cb.and(list.toArray(new Predicate[list.size()]));
        };

        return queryCondition;

    }

    private static class Item {
        String operator;
        String name;
        Object value;

        public Item(String operator, String name, Object value) {
            this.operator = operator;
            this.name = name;
            this.value = value;
        }
    }
}

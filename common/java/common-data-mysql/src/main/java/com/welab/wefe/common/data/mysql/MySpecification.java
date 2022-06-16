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
package com.welab.wefe.common.data.mysql;

import com.welab.wefe.common.data.mysql.enums.OrderBy;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2022/3/22
 */
public class MySpecification<T> implements Specification<T> {
    private List<Where.Item> items = new ArrayList<>();


    MySpecification(List<Where.Item> items) {
        this.items = items;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> list = new ArrayList<>();

        for (Where.Item item : items) {

            switch (item.operator) {
                case "contains":
                    list.add(criteriaBuilder.like(root.get(item.name), "%" + item.value + "%"));
                    break;
                case "groupBy":
                    List<Expression<?>> pathList = new ArrayList<>();
                    for (String var : (List<String>) item.value) {
                        pathList.add(root.get(var));
                    }
                    query.groupBy(pathList);
                    break;
                case "equal":
                    if (item.value == null) {
                        list.add(criteriaBuilder.isNull(root.get(item.name)));
                    } else {
                        list.add(criteriaBuilder.equal(root.get(item.name), item.value));
                    }

                    break;
                case "notEqual":
                    if (item.value == null) {
                        list.add(criteriaBuilder.isNotNull(root.get(item.name)));
                    } else {
                        list.add(criteriaBuilder.notEqual(root.get(item.name), item.value));
                    }

                    break;
                case "in":
                    CriteriaBuilder.In<Object> in = criteriaBuilder.in(root.get(item.name));
                    for (Object o : ((List) item.value)) {
                        in.value(o);
                    }
                    list.add(in);
                    break;
                case "lessThan":
                    if (item.value instanceof Date) {
                        list.add(criteriaBuilder.lessThan(root.get(item.name), (Date) item.value));
                    } else {
                        list.add(criteriaBuilder.lessThan(root.get(item.name), item.value.toString()));
                    }

                    break;
                case "lessThanOrEqualTo":
                    if (item.value instanceof Date) {
                        list.add(criteriaBuilder.lessThanOrEqualTo(root.get(item.name), (Date) item.value));
                    } else {
                        list.add(criteriaBuilder.lessThanOrEqualTo(root.get(item.name), item.value.toString()));
                    }
                    break;
                case "greaterThan":
                    if (item.value instanceof Date) {
                        list.add(criteriaBuilder.greaterThan(root.get(item.name), (Date) item.value));
                    } else {
                        list.add(criteriaBuilder.greaterThan(root.get(item.name), item.value.toString()));
                    }

                    break;
                case "greaterThanOrEqualTo":
                    if (item.value instanceof Date) {
                        list.add(criteriaBuilder.greaterThanOrEqualTo(root.get(item.name), (Date) item.value));
                    } else {
                        list.add(criteriaBuilder.greaterThanOrEqualTo(root.get(item.name), item.value.toString()));
                    }

                    break;
                case "orderBy":
                    OrderBy orderBy = (OrderBy) item.value;
                    // 在现有 order by 子句的基础上追加
                    List<Order> orders = new ArrayList<>(query.getOrderList());
                    orders.add(new OrderImpl(root.get(item.name), orderBy == OrderBy.asc));
                    query.orderBy(orders);
                    break;

                default:
            }
        }

        return criteriaBuilder.and(list.toArray(new Predicate[list.size()]));
    }

    public Sort getSort() {
        List<Sort.Order> list = this.items.stream()
                .filter(x -> "orderBy".equals(x.operator))
                .map(x -> {
                    OrderBy orderBy = (OrderBy) x.value;
                    switch (orderBy) {
                        case desc:
                            return Sort.Order.desc(x.name);
                        case asc:
                            return Sort.Order.asc(x.name);
                        default:
                            return null;
                    }

                }).collect(Collectors.toList());

        return Sort.by(list);
    }
}

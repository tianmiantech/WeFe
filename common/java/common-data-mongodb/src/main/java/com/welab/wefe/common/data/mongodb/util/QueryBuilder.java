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

package com.welab.wefe.common.data.mongodb.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Encapsulate the most commonly used query conditions
 *
 * @author yuxin.zhang
 **/
public class QueryBuilder {

    private Map<String, Object> eqMap = new LinkedHashMap<>();
    private Map<String, Object> noteqMap = new LinkedHashMap<>();
    private Map<String, Boolean> sortMap = new LinkedHashMap<>();
    private Map<String, Boolean> existMap = new LinkedHashMap<>();
    private Map<String, Object> lteMap = new LinkedHashMap<>();
    private Map<String, Object> gteMap = new LinkedHashMap<>();
    private Map<String, List<?>> inMap = new LinkedHashMap<>();
    private Map<String, String> likeMap = new LinkedHashMap<>();


    private long[] timeBetween = new long[2];
    private long timeWithin;


    private Integer pageSize;
    private Integer pageIndex;

    /**
     * Less than or equal to
     */
    public QueryBuilder lte(String key, Object value) {
        lteMap.put(key, value);
        return this;
    }

    /**
     * Greater than or equal to
     */
    public QueryBuilder gte(String key, Object value) {
        gteMap.put(key, value);
        return this;
    }

    /**
     * Not equal to
     */
    public QueryBuilder notEq(String key, Object value) {
        noteqMap.put(key, value);
        return this;
    }

    public QueryBuilder append(String key, Object value) {
        eqMap.put(key, value);
        return this;
    }

    public QueryBuilder notRemoved() {
        eqMap.put("status", 0);
        return this;
    }

    public QueryBuilder page(Integer pageIndex, Integer pageSize) {
        this.pageIndex = pageIndex == null || pageIndex < 0 ? 0 : pageIndex;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * Within a few days, mutually exclusive calls with between
     */
    public QueryBuilder withinDays(int days) {
        timeWithin = System.currentTimeMillis() / 1000 - days * 24 * 60 * 60;
        return this;
    }

    /**
     * Within a few minutes, mutually exclusive calls with between
     */
    public QueryBuilder withinMinutes(int minutes) {
        timeWithin = System.currentTimeMillis() / 1000 - minutes * 60;
        return this;
    }

    /**
     * Mutually exclusive calls with in days between time periods
     */
    public QueryBuilder between(long start, long end) {
        timeBetween[0] = start;
        timeBetween[1] = end;
        return this;
    }

    public QueryBuilder sort(String key, boolean asc) {
        sortMap.put(key, asc);
        return this;
    }

    public QueryBuilder sort(String key) {
        return sort(key, false);
    }

    public QueryBuilder exist(String key, boolean isExist) {
        existMap.put(key, isExist);
        return this;
    }

    public QueryBuilder in(String key, List<?> collection) {
        inMap.put(key, collection);
        return this;
    }

    public QueryBuilder like(String key, String value) {
        likeMap.put(key, value);
        return this;
    }

    public Criteria getCriteria() {
        Criteria criteria = new Criteria();

        eqMap.forEach((k, v) -> {
            if (null != v) {
                if(v instanceof String){
                    if(StringUtils.isNotEmpty(v.toString())){
                        criteria.and(k).is(v);
                    }
                } else {
                    criteria.and(k).is(v);
                }
            }
        });
        existMap.forEach((k, v) -> {
            if (null != v) {
                criteria.and(k).exists(v);
            }
        });
        noteqMap.forEach((k, v) -> {
            if (null != v) {
                criteria.and(k).ne(v);
            }
        });
        gteMap.forEach((k, v) -> {
            if (null != v) {
                criteria.and(k).gte(v);
            }
        });
        lteMap.forEach((k, v) -> {
            if (null != v) {
                criteria.and(k).lte(v);
            }
        });
        inMap.forEach((k, v) -> {
            if (null != v && v.size() > 0) {
                criteria.and(k).in(v);
            }
        });
        likeMap.forEach((k, v) -> {
            if (StringUtils.isNotEmpty(v)) {
                criteria.and(k).regex(".*?" + v + ".*");
            }
        });
        if (Arrays.stream(timeBetween).allMatch(time -> time > 0)) {
            criteria.and("logTime").gt(timeBetween[0]).lt(timeBetween[1]);
        }

        if (timeWithin > 0) {
            criteria.and("logTime").gt(timeWithin);
        }

        return criteria;
    }

    public Query build() {

        Query query = Query.query(getCriteria());
        sortMap.forEach((k, v) -> query.with(Sort.by(v ? Sort.Order.asc(k) : Sort.Order.desc(k))));
        if (pageIndex != null && pageSize != null) {
            pageSize = pageSize == 0 ? 10 : pageSize;
            query.limit(pageSize);
            query.skip(pageIndex * pageSize);
        }

        return query;
    }

}

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

package com.welab.wefe.board.service.database.entity.fusion.bloomfilter;

import com.alibaba.fastjson.JSONObject;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.wefe.enums.ColumnDataType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author jacky.jiang
 */
@Entity(name = "bloom_filter_column")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class BloomFilterColumnMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 过滤器Id
     */
    private String bloomFilterId;
    /**
     * 字段序号
     */
    @Column(name = "`index`")
    private Integer index;
    /**
     * 字段名称
     */
    private String name;
    /**
     * 数据类型
     */
    @Enumerated(EnumType.STRING)
    private ColumnDataType dataType;
    /**
     * 注释
     */
    private String comment;
    /**
     * 空值数据行数
     */
    private Long emptyRows;
    /**
     * 数值分布
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject valueDistribution;

    //region getter/setter


    public String getBloomFilterId() {
        return bloomFilterId;
    }

    public void setBloomFilterId(String bloomFilterId) {
        this.bloomFilterId = bloomFilterId;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColumnDataType dataType) {
        this.dataType = dataType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getEmptyRows() {
        return emptyRows;
    }

    public void setEmptyRows(Long emptyRows) {
        this.emptyRows = emptyRows;
    }

    public JSONObject getValueDistribution() {
        return valueDistribution;
    }

    public void setValueDistribution(JSONObject valueDistribution) {
        this.valueDistribution = valueDistribution;
    }


    //endregion
}

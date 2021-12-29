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

package com.welab.wefe.board.service.dto.entity.data_set;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.ColumnDataType;

import java.util.Map;

/**
 * @author Zane
 */
public class DataSetColumnOutputModel extends AbstractOutputModel {

    @Check(name = "数据集Id")
    private String dataSetId;
    @Check(name = "字段序号")
    private Integer index;
    @Check(name = "字段名称")
    private String name;
    @Check(name = "数据类型")
    private ColumnDataType dataType;
    @Check(name = "注释")
    private String comment;
    @Check(name = "空值数据行数")
    private Long emptyRows;
    @Check(name = "数值分布")
    private Map<String, Object> valueDistribution;


    //region getter/setter

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
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

    public Map<String, Object> getValueDistribution() {
        return valueDistribution;
    }

    public void setValueDistribution(Map<String, Object> valueDistribution) {
        this.valueDistribution = valueDistribution;
    }


    //endregion

}

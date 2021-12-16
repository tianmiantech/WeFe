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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataResourceExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.TableDataSetExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = MongodbTable.Union.TABLE_DATASET)
public class TableDataSet extends AbstractBlockChainBusinessModel {
    private String dataResourceId;
    private String containsY;
    private String columnCount;
    private String columnNameList;
    private String featureCount;
    private String featureNameList;
    private TableDataSetExtJSON extJson = new TableDataSetExtJSON();

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getContainsY() {
        return containsY;
    }

    public void setContainsY(String containsY) {
        this.containsY = containsY;
    }

    public String getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(String columnCount) {
        this.columnCount = columnCount;
    }

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public String getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(String featureCount) {
        this.featureCount = featureCount;
    }

    public String getFeatureNameList() {
        return featureNameList;
    }

    public void setFeatureNameList(String featureNameList) {
        this.featureNameList = featureNameList;
    }

    public TableDataSetExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(TableDataSetExtJSON extJson) {
        this.extJson = extJson;
    }
}

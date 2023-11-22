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

package com.welab.wefe.data.fusion.service.dto.entity.bloomfilter;

import com.welab.wefe.data.fusion.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.enums.DataResourceType;
import com.welab.wefe.data.fusion.service.enums.Progress;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
public class BloomfilterOutputModel extends AbstractOutputModel {

    private String name;

    private String dataSourceId;

    /**
     * description
     */
    private String description;

    /**
     * Data set source
     */
    @Enumerated(EnumType.STRING)
    private DataResourceSource dataResourceSource;


    /**
     * Data article number
     */
    private Integer rowCount = 0;

    /**
     * Use the number
     */
    private int usedCount = 0;

    /**
     * The SQL statement
     */
    private String statement;

    /**
     * Select the column
     */
    private String rows;

    /**
     * The progress bar number
     */
    private Integer processCount;

    /**
     * Progress status
     */
    private Progress process;

    /**
     * 主键hash方式
     */
    private String hashFunction;

    DataResourceType type = DataResourceType.BloomFilter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataResourceSource getDataResourceSource() {
        return dataResourceSource;
    }

    public void setDataResourceSource(DataResourceSource dataResourceSource) {
        this.dataResourceSource = dataResourceSource;
    }


    public DataResourceType getType() {
        return type;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }


    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public void setType(DataResourceType type) {
        this.type = type;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Progress getProcess() {
        return process;
    }

    public void setProcess(Progress process) {
        this.process = process;
    }

    public String getHashFunction() {
        return hashFunction;
    }

    public void setHashFunction(String hashFunction) {
        this.hashFunction = hashFunction;
    }
}

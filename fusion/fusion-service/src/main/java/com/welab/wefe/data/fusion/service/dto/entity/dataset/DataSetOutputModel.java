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

package com.welab.wefe.data.fusion.service.dto.entity.dataset;

import com.welab.wefe.data.fusion.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.enums.DataResourceType;
import com.welab.wefe.data.fusion.service.enums.Progress;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
public class DataSetOutputModel extends AbstractOutputModel {
    private String name;

    private int rowCount;

    private String dataSourceId;
    /**
     * description
     */
    private String description;

    /**
     * Use the number
     */
    private int usedCount;

    /**
     * Whether saved to database (dataset)/file (filter)
     */
    private boolean isStoraged = false;

    /**
     * Data set source
     */
    @Enumerated(EnumType.STRING)
    private DataResourceSource dataResourceSource;

    /**
     * The SQL statement
     */
    private String statement;

    /**
     * Source file path
     */
    private String sourcePath;

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

    DataResourceType type = DataResourceType.DataSet;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
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

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public boolean isStoraged() {
        return isStoraged;
    }

    public void setStoraged(boolean storaged) {
        isStoraged = storaged;
    }

    public DataResourceSource getDataResourceSource() {
        return dataResourceSource;
    }

    public void setDataResourceSource(DataResourceSource dataResourceSource) {
        this.dataResourceSource = dataResourceSource;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public DataResourceType getType() {
        return type;
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
}

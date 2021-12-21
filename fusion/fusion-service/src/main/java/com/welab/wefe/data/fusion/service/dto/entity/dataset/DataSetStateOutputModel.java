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

package com.welab.wefe.data.fusion.service.dto.entity.dataset;

import com.welab.wefe.data.fusion.service.dto.entity.AbstractOutputModel;

/**
 * @author hunter.zhao
 */
public class DataSetStateOutputModel extends AbstractOutputModel {
    private String dataSourceId;

    private Integer rowCount;

    private Integer processCount;


    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }
}
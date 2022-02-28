/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.data.fusion.service.database.entity.DataSetColumnOutputModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 * @date 2022/2/22
 */
public class DataSetPreviewOutputModel {

    @Check(name = "字段列表")
    private List<String> header = new ArrayList<>();
    @Check(name = "元数据信息")
    private List<DataSetColumnOutputModel> metadataList = new ArrayList<>();
    @Check(name = "原始数据列表")
    private List<Map<String, Object>> rawDataList = new ArrayList<>();

    //region
    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    public List<Map<String, Object>> getRawDataList() {
        return rawDataList;
    }

    public void setRawDataList(List<Map<String, Object>> rawDataList) {
        this.rawDataList = rawDataList;
    }

    public List<DataSetColumnOutputModel> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<DataSetColumnOutputModel> metadataList) {
        this.metadataList = metadataList;
    }

    //endregion
}

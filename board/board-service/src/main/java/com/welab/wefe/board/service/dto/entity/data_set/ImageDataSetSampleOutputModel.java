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
package com.welab.wefe.board.service.dto.entity.data_set;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;

/**
 * @author zane
 * @date 2021/11/12
 */
public class ImageDataSetSampleOutputModel extends AbstractOutputModel {

    @Check(name = "数据集id")
    private String dataSetId;
    @Check(name = "文件名")
    private String fileName;
    @Check(name = "文件路径")
    private String filePath;
    @Check(name = "文件大小")
    private long fileSize;
    @Check(name = "label")
    private String labelList;
    @Check(name = "是否已标注")
    private boolean labeled;
    @Check(name = "json 形式的标注信息")
    private JSONObject labelInfo;

    public String getLabelList() {
        // 移除前后的逗号，不然前端会报错。
        return StringUtil.trim(labelList, ',');
    }

    // region getter/setter

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setLabelList(String labelList) {
        this.labelList = labelList;
    }

    public boolean isLabeled() {
        return labeled;
    }

    public void setLabeled(boolean labeled) {
        this.labeled = labeled;
    }

    public JSONObject getLabelInfo() {
        return labelInfo;
    }

    public void setLabelInfo(JSONObject labelInfo) {
        this.labelInfo = labelInfo;
    }


    // endregion
}

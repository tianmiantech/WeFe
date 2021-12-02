/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.database.entity.data_set;

import com.alibaba.fastjson.JSONObject;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Zane
 */
@Entity(name = "image_data_set_sample")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class ImageDataSetSampleMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 数据集id
     */
    private String dataSetId;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 文件大小
     */
    private long fileSize;
    /**
     * label
     */
    private String labelList;
    /**
     * 是否已标注
     */
    private boolean labeled;
    /**
     * json 形式的标注信息
     */
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private JSONObject labelInfo;

    /**
     * xml 形式的标注信息
     */
    private String xmlAnnotation;

    //region getter/setter

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

    public String getLabelList() {
        return labelList;
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

    public String getXmlAnnotation() {
        return xmlAnnotation;
    }

    public void setXmlAnnotation(String xmlAnnotation) {
        this.xmlAnnotation = xmlAnnotation;
    }

    //endregion
}

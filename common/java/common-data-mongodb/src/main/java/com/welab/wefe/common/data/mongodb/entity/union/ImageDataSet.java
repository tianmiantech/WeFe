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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.ImageDataSetExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.IMAGE_DATASET)
public class ImageDataSet extends AbstractBlockChainBusinessModel {
    private String dataSetId;
    private String memberId;
    private String name;
    private String tags;
    private String description;
    private String storageType;
    private String forJobType;
    private String labelList;
    private String sampleCount;
    private String labeledCount;
    private String completed;
    private String filesSize;
    private String publicLevel;
    private String publicMemberList;
    private String usageCountInJob;
    private String usageCountInFlow;
    private String usageCountInProject;
    private String enable;
    private ImageDataSetExtJSON extJson;


    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getForJobType() {
        return forJobType;
    }

    public void setForJobType(String forJobType) {
        this.forJobType = forJobType;
    }

    public String getLabelList() {
        return labelList;
    }

    public void setLabelList(String labelList) {
        this.labelList = labelList;
    }

    public String getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(String sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getLabeledCount() {
        return labeledCount;
    }

    public void setLabeledCount(String labeledCount) {
        this.labeledCount = labeledCount;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(String filesSize) {
        this.filesSize = filesSize;
    }

    public String getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(String publicLevel) {
        this.publicLevel = publicLevel;
    }

    public String getPublicMemberList() {
        return publicMemberList;
    }

    public void setPublicMemberList(String publicMemberList) {
        this.publicMemberList = publicMemberList;
    }

    public String getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(String usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public String getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(String usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public String getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(String usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public ImageDataSetExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(ImageDataSetExtJSON extJson) {
        this.extJson = extJson;
    }
}

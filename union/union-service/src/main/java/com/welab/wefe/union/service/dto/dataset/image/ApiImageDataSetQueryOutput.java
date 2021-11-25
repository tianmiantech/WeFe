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

package com.welab.wefe.union.service.dto.dataset.image;

import com.welab.wefe.common.data.mongodb.entity.union.ext.ImageDataSetExtJSON;
import com.welab.wefe.common.web.dto.AbstractTimedApiOutput;

/**
 * @author yuxin.zhang
 **/
public class ApiImageDataSetQueryOutput extends AbstractTimedApiOutput {
    private String dataSetId;
    private String memberId;
    private String memberName;
    private String name;
    private String tags;
    private String description;
    private String forJobType;
    private String labelList;
    private Integer sampleCount;
    private Integer labeledCount;
    private String labelCompleted;
    private String filesSize;
    private String publicLevel;
    private String publicMemberList;
    private Integer usageCountInJob;
    private Integer usageCountInFlow;
    private Integer usageCountInProject;
    private ImageDataSetExtJSON extJson;

    public ApiImageDataSetQueryOutput() {
    }

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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
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

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Integer getLabeledCount() {
        return labeledCount;
    }

    public void setLabeledCount(Integer labeledCount) {
        this.labeledCount = labeledCount;
    }

    public String getLabelCompleted() {
        return labelCompleted;
    }

    public void setLabelCompleted(String labelCompleted) {
        this.labelCompleted = labelCompleted;
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

    public Integer getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(Integer usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public Integer getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(Integer usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public Integer getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(Integer usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
    }

    public ImageDataSetExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(ImageDataSetExtJSON extJson) {
        this.extJson = extJson;
    }
}

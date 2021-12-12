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
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = MongodbTable.Union.DATA_RESOURCE)
public class DataResource extends AbstractBlockChainBusinessModel {
    private String dataResourceId;
    private String memberId;
    private String name;
    private String description;
    private String tags;
    private String totalDataCount;
    private String publicLevel;
    private String publicMemberList;
    private String usageCountInJob;
    private String usageCountInFlow;
    private String usageCountInProject;
    private String usageCountInMember;
    private String enable;
    private String dataResourceType;

    private DataResourceExtJSON extJson = new DataResourceExtJSON();

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(String totalDataCount) {
        this.totalDataCount = totalDataCount;
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

    public String getUsageCountInMember() {
        return usageCountInMember;
    }

    public void setUsageCountInMember(String usageCountInMember) {
        this.usageCountInMember = usageCountInMember;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(String dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public DataResourceExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataResourceExtJSON extJson) {
        this.extJson = extJson;
    }
}

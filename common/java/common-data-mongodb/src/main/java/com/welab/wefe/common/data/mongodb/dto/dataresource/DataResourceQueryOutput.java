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

package com.welab.wefe.common.data.mongodb.dto.dataresource;

import com.welab.wefe.common.data.mongodb.entity.union.BloomFilter;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.enums.DataResourceType;

/**
 * @author yuxin.zhang
 **/
public class DataResourceQueryOutput {
    private String dataResourceId;
    private String memberId;
    private String name;
    private String memberName;
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
    private DataResourceType dataResourceType;
    private String createdTime;
    private String updatedTime;
    private ImageDataSet imageDataSet;
    private TableDataSet tableDataSet;
    private BloomFilter bloomFilter;

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

    public ImageDataSet getImageDataSet() {
        return imageDataSet;
    }

    public void setImageDataSet(ImageDataSet imageDataSet) {
        this.imageDataSet = imageDataSet;
    }

    public TableDataSet getTableDataSet() {
        return tableDataSet;
    }

    public void setTableDataSet(TableDataSet tableDataSet) {
        this.tableDataSet = tableDataSet;
    }

    public BloomFilter getBloomFilter() {
        return bloomFilter;
    }

    public void setBloomFilter(BloomFilter bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }


    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}

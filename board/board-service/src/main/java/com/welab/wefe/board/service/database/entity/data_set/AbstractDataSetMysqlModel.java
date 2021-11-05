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

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.DataSetPublicLevel;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane
 * @date 2021/11/5
 */
public class AbstractDataSetMysqlModel extends AbstractBaseMySqlModel {
    /**
     * 数据集名称
     */
    private String name;
    /**
     * 标签
     */
    private String tags;
    /**
     * 描述
     */
    private String description;
    /**
     * 存储类型
     */
    private String storageType;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 数据集的可见性
     */
    @Enumerated(EnumType.STRING)
    private DataSetPublicLevel publicLevel;
    /**
     * 可见成员列表，只有在列表中的联邦成员才可以看到该数据集的基本信息
     */
    private String publicMemberList;
    /**
     * 使用次数
     */
    private Integer usageCountInJob = 0;
    /**
     * 使用次数
     */
    private Integer usageCountInFlow = 0;
    /**
     * 使用次数
     */
    private Integer usageCountInProject = 0;

    // region getter/setter

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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public DataSetPublicLevel getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(DataSetPublicLevel publicLevel) {
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


    // endregion
}

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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.DATA_RESOURCE_LAZY_UPDATE_MODEL)
public class DataResourceLazyUpdateModel extends AbstractNormalMongoModel {
    private String dataResourceId;
    private String labelList;
    private int totalDataCount;
    private int labeledCount;
    private boolean labelCompleted;

    private int usageCountInJob;
    private int usageCountInFlow;
    private int usageCountInProject;
    private int usageCountInMember;

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getLabelList() {
        return labelList;
    }

    public void setLabelList(String labelList) {
        this.labelList = labelList;
    }

    public int getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(int totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public int getLabeledCount() {
        return labeledCount;
    }

    public void setLabeledCount(int labeledCount) {
        this.labeledCount = labeledCount;
    }


    public int getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(int usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public int getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(int usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public int getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(int usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
    }

    public int getUsageCountInMember() {
        return usageCountInMember;
    }

    public void setUsageCountInMember(int usageCountInMember) {
        this.usageCountInMember = usageCountInMember;
    }

    public boolean isLabelCompleted() {
        return labelCompleted;
    }

    public void setLabelCompleted(boolean labelCompleted) {
        this.labelCompleted = labelCompleted;
    }
}

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

package com.welab.wefe.common.data.mongodb.dto.dataresource;

import com.welab.wefe.common.data.mongodb.dto.PageInput;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;

import java.util.List;


/**
 * @author yuxin.zhang
 **/
public class DataResourceQueryInput extends PageInput {
    private String dataResourceId;
    private String memberName;
    private String name;
    private String tag;
    private String curMemberId;
    private String memberId;
    private List<DataResourceType> dataResourceType;
    private DeepLearningJobType forJobType;
    private Boolean containsY;
    private Boolean enable;
    private Boolean status;

    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCurMemberId() {
        return curMemberId;
    }

    public void setCurMemberId(String curMemberId) {
        this.curMemberId = curMemberId;
    }

    public List<DataResourceType> getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(List<DataResourceType> dataResourceType) {
        this.dataResourceType = dataResourceType;
    }
    public DeepLearningJobType getForJobType() {
        return forJobType;
    }

    public void setForJobType(DeepLearningJobType forJobType) {
        this.forJobType = forJobType;
    }

    public Boolean getContainsY() {
        return containsY;
    }

    public void setContainsY(Boolean containsY) {
        this.containsY = containsY;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}

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

import com.welab.wefe.common.data.mongodb.dto.PageInput;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;


/**
 * @author yuxin.zhang
 **/
public class DataResourceQueryInput extends PageInput {
    protected String dataResourceId;
    protected String memberName;
    protected String name;
    protected String tag;
    protected String curMemberId;
    protected String memberId;
    protected DataResourceType dataResourceType;
    private DeepLearningJobType forJobType;
    private Boolean containsY;
    protected String enable;

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

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
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
}

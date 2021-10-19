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

package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.manager.service.dto.base.PageInput;

/**
 * @author yuxin.zhang
 */
public class ApiDataSetQueryInput extends PageInput {
    private String id;
    private String memberId;
    private String memberName;
    private String name;
    private Boolean containsY;
    private String tag;

    private DataSetExtJSON extJson;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getContainsY() {
        return containsY;
    }

    public void setContainsY(Boolean containsY) {
        this.containsY = containsY;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


    public DataSetExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataSetExtJSON extJson) {
        this.extJson = extJson;
    }
}

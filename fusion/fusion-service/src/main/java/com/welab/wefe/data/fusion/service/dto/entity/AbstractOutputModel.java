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

package com.welab.wefe.data.fusion.service.dto.entity;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.data.fusion.service.service.CacheObjects;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author Zane
 * @date 2020/5/21 10:39
 */
public class AbstractOutputModel extends AbstractApiOutput {

    /**
     * Globally unique identifier
     */
    private String id;
    /**
     * founder
     */
    private String createdBy;
    /**
     * Creation time
     */
    private Date createdTime;
    /**
     * Update one
     */
    private String updatedBy;
    /**
     * Update time
     */
    private Date updatedTime;


    @Check(name = "创建者昵称")
    private String creatorNickname;

    @Check(name = "修改者昵称")
    private String updaterNickname;


    //region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        this.creatorNickname = CacheObjects.getNickname(createdBy);
        if (StringUtils.isBlank(this.creatorNickname)) {
            this.creatorNickname = CacheObjects.getMemberName();
        }
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        this.updaterNickname = CacheObjects.getNickname(updatedBy);
        if (StringUtils.isBlank(this.updaterNickname)) {
            this.updaterNickname = CacheObjects.getMemberName();
        }
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatorNickname() {
        return creatorNickname;
    }

    public void setCreatorNickname(String creatorNickname) {
        this.creatorNickname = creatorNickname;
    }

    public String getUpdaterNickname() {
        return updaterNickname;
    }

    public void setUpdaterNickname(String updaterNickname) {
        this.updaterNickname = updaterNickname;
    }

    //endregion
}

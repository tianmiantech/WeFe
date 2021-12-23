/*
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

package com.welab.wefe.board.service.dto.union;

import java.util.Date;
import java.util.List;

/**
 * @author zane.luo
 */
public class UnionDataSetOutput {
    private String memberId;
    private Date createdTime;
    private Date updatedTime;
    private Integer dimensionCount;
    private String description;
    private String tags;
    private boolean containsY;
    private int useCount;
    private String name;
    private Long sampleCount;
    private String id;
    private List<String> dimensions;

    //region getter/setter

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Integer getDimensionCount() {
        return dimensionCount;
    }

    public void setDimensionCount(Integer dimensionCount) {
        this.dimensionCount = dimensionCount;
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

    public boolean isContainsY() {
        return containsY;
    }

    public void setContainsY(boolean containsY) {
        this.containsY = containsY;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<String> dimensions) {
        this.dimensions = dimensions;
    }


    //endregion
}

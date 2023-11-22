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

package com.welab.wefe.serving.sdk.model.xgboost;

/**
 * @author hunter.zhao
 */
public class XgboostNodeModel {
    private Integer id = 0;
    private Integer fid;
    private Double bid;
    private String sitename;
    private Double weight;
    private Integer leftNodeId;
    private Integer rightNodeId;
    private Integer missingDir;
    private boolean isLeaf = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getLeftNodeId() {
        return leftNodeId;
    }

    public void setLeftNodeId(Integer leftNodeId) {
        this.leftNodeId = leftNodeId;
    }

    public Integer getRightNodeId() {
        return rightNodeId;
    }

    public void setRightNodeId(Integer rightNodeId) {
        this.rightNodeId = rightNodeId;
    }

    public Integer getMissingDir() {
        return missingDir;
    }

    public void setMissingDir(Integer missingDir) {
        this.missingDir = missingDir;
    }

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }
}

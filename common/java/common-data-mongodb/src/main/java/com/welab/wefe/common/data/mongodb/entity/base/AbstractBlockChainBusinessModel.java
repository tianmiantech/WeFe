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

package com.welab.wefe.common.data.mongodb.entity.base;

import com.welab.wefe.common.util.DateUtil;

import java.util.Date;

/**
 * @author yuxin.zhang
 **/
public class AbstractBlockChainBusinessModel extends AbstractMongoModel {

    /**
     * Data synchronization time
     */
    private long dataSyncTime = System.currentTimeMillis();
    /**
     * Data status (1: deleted, 0: not deleted)
     */
    private int status;


    private String createdTime = DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());

    private String updatedTime = DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());

    public long getDataSyncTime() {
        return dataSyncTime;
    }

    public void setDataSyncTime(long dataSyncTime) {
        this.dataSyncTime = dataSyncTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
}

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

package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.common.util.DateUtil;

import javax.persistence.*;
import java.util.Date;

/**
 * @author hunter.zhao
 */
@Entity(name = "predict_statistics")
public class PredictStatisticsMySqlModel {

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "model_id")
    private String modelId;

    private String month;

    private String day;

    private String hour;

    private String minute;

    private long total;

    private long success;

    private long fail;


    @Column(name = "created_time")
    private Date createdTime = new Date();

    @Column(name = "updated_time")
    private Date updatedTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFail() {
        return fail;
    }

    public void setFail(long fail) {
        this.fail = fail;
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

    /**
     * Set additional time fields by minute timestamp
     */
    public void setDateFields(String minute) {
        this.month = DateUtil.toString(DateUtil.fromString(minute, DateUtil.YYYY_MM), DateUtil.YYYY_MM);
        this.day = DateUtil.toString(DateUtil.fromString(minute, DateUtil.YYYY_MM_DD), DateUtil.YYYY_MM_DD);
        this.hour = DateUtil.toString(DateUtil.fromString(minute, DateUtil.YYYY_MM_DD_HH), DateUtil.YYYY_MM_DD_HH);
    }
}

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

package com.welab.wefe.serving.service.database.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

/**
 * @author hunter.zhao
 */
@Entity(name = "model_predict_score_statistics")
public class ModelPredictScoreStatisticsMySqlModel extends AbstractBaseMySqlModel {
    @Column(name = "service_id")
    private String serviceId;

    private Date day;

    private Double splitPoint;

    private int count = 0;


    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public Double getSplitPoint() {
        return splitPoint;
    }

    public void setSplitPoint(Double splitPoint) {
        this.splitPoint = splitPoint;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

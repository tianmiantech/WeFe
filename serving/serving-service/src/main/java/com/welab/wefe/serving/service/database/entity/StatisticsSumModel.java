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
package com.welab.wefe.serving.service.database.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author hunter.zhao
 */
@Entity
public class StatisticsSumModel {
    @Id
    private Double splitPoint;

    private Integer count;

    public Double getSplitPoint() {
        return splitPoint;
    }

    public void setSplitPoint(Double splitPoint) {
        this.splitPoint = splitPoint;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}

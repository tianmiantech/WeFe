package com.welab.wefe.board.service.database.entity.fusion;

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


import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author hunter.zhao
 */
@Entity(name = "fusion_actuator_info")
public class FusionActuatorInfoMySqlModel extends AbstractBaseMySqlModel {
    String type;

    @Enumerated(EnumType.STRING)
    FusionTaskStatus status;

    int progress;

    String businessId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FusionTaskStatus getStatus() {
        return status;
    }

    public void setStatus(FusionTaskStatus status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
}

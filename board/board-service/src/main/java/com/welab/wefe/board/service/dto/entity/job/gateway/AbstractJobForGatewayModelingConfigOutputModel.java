/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.dto.entity.job.gateway;

import com.welab.wefe.common.enums.FederatedLearningType;

import javax.persistence.MappedSuperclass;

/**
 * @author seven.zeng
 */
@MappedSuperclass
public class AbstractJobForGatewayModelingConfigOutputModel {

    /**
     * 联邦学习模式
     */
    private FederatedLearningType flType;

    //region getter/setter

    public FederatedLearningType getFlType() {
        return flType;
    }

    public void setFlType(FederatedLearningType flType) {
        this.flType = flType;
    }


    //endregion

}

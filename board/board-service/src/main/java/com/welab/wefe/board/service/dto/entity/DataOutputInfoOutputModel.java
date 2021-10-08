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

package com.welab.wefe.board.service.dto.entity;

/**
 * @author aaron.li
 **/
public class DataOutputInfoOutputModel extends AbstractOutputModel {
    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 模型id
     */
    private String partyModelId;
    /**
     * 模型版本
     */
    private String modelVersion;

    public String getPartyModelId() {
        return partyModelId;
    }

    public void setPartyModelId(String partyModelId) {
        this.partyModelId = partyModelId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}

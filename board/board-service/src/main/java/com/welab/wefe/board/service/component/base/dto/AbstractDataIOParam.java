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
package com.welab.wefe.board.service.component.base.dto;

import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;

import java.util.List;

/**
 * @author zane
 * @date 2021/11/24
 */
public abstract class AbstractDataIOParam<T extends AbstractDataSetItem> extends AbstractCheckModel {
    public List<T> dataSetList;

    // region getter/setter

    public List<T> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<T> dataSetList) {
        this.dataSetList = dataSetList;
    }


    // endregion
}

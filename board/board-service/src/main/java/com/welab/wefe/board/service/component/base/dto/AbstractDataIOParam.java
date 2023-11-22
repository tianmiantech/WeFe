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
package com.welab.wefe.board.service.component.base.dto;

import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.deep_learning.ImageDataIOComponent;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author zane
 * @date 2021/11/24
 */
public abstract class AbstractDataIOParam<T extends AbstractDataSetItem> extends AbstractCheckModel {
    public List<T> dataSetList;

    public T getMyJobDataSetItem(JobMemberRole role) {
        if (CollectionUtils.isEmpty(dataSetList)) {
            return null;
        }

        return dataSetList.stream()
                .filter(x -> CacheObjects.getMemberId().equals(x.memberId) && role == x.getMemberRole())
                .findFirst()
                .orElse(null);
    }

    public DataResourceOutputModel getMyJobDataSet(JobMemberRole role) throws StatusCodeWithException {
        T dataSetItem = getMyJobDataSetItem(role);
        if (dataSetItem == null) {
            return null;
        }

        if (dataSetItem instanceof ImageDataIOComponent.DataSetItem) {
            return Launcher.CONTEXT
                    .getBean(ImageDataSetService.class)
                    .findDataSetFromLocalOrUnion(
                            dataSetItem.getMemberId(),
                            dataSetItem.getDataSetId()
                    );

        } else if (dataSetItem instanceof DataIOComponent.DataSetItem) {
            return Launcher.CONTEXT
                    .getBean(TableDataSetService.class)
                    .findDataSetFromLocalOrUnion(
                            dataSetItem.getMemberId(),
                            dataSetItem.getDataSetId()
                    );
        }

        return null;
    }

    // region getter/setter

    public List<T> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<T> dataSetList) {
        this.dataSetList = dataSetList;
    }


    // endregion
}

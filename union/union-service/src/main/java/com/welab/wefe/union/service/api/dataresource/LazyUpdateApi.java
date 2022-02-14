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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceLazyUpdateModel;
import com.welab.wefe.common.data.mongodb.repo.DataResourceLazyUpdateModelMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "data_resource/lazy_update", name = "image_data_set_update_labeled_count", rsaVerify = true, login = false)
public class LazyUpdateApi extends AbstractApi<LazyUpdateApi.Input, AbstractApiOutput> {
    @Autowired
    private ImageDataSetMongoReop imageDataSetMongoReop;

    @Autowired
    private DataResourceLazyUpdateModelMongoReop dataResourceLazyUpdateModelMongoReop;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        ImageDataSet imageDataSet = imageDataSetMongoReop.findByDataResourceId(input.getDataResourceId());
        if (imageDataSet == null) {
            throw new StatusCodeWithException(StatusCode.INVALID_DATASET, input.getDataResourceId());
        }

        if ("1".equals(imageDataSet.getLabelCompleted())) {
            return success();
        }

        saveImageDataSetLabeledCount(input);

        return success();
    }

    private void saveImageDataSetLabeledCount(Input input) {
        DataResourceLazyUpdateModel dataResourceLazyUpdateModel = dataResourceLazyUpdateModelMongoReop.findByDataResourceId(input.getDataResourceId());
        if (dataResourceLazyUpdateModel == null) {
            dataResourceLazyUpdateModel = new DataResourceLazyUpdateModel();
        }
        dataResourceLazyUpdateModel.setDataResourceId(input.getDataResourceId());
        dataResourceLazyUpdateModel.setLabeledCount(input.getLabeledCount());
        dataResourceLazyUpdateModel.setTotalDataCount(input.getTotalDataCount());
        dataResourceLazyUpdateModel.setLabelList(input.getLabelList());
        dataResourceLazyUpdateModelMongoReop.save(dataResourceLazyUpdateModel);
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String dataResourceId;
        @Check(require = true)
        private String labelList;
        @Check(require = true)
        private int totalDataCount;
        @Check(require = true)
        private int labeledCount;

        public String getDataResourceId() {
            return dataResourceId;
        }

        public void setDataResourceId(String dataResourceId) {
            this.dataResourceId = dataResourceId;
        }


        public String getLabelList() {
            return labelList;
        }

        public void setLabelList(String labelList) {
            this.labelList = labelList;
        }

        public int getTotalDataCount() {
            return totalDataCount;
        }

        public void setTotalDataCount(int totalDataCount) {
            this.totalDataCount = totalDataCount;
        }

        public int getLabeledCount() {
            return labeledCount;
        }

        public void setLabeledCount(int labeledCount) {
            this.labeledCount = labeledCount;
        }
    }
}

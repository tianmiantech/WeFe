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

package com.welab.wefe.union.service.api.dataset.image;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSetLabeledCount;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetLabeledCountMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.ImageDataSetContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "image_data_set/update_labeled_count", name = "image_data_set_update_labeled_count", rsaVerify = true, login = false)
public class UpdateLabeledCountApi extends AbstractApi<UpdateLabeledCountApi.Input, AbstractApiOutput> {
    @Autowired
    private ImageDataSetMongoReop imageDataSetMongoReop;

    @Autowired
    private ImageDataSetLabeledCountMongoReop imageDataSetLabeledCountMongoReop;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        ImageDataSet imageDataSet = imageDataSetMongoReop.findDataSetId(input.getDataSetId());
        if (imageDataSet == null) {
            throw new StatusCodeWithException(StatusCode.INVALID_DATASET, input.getDataSetId());
        }

        if ("1".equals(imageDataSet.getLabelCompleted())) {
            return success();
        }

        saveImageDataSetLabeledCount(input);

        return success();
    }

    private void saveImageDataSetLabeledCount(Input input){
        ImageDataSetLabeledCount imageDataSetLabeledCount = new ImageDataSetLabeledCount();
        imageDataSetLabeledCount.setDataSetId(input.getDataSetId());
        imageDataSetLabeledCount.setLabeledCount(input.getLabeledCount());
        imageDataSetLabeledCount.setSampleCount(input.getSampleCount());
        imageDataSetLabeledCount.setLabelList(input.getLabelList());
        imageDataSetLabeledCountMongoReop.save(imageDataSetLabeledCount);
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String dataSetId;
        @Check(require = true)
        private String labelList;
        @Check(require = true)
        private int sampleCount;
        @Check(require = true)
        private int labeledCount;

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }


        public String getLabelList() {
            return labelList;
        }

        public void setLabelList(String labelList) {
            this.labelList = labelList;
        }

        public int getSampleCount() {
            return sampleCount;
        }

        public void setSampleCount(int sampleCount) {
            this.sampleCount = sampleCount;
        }

        public int getLabeledCount() {
            return labeledCount;
        }

        public void setLabeledCount(int labeledCount) {
            this.labeledCount = labeledCount;
        }
    }
}

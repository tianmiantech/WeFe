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

package com.welab.wefe.board.service.api.data_resource.image_data_set.sample;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.ImageDataSetSampleOutputModel;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetSampleService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "image_data_set_sample/query", name = "query image data set samples")
public class ImageDataSetSampleQueryApi extends AbstractApi<ImageDataSetSampleQueryApi.Input, PagingOutput<ImageDataSetSampleOutputModel>> {

    @Autowired
    private ImageDataSetSampleService imageDataSetSampleService;

    @Override
    protected ApiResult<PagingOutput<ImageDataSetSampleOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(imageDataSetSampleService.query(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "数据集Id")
        private String dataSetId;

        @Check(name = "标签名称")
        private String label;

        @Check(name = "标签名称使用模糊匹配")
        public boolean labelMatchWithContains = true;

        @Check(name = "是否已标注")
        private Boolean labeled;

        //region getter/setter


        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Boolean getLabeled() {
            return labeled;
        }

        public void setLabeled(Boolean labeled) {
            this.labeled = labeled;
        }


        //endregion
    }
}

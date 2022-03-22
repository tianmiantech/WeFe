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

package com.welab.wefe.data.fusion.service.api.bloomfilter;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.dto.entity.bloomfilter.BloomfilterDetailOutputModel;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetDetailOutputModel;
import com.welab.wefe.data.fusion.service.service.bloomfilter.BloomFilterService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "filter/detail_and_preview", name = "过滤器详情预览", desc = "过滤器详情预览")
public class DetailAndPreviewApi extends AbstractApi<DetailAndPreviewApi.Input, BloomfilterDetailOutputModel> {
    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<BloomfilterDetailOutputModel> handle(Input input) throws Exception {
        return success(bloomFilterService.detailAndPreview(input.getId()));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "数据id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
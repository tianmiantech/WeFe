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
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.dto.base.PagingInput;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.entity.bloomfilter.BloomfilterOutputModel;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.service.bloomfilter.BloomFilterService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jacky.Jiang
 */
@Api(path = "filter/query", name = "查询过滤器列表")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<BloomfilterOutputModel>> {

    @Autowired
    private BloomFilterService bloomFilterService;


    @Override
    protected ApiResult<PagingOutput<BloomfilterOutputModel>> handle(Input input) {
        return success(bloomFilterService.query(input));
    }

    public static class Input extends PagingInput {
        private String id;

        @Check(name = "过滤器名称")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Output {

        private String id;

        @Check(name = "过滤器名称")
        private String name;

        private DataResourceSource dataResourceSource;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DataResourceSource getDataResourceSource() {
            return dataResourceSource;
        }

        public void setDataResourceSource(DataResourceSource dataResourceSource) {
            this.dataResourceSource = dataResourceSource;
        }


    }
}

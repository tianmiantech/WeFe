/**
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

package com.welab.wefe.data.fusion.service.api.bloomfilter;


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.service.bloomfilter.GetBloomFilterStateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


/**
 * @author jacky.jiang
 */
@Api(path = "filter/get_state", name = "获取过滤器当前状态", desc = "获取过滤器当前状态", login = true)
public class GetBloomFilterStateApi extends AbstractApi<GetBloomFilterStateApi.Input, GetBloomFilterStateApi.Output> {

    @Autowired
    private GetBloomFilterStateService getBloomFilterStateService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        return success(getBloomFilterStateService.getStatue(input));
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "布隆过滤器id")
        private String BloomFilterId;

        public String getBloomFilterId() {
            return BloomFilterId;
        }

        public void setBloomFilterId(String bloomFilterId) {
            BloomFilterId = bloomFilterId;
        }
    }

    public static class Output extends AbstractApiOutput {
        private String bloomFilterId;

        private Integer rowCount;

        private Integer processCount;

        private Progress progress;

        public Output() {

        }

        public String getBloomFilterId() {
            return bloomFilterId;
        }

        public void setBloomFilterId(String bloomFilterId) {
            this.bloomFilterId = bloomFilterId;
        }

        public Integer getRowCount() {
            return rowCount;
        }

        public void setRowCount(Integer rowCount) {
            this.rowCount = rowCount;
        }

        public Integer getProcessCount() {
            return processCount;
        }

        public void setProcessCount(Integer processCount) {
            this.processCount = processCount;
        }

        public Progress getProgress() {
            return progress;
        }

        public void setProgress(Progress progress) {
            this.progress = progress;
        }
    }
}



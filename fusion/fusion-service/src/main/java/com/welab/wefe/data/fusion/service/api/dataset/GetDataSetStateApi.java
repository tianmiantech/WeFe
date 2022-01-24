/*
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

package com.welab.wefe.data.fusion.service.api.dataset;


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.service.dataset.GetDataSetStateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;


/**
 * @author jacky.jiang
 */
@Api(path = "data_set/get_state", name = "获取数据集当前状态", desc = "获取数据集当前状态", login = true)
public class GetDataSetStateApi extends AbstractApi<GetDataSetStateApi.Input, GetDataSetStateApi.Output> {

    @Autowired
    private GetDataSetStateService getDataSetStateService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        return success(getDataSetStateService.getStatue(input));
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "数据源id")
        private String dataSetId;

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }
    }

    public static class Output extends AbstractApiOutput {
        private String dataSetId;

        private Integer rowCount;

        private Integer processCount;

        private Progress progress;

        public Output() {

        }

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
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



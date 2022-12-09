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

package com.welab.wefe.union.service.api.dataresource.dataset.table;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.service.TableDataSetService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "table_data_set/put", name = "table_data_set", allowAccessWithSign = true)
public class PutApi extends AbstractApi<PutApi.Input, AbstractApiOutput> {
    @Autowired
    private TableDataSetService tableDataSetService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        tableDataSetService.add(input);
        return success();
    }

    public static class Input extends DataResourcePutInput {
        @Check(require = true)
        private boolean containsY;
        private int columnCount;
        private String columnNameList;
        private int featureCount;
        private String featureNameList;

        public boolean isContainsY() {
            return containsY;
        }

        public void setContainsY(boolean containsY) {
            this.containsY = containsY;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public String getColumnNameList() {
            return columnNameList;
        }

        public void setColumnNameList(String columnNameList) {
            this.columnNameList = columnNameList;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(int featureCount) {
            this.featureCount = featureCount;
        }

        public String getFeatureNameList() {
            return featureNameList;
        }

        public void setFeatureNameList(String featureNameList) {
            this.featureNameList = featureNameList;
        }
    }
}

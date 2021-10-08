/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.data.fusion.service.api.task;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.DataResourceType;
import com.welab.wefe.data.fusion.service.service.TaskService;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(path = "task/handle", name = "任务处理", desc = "任务处理", login = true)
public class HandleApi extends AbstractNoneOutputApi<HandleApi.Input> {

    @Autowired
    TaskService taskService;

    @Override
    protected ApiResult handler(HandleApi.Input input) throws StatusCodeWithException {
        taskService.handle(input);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", require = true)
        private String id;


        @Check(name = "数据资源id", require = true)
        private String dataResourceId;

        @Check(name = "数据资源类型", require = true)
        private DataResourceType dataResourceType;

        @Check(name = "样本量", require = true)
        private Integer rowCount;

        @Check(name = "主键处理")
        private List<FieldInfo> fieldInfoList;

        @Check(name = "是否追溯", require = true)
        private Boolean isTrace;

        @Check(name = "追溯字段")
        private String traceColumn;


        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (DataResourceType.DataSet.equals(dataResourceType) && fieldInfoList.isEmpty()) {
                throw new StatusCodeWithException("请设置主键", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (isTrace && StringUtil.isEmpty(traceColumn)) {
                throw new StatusCodeWithException("追溯字段不能为空", StatusCode.PARAMETER_VALUE_INVALID);
            }

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDataResourceId() {
            return dataResourceId;
        }

        public void setDataResourceId(String dataResourceId) {
            this.dataResourceId = dataResourceId;
        }

        public DataResourceType getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(DataResourceType dataResourceType) {
            this.dataResourceType = dataResourceType;
        }

        public Integer getRowCount() {
            return rowCount;
        }

        public void setRowCount(Integer rowCount) {
            this.rowCount = rowCount;
        }

        public List<FieldInfo> getFieldInfoList() {
            return fieldInfoList;
        }

        public void setFieldInfoList(List<FieldInfo> fieldInfoList) {
            this.fieldInfoList = fieldInfoList;
        }

        public Boolean getTrace() {
            return isTrace;
        }

        public void setTrace(Boolean trace) {
            isTrace = trace;
        }

        public String getTraceColumn() {
            return traceColumn;
        }

        public void setTraceColumn(String traceColumn) {
            this.traceColumn = traceColumn;
        }
    }

}

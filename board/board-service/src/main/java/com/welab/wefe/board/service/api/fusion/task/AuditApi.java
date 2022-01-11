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

package com.welab.wefe.board.service.api.fusion.task;

import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.board.service.util.primarykey.FieldInfo;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/task/audit", name = "任务处理", desc = "任务处理")
public class AuditApi extends AbstractNoneOutputApi<AuditApi.Input> {

    @Autowired
    FusionTaskService fusionTaskService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        fusionTaskService.handle(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "businessId", require = true)
        private String businessId;

        @Check(name = "主键处理")
        private List<FieldInfo> fieldInfoList;

        @Check(name = "是否追溯")
        private Boolean isTrace = false;

        @Check(name = "追溯字段")
        private String traceColumn;

        @Check(name = "审核字段", require = true)
        private AuditStatus auditStatus;

        @Check(name = "审核评论")
        private String auditComment;


        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

//            if (DataResourceType.DataSet.equals(dataResourceType) && fieldInfoList.isEmpty()) {
//                throw new StatusCodeWithException("请设置主键", StatusCode.PARAMETER_VALUE_INVALID);
//            }

            if (isTrace && StringUtil.isEmpty(traceColumn)) {
                throw new StatusCodeWithException("追溯字段不能为空", StatusCode.PARAMETER_VALUE_INVALID);
            }

        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
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

        public AuditStatus getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(AuditStatus auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getAuditComment() {
            return auditComment;
        }

        public void setAuditComment(String auditComment) {
            this.auditComment = auditComment;
        }
    }

}

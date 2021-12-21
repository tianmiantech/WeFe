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

package com.welab.wefe.board.service.api.project.dataset;

import com.welab.wefe.board.service.service.ProjectDataSetAuditService;
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

/**
 * @author zane.luo
 */
@Api(path = "project/data_resource/audit", name = "audit the data set authorization application in the project")
public class AuditDataSetApi extends AbstractNoneOutputApi<AuditDataSetApi.Input> {

    @Autowired
    ProjectDataSetAuditService projectDataSetAuditService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        projectDataSetAuditService.auditDataSet(input);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "项目ID", require = true)
        private String projectId;

        @Check(name = "数据集ID", require = true)
        private String dataSetId;

        @Check(name = "状态", require = true)
        private AuditStatus auditStatus;
        @Check(name = "审核意见")
        private String auditComment;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (auditStatus == AuditStatus.disagree && StringUtil.isEmpty(auditComment)) {
                throw new StatusCodeWithException("请填写拒绝原因哦~", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
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

        //endregion

    }

}

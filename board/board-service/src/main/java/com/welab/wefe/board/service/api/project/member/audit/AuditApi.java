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

package com.welab.wefe.board.service.api.project.member.audit;

import com.welab.wefe.board.service.service.ProjectMemberAuditService;
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
@Api(path = "project/member/add/audit", name = "audit newly added project members")
public class AuditApi extends AbstractNoneOutputApi<AuditApi.Input> {

    @Autowired
    ProjectMemberAuditService projectMemberAuditService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        projectMemberAuditService.auditMember(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "项目ID", require = true)
        private String projectId;

        @Check(name = "被审核的成员ID", require = true)
        private String memberId;

        @Check(name = "审核结果", require = true)
        private AuditStatus auditResult;

        @Check(name = "审批意见")
        private String auditComment;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (auditResult == AuditStatus.disagree && StringUtil.isEmpty(auditComment)) {
                throw new StatusCodeWithException("请陈述您拒绝的理由", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        //region getter/setter

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public AuditStatus getAuditResult() {
            return auditResult;
        }

        public void setAuditResult(AuditStatus auditResult) {
            this.auditResult = auditResult;
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

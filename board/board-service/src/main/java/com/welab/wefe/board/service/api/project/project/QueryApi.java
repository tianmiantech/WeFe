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

package com.welab.wefe.board.service.api.project.project;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.project.ProjectQueryOutputModel;
import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/query", name = "Query project list")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<ProjectQueryOutputModel>> {

    @Autowired
    ProjectService service;

    @Override
    protected ApiResult<PagingOutput<ProjectQueryOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(service.query(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "项目名称")
        private String name;

        @Check(name = "合作方id")
        private String memberId;

        @Check(name = "审核状态")
        private AuditStatus auditStatus;

        @Check(name = "起始创建时间")
        private String startCreateTime;

        @Check(name = "结束创建时间")
        private String endCreateTime;

        @Check(name = "我方角色")
        private JobMemberRole myRole;

        @Check(name = "参与方角色")
        private JobMemberRole memberRole;

        @Check(name = "是否已退出")
        private Boolean exited;

        @Check(name = "是否已关闭")
        private Boolean closed;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public AuditStatus getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(AuditStatus auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getStartCreateTime() {
            return startCreateTime;
        }

        public void setStartCreateTime(String startCreateTime) {
            this.startCreateTime = startCreateTime;
        }

        public String getEndCreateTime() {
            return endCreateTime;
        }

        public void setEndCreateTime(String endCreateTime) {
            this.endCreateTime = endCreateTime;
        }

        public JobMemberRole getMyRole() {
            return myRole;
        }

        public void setMyRole(JobMemberRole myRole) {
            this.myRole = myRole;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        public Boolean getExited() {
            return exited;
        }

        public void setExited(Boolean exited) {
            this.exited = exited;
        }

        public Boolean getClosed() {
            return closed;
        }

        public void setClosed(Boolean closed) {
            this.closed = closed;
        }
    }
}

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

package com.welab.wefe.board.service.api.project.project;

import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author zane.luo
 */
@Api(path = "project/count_statistics", name = "Count the number of projects")
public class CountStatisticsApi extends AbstractApi<QueryApi.Input, CountStatisticsApi.Output> {

    @Autowired
    ProjectService projectService;

    @Override
    protected ApiResult<Output> handle(QueryApi.Input input) throws StatusCodeWithException {
        return success(projectService.statistics(input));
    }

    public static class Output {
        private long total;
        private Map<JobMemberRole, Long> byRole;
        private Map<AuditStatus, Long> byAuditStatus;

        public Output() {
        }

        public Output(long total, Map<JobMemberRole, Long> byRole, Map<AuditStatus, Long> byAuditStatus) {
            this.total = total;
            this.byRole = byRole;
            this.byAuditStatus = byAuditStatus;
        }

        //region getter/setter

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public Map<JobMemberRole, Long> getByRole() {
            return byRole;
        }

        public void setByRole(Map<JobMemberRole, Long> byRole) {
            this.byRole = byRole;
        }

        public Map<AuditStatus, Long> getByAuditStatus() {
            return byAuditStatus;
        }

        public void setByAuditStatus(Map<AuditStatus, Long> byAuditStatus) {
            this.byAuditStatus = byAuditStatus;
        }

//endregion
    }
}

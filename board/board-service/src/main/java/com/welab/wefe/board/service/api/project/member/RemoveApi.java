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

package com.welab.wefe.board.service.api.project.member;

import com.welab.wefe.board.service.service.ProjectService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "project/member/remove", name = "remove project member")
public class RemoveApi extends AbstractNoneOutputApi<RemoveApi.Input> {

    @Autowired
    ProjectService service;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        service.removeMember(input);
        return success();
    }


    public static class Input extends AbstractApiInput {
        @Check(name = "项目ID", require = true)
        private String projectId;

        @Check(name = "成员ID", require = true)
        private String memberId;

        @Check(name = "成员角色", require = true, desc = "由于存在自己和自己联邦的情况，所以需要指定角色。")
        private JobMemberRole memberRole;


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

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        //endregion

    }

}

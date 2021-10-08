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

package com.welab.wefe.board.service.api.project.member;

import com.welab.wefe.board.service.dto.entity.ProjectMemberInput;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.ProjectMemberService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 */
@Api(path = "project/member/add", name = "add project member")
public class AddApi extends AbstractNoneOutputApi<AddApi.Input> {

    @Autowired
    private ProjectMemberService service;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        service.addMember(input);
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "项目ID", require = true)
        private String projectId;

        @Check(name = "合作方列表", require = true)
        private List<ProjectMemberInput> memberList;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public List<ProjectMemberInput> getMemberList() {
            return memberList;
        }

        public void setMemberList(List<ProjectMemberInput> memberList) {
            this.memberList = memberList;
        }

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (CollectionUtils.isEmpty(memberList)) {
                throw new StatusCodeWithException("请选择成员！", StatusCode.PARAMETER_VALUE_INVALID);
            }

            for (ProjectMemberInput item : memberList) {
                if (CacheObjects.getMemberName(item.getMemberId()) == null) {
                    throw new StatusCodeWithException("错误的 memberId：" + item.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
                }
            }
        }
    }

}

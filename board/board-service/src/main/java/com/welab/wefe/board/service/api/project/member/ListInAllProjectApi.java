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

package com.welab.wefe.board.service.api.project.member;

import com.welab.wefe.board.service.database.repository.ProjectMemberRepository;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Api(path = "project/member/all", name = "Get a list of all the members who work with me")
public class ListInAllProjectApi extends AbstractApi<ListInAllProjectApi.Input, ListInAllProjectApi.Output> {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        List<String> list = projectMemberRepository.listAllMemberId();

        List<Member> output = list
                .parallelStream()
                .map(x -> new Member(x, CacheObjects.getMemberName(x)))
                .filter(x -> StringUtil.isNotEmpty(x.memberName))
                .sorted(Comparator.comparing(x -> x.memberName == null ? "" : x.memberName))
                .collect(Collectors.toList());

        return success(new Output(output));
    }

    public static class Input extends AbstractApiInput {

    }

    public static class Output {
        private List<Member> list;

        public Output(List<Member> list) {
            this.list = list;
        }

        public List<Member> getList() {
            return list;
        }

        public void setList(List<Member> list) {
            this.list = list;
        }
    }

    public static class Member {
        public String memberId;
        public String memberName;

        public Member() {
        }

        public Member(String memberId, String memberName) {
            this.memberId = memberId;
            this.memberName = memberName;
        }
    }
}

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

package com.welab.wefe.serving.service.api.member;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "member/delete", name = "Delete Federation member")
public class DeleteApi extends AbstractNoneOutputApi<DeleteApi.Input> {
    @Autowired
    MemberRepository memberRepository;

    @Override
    protected ApiResult<?> handler(Input input) {
        memberRepository.deleteById(input.getId());
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "id")
        private String id;


        //region getter/setter


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        //endregion
    }

}

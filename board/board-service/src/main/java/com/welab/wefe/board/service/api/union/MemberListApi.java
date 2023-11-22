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

package com.welab.wefe.board.service.api.union;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "union/member/query", name = "Query members from union")
public class MemberListApi extends AbstractApi<MemberListApi.Input, Object> {

    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<Object> handle(Input input) throws StatusCodeWithException {
        JSONObject result = unionService.queryMembers(input);
        return unionApiResultToBoardApiResult(result);
    }

    public static class Input extends PagingInput {
        @Check(name = "member 名称")
        private String name;

        @Check(name = "member id")
        private String id;

        //region getter/setter

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
//endregion

    }
}

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

package com.welab.wefe.board.service.api.union.member_auth;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "union/member/authtype/query", name = "Query member authtype from union")
public class MemberAuthTypeQueryApi extends AbstractApi<AbstractApiInput, Object> {

    @Autowired
    UnionService unionService;

    @Override
    protected ApiResult<Object> handle(AbstractApiInput input) throws StatusCodeWithException {
        JSONObject result = unionService.queryMemberAuthTypeList();
        return unionApiResultToBoardApiResult(result);
    }


}

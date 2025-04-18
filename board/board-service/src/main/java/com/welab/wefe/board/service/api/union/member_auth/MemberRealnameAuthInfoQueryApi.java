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
package com.welab.wefe.board.service.api.union.member_auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.board.service.service.CertOperationService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/3
 */
@Api(path = "union/member/realname/authInfo/query", name = "realname auth agreement template query")
public class MemberRealnameAuthInfoQueryApi extends AbstractApi<AbstractApiInput, Object> {
    @Autowired
    private UnionService unionService;
    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<Object> handle(AbstractApiInput input) throws StatusCodeWithException, IOException {
        JSONObject result = unionService.realnameAuthInfoQuery();
        int code = result.getInteger("code");
        if (code == 0) {
            try {
                JSONObject data = result.getJSONObject("data");
                String certPemContent = data.getString("cert_pem_content");
                String certRequestId = data.getString("cert_request_id");
                String certStatus = data.getString("cert_status");
                certOperationService.saveCertInfo(certRequestId, certPemContent, certStatus);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return unionApiResultToBoardApiResult(result);
    }
}

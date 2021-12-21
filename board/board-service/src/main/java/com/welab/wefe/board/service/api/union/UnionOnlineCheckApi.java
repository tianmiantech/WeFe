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

package com.welab.wefe.board.service.api.union;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lonnie
 */
@Api(path = "union/online/check", name = "Check the access status of the union")
public class UnionOnlineCheckApi extends AbstractNoneInputApi<UnionOnlineCheckApi.OutPut> {

    @Autowired
    private UnionService unionService;

    @Autowired
    private Config config;

    @Override
    protected ApiResult<OutPut> handle() throws StatusCodeWithException {

        OutPut outPut = new OutPut();
        outPut.setUnionUrl(config.getUNION_BASE_URL());

        try {

            JSONObject unionResult = unionService.queryMember(0, 10);
            int code = unionResult.getInteger("code");

            outPut.setStatus(code == 0);
        } catch (StatusCodeWithException e) {
            outPut.setStatus(false);
            return success(outPut);
        }

        return success(outPut);
    }

    public static class OutPut {

        private String unionUrl;

        private boolean status;

        public String getUnionUrl() {
            return unionUrl;
        }

        public void setUnionUrl(String unionUrl) {
            this.unionUrl = unionUrl;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }
    }
}

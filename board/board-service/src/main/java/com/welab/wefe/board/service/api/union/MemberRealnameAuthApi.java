/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.api.union;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @author zane
 * @date 2021/11/2
 */
@Api(path = "union/member/realname/auth", name = "apply realname auth")
public class MemberRealnameAuthApi extends AbstractApi<MemberRealnameAuthApi.Input, Object> {

    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<Object> handle(MemberRealnameAuthApi.Input input) throws StatusCodeWithException, IOException {
        JSONObject result = unionService.realnameAuth(input);
        return super.unionApiResultToBoardApiResult(result);
    }

    public static class Input extends AbstractApiInput {
        private String principalName;
        private String authType;
        private String description;
        private List<String> fileIdList;

        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String principalName) {
            this.principalName = principalName;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getFileIdList() {
            return fileIdList;
        }

        public void setFileIdList(List<String> fileIdList) {
            this.fileIdList = fileIdList;
        }
    }
}

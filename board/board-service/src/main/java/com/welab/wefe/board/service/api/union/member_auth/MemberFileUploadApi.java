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
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "union/member/file/upload", name = "upload file")
public class MemberFileUploadApi extends AbstractApi<MemberFileUploadApi.Input, Object> {
    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<Object> handle(Input input) throws StatusCodeWithException {
        JSONObject result = unionService.uploadFile(
                input.files,
                JObject.create("filename", input.filename).append("purpose", input.purpose)
        );

        return super.unionApiResultToBoardApiResult(result);

    }

    public static class Input extends AbstractWithFilesApiInput {
        private String filename;
        private String purpose;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }
    }

}

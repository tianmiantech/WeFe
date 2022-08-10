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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.cert.CertRequestInfoMysqlModel;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.board.service.service.CertOperationService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author zane
 * @date 2021/11/2
 */
@Api(path = "union/member/realname/auth", name = "apply realname auth")
public class MemberRealnameAuthApi extends AbstractApi<MemberRealnameAuthApi.Input, Object> {

    @Autowired
    private UnionService unionService;
    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<Object> handle(MemberRealnameAuthApi.Input input) throws StatusCodeWithException, IOException {
        certOperationService.resetCert();// 将本地证书置为无效
        // 生成csr
        generateCertRequestContent(input);
        JSONObject result = unionService.realnameAuth(input);
        return super.unionApiResultToBoardApiResult(result);
    }

    // 生成csr
    private void generateCertRequestContent(Input input) throws StatusCodeWithException {
        try {
            CertRequestInfoMysqlModel model = certOperationService.createCertRequestInfo(input.getPrincipalName(),
                    input.getOrganizationName(), "IT");
            input.setCertRequestContent(model.getCertRequestContent());
            input.setCertRequestId(model.getId());
        } catch (Exception e) {
            LOG.error("generateCertRequestContent error ", e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public static class Input extends AbstractApiInput {
        private String principalName;
        private String authType;
        private String description;
        private List<String> fileIdList;
        private String organizationName;
        private String provinceCityName;
        private String email;
        private String certRequestContent;
        private String certRequestId;

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

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }

        public String getProvinceCityName() {
            return provinceCityName;
        }

        public void setProvinceCityName(String provinceCityName) {
            this.provinceCityName = provinceCityName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCertRequestContent() {
            return certRequestContent;
        }

        public void setCertRequestContent(String certRequestContent) {
            this.certRequestContent = certRequestContent;
        }

        public String getCertRequestId() {
            return certRequestId;
        }

        public void setCertRequestId(String certRequestId) {
            this.certRequestId = certRequestId;
        }
    }
}

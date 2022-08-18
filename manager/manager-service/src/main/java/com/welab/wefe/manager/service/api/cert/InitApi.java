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
package com.welab.wefe.manager.service.api.cert;

import org.springframework.beans.factory.annotation.Autowired;

import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.api.cert.InitApi.InitCertInput;

@Api(path = "cert/init_root", name = "init root cert")
public class InitApi extends AbstractApi<InitCertInput, AbstractApiOutput> {

    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(InitCertInput input) throws Exception {
        // 初始化根证书
        CertVO rootCert = certOperationService.initRootCert(input.getCommonName(), input.getOrganizationName(),
                input.getOrganizationUnitName());
        // 初始化issuer签发证书
        certOperationService.createIssuerCert(rootCert.getPkId(), "Welab", input.getOrganizationName(),
                input.getOrganizationUnitName());
        return success();
    }

    public static class InitCertInput extends AbstractApiInput {
        @Check(name = "常用名", require = true)
        private String commonName;
        @Check(name = "组织名称", require = true)
        private String organizationName;
        @Check(name = "组织单位名称", require = true)
        private String organizationUnitName;

        public String getCommonName() {
            return commonName;
        }

        public void setCommonName(String commonName) {
            this.commonName = commonName;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }

        public String getOrganizationUnitName() {
            return organizationUnitName;
        }

        public void setOrganizationUnitName(String organizationUnitName) {
            this.organizationUnitName = organizationUnitName;
        }

    }
}

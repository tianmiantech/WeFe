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

import com.webank.cert.mgr.model.vo.CertRequestVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.api.cert.DetailCsrApi.CsrDetailInput;

@Api(path = "csr/detail", name = "detail csr")
public class DetailCsrApi extends AbstractApi<CsrDetailInput, CertRequestVO> {

    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<CertRequestVO> handle(CsrDetailInput input) throws Exception {
        CertRequestVO vo = certOperationService.findCertRequestById(input.getCsrId());
        return success(vo);
    }

    public static class CsrDetailInput extends AbstractApiInput {
        @Check(require = true)
        private String csrId;

        public String getCsrId() {
            return csrId;
        }

        public void setCsrId(String csrId) {
            this.csrId = csrId;
        }

    }

}

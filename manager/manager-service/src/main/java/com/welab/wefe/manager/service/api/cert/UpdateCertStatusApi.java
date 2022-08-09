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

import com.webank.cert.mgr.enums.CertStatusEnums;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.api.cert.UpdateCertStatusApi.CertDetailInput;
import com.welab.wefe.manager.service.service.MemberContractService;

/**
 * 证书详情
 * 
 * @author winter.zou
 *
 */
@Api(path = "cert/update_status", name = "update cert status")
public class UpdateCertStatusApi extends AbstractApi<CertDetailInput, CertVO> {
    @Autowired
    protected MemberContractService memberContractService;
    @Autowired
    private CertOperationService certOperationService;
    @Autowired
    private MemberMongoReop memberMongoReop;

    @Override
    protected ApiResult<CertVO> handle(CertDetailInput input) throws Exception {
        CertVO vo = certOperationService.queryCertInfoByCertId(input.getCertId());
        if (vo == null) {
            throw new StatusCodeWithException("数据不存在", StatusCode.DATA_NOT_FOUND);
        }
        if (vo.getIsCACert() || vo.getIsRootCert()) {
            throw new StatusCodeWithException("非法操作", StatusCode.ILLEGAL_REQUEST);
        }
        if (vo.getStatus() == input.getStatus()) {
            throw new StatusCodeWithException("非法操作", StatusCode.ILLEGAL_REQUEST);
        }
        vo.setStatus(input.getStatus());
        certOperationService.updateStatus(vo.getSerialNumber(), input.getStatus());
        // 同步到区块链
        Member member = memberMongoReop.findMemberId(vo.getUserId());
        if (member == null) {
            throw new StatusCodeWithException("成员不存在", StatusCode.DATA_NOT_FOUND);
        }
        MemberExtJSON memberExtJSON = member.getExtJson();
        memberExtJSON.setCertStatus(CertStatusEnums.getStatus(input.getStatus()).name());
        memberContractService.updateExtJson(vo.getUserId(), memberExtJSON);
        return success(vo);
    }

    public static class CertDetailInput extends AbstractApiInput {
        @Check(require = true)
        private String certId;

        @Check(require = true)
        private int status;

        public String getCertId() {
            return certId;
        }

        public void setCertId(String certId) {
            this.certId = certId;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

    }

}

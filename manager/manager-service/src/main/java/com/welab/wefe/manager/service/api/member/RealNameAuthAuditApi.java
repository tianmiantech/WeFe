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

package com.welab.wefe.manager.service.api.member;

import org.springframework.beans.factory.annotation.Autowired;

import com.webank.cert.mgr.enums.CertStatusEnums;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.RealNameAuthInput;
import com.welab.wefe.manager.service.service.MemberContractService;

/**
 * @author yuxin.zhang
 */
@Api(path = "member/realname/auth/audit", name = "member_realname_auth_audit")
public class RealNameAuthAuditApi extends AbstractApi<RealNameAuthInput, AbstractApiOutput> {
    @Autowired
    protected MemberContractService memberContractService;

    @Autowired
    private CertOperationService certOperationService;

    @Autowired
    private MemberMongoReop memberMongoReop;

    @Override
    protected ApiResult<AbstractApiOutput> handle(RealNameAuthInput input) throws StatusCodeWithException {
        MemberExtJSON memberExtJSON = new MemberExtJSON();
        memberExtJSON.setRealNameAuthStatus(input.getRealNameAuthStatus());
        memberExtJSON.setAuditComment(input.getAuditComment());

        if (input.getRealNameAuthStatus() == 2) {
            Member member = memberMongoReop.findMemberId(input.getId());
            if (member == null) {
                throw new StatusCodeWithException("成员不存在", StatusCode.DATA_NOT_FOUND);
            }
            memberExtJSON = member.getExtJson();
            memberExtJSON.setRealNameAuthStatus(input.getRealNameAuthStatus());
            memberExtJSON.setAuditComment(input.getAuditComment());
            memberExtJSON.setRealNameAuthTime(System.currentTimeMillis());
            // 用户ID
            String memberId = member.getMemberId();
            // 证书请求内容
            String certRequestContent = memberExtJSON.getCertRequestContent();
            // 签发机构的证书ID
            String issuerCertId = input.getIssuerCertId();
            try {
                // 签发证书
                CertVO cert = certOperationService.createUserCert(issuerCertId, memberId, certRequestContent);
                memberExtJSON.setCertStatus(CertStatusEnums.VALID.name());
                // 将证书内容写入
                memberExtJSON.setCertPemContent(cert.getCertContent());
                memberExtJSON.setCertSerialNumber(cert.getSerialNumber());
            } catch (Exception e) {
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }
        }
        else if (input.getRealNameAuthStatus() == 3) { //-1认证失败 /0未认证 /1认证中 /2已认证 /3撤销认证
            memberExtJSON.setUpdatedTime(System.currentTimeMillis());
            memberExtJSON.setCertStatus(CertStatusEnums.INVALID.name());
            // 更新证书状态
            certOperationService.updateStatus(memberExtJSON.getCertSerialNumber(), CertStatusEnums.INVALID.getCode());
        }
        memberContractService.updateExtJson(input.getId(), memberExtJSON);
        return success();
    }

}

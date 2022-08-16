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

import cn.hutool.core.bean.BeanUtil;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.TrustCerts;
import com.welab.wefe.common.data.mongodb.repo.TrustCertsMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.service.TrustCertsContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "trust/certs/update", name = "trust_certs_add")
public class TrustCertsUpdateApi extends AbstractApi<TrustCertsUpdateApi.Input, AbstractApiOutput> {

    @Autowired
    private TrustCertsContractService trustCertsContractService;

    @Autowired
    protected TrustCertsMongoRepo trustCertsMongoRepo;

    @Autowired
    private CertOperationService certService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(TrustCertsUpdateApi.Input input) throws StatusCodeWithException {
        LOG.info("TrustCertsUpdateApi handle..");
        CertVO certVO = certService.queryCertInfoByCertId(input.getCertId());
        if (certVO == null) {
            throw new StatusCodeWithException("证书不存在", StatusCode.DATA_NOT_FOUND);
        }

        if ("add".equalsIgnoreCase(input.getOp())) {
            if (certVO.getCanTrust() != null && certVO.getCanTrust().booleanValue()) {
                throw new StatusCodeWithException("证书已存在信任库中", StatusCode.DATA_EXISTED);
            }
            try {
                boolean isExist = trustCertsContractService.isExistBySerialNumber(certVO.getSerialNumber());
                if (isExist) {
                    throw new StatusCodeWithException("证书已存在信任库中", StatusCode.DATA_EXISTED);
                }
                TrustCerts trustCerts = new TrustCerts();
                BeanUtil.copyProperties(certVO, trustCerts);
                trustCerts.setCertId(certVO.getPkId());
                trustCerts.setIsCaCert(
                        String.valueOf(certVO.getIsCACert() != null && certVO.getIsCACert().booleanValue() ? 1 : 0));
                trustCerts.setIsRootCert(String
                        .valueOf(certVO.getIsRootCert() != null && certVO.getIsRootCert().booleanValue() ? 1 : 0));
                trustCerts.setIssuerCn(certVO.getIssuerCN());
                trustCerts.setSubjectCn(certVO.getSubjectCN());
                trustCerts.setpCertId(certVO.getpCertId());
                trustCertsContractService.add(trustCerts);
                certService.updateCanTrust(certVO.getSerialNumber(), true);
            } catch (StatusCodeWithException e) {
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }
        } else {
            trustCertsContractService.deleteBySerialNumber(certVO.getSerialNumber());
            certService.updateCanTrust(certVO.getSerialNumber(), false);
        }
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String certId;
        @Check(require = true)
        private String op; // add or delete

        public String getCertId() {
            return certId;
        }

        public void setCertId(String certId) {
            this.certId = certId;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

    }
}

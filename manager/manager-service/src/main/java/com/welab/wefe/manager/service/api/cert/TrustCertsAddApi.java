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
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.data.mongodb.entity.union.TrustCerts;
import com.welab.wefe.common.data.mongodb.repo.TrustCertsMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.cert.TrustCertsAddInput;
import com.welab.wefe.manager.service.service.TrustCertsContractService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "trust/certs/add", name = "trust_certs_add")
public class TrustCertsAddApi extends AbstractApi<TrustCertsAddInput, AbstractApiOutput> {

    @Autowired
    private TrustCertsContractService trustCertsContractService;

    @Autowired
    protected TrustCertsMongoRepo trustCertsMongoRepo;

    @Override
    protected ApiResult<AbstractApiOutput> handle(TrustCertsAddInput input) throws StatusCodeWithException {
        LOG.info("TrustCertsAddApi handle..");
        try {
            boolean isExist = trustCertsMongoRepo.existsBySerialNumber(input.getSerialNumber());
            if (isExist) {
                throw new StatusCodeWithException("证书已存在", StatusCode.DATA_EXISTED);
            }
            TrustCerts trustCerts = new TrustCerts();
            BeanUtil.copyProperties(input,trustCerts);
            trustCerts.setIsCaCert(String.valueOf(input.getCaCert() ? 1 : 0));
            trustCerts.setIsRootCert(String.valueOf(input.getRootCert() ? 1 : 0));
            trustCertsContractService.add(trustCerts);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

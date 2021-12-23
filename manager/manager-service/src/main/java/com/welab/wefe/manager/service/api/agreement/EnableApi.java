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

package com.welab.wefe.manager.service.api.agreement;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.agreement.RealnameAuthAgreementTemplateEnableInput;
import com.welab.wefe.manager.service.service.RealnameAuthAgreementTemplateContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "realname/auth/agreement/template/enable", name = "realname_auth_agreement_template_enable")
public class EnableApi extends AbstractApi<RealnameAuthAgreementTemplateEnableInput, AbstractApiOutput> {

    @Autowired
    private RealnameAuthAgreementTemplateContractService contractService;
    @Autowired
    private RealnameAuthAgreementTemplateMongoRepo realnameAuthAgreementTemplateMongoRepo;

    @Override
    protected ApiResult<AbstractApiOutput> handle(RealnameAuthAgreementTemplateEnableInput input) throws StatusCodeWithException {
        LOG.info("RealnameAuthAgreementTemplate enable handle..");
        try {
            RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByEnable(true);
            if (realnameAuthAgreementTemplate != null) {
                contractService.enable(realnameAuthAgreementTemplate.getTemplateFileId(), false);
            }
            contractService.enable(input.getTemplateFileId(), true);

            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(300);
                    String enableStr = realnameAuthAgreementTemplateMongoRepo.findByTemplateFileId(input.getTemplateFileId()).getEnable();
                    if("1".equals(enableStr)) {
                        break;
                    } else {
                        if(i == 2) {
                            return fail("启用失败");
                        }
                    }

                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

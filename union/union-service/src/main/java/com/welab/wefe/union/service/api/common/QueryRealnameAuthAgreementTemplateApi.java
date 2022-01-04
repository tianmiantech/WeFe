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

package com.welab.wefe.union.service.api.common;

import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.common.RealnameAuthAgreementTemplateOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Check service availability
 *
 * @author aaron.li
 **/
@Api(path = "realname/auth/agreement/template/query", name = "available", rsaVerify = true, login = false)
public class QueryRealnameAuthAgreementTemplateApi extends AbstractApi<BaseInput, RealnameAuthAgreementTemplateOutput> {
    @Autowired
    private RealnameAuthAgreementTemplateMongoRepo realnameAuthAgreementTemplateMongoRepo;

    @Override
    protected ApiResult<RealnameAuthAgreementTemplateOutput> handle(BaseInput input) throws StatusCodeWithException, IOException {
        RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByEnable(true);
        RealnameAuthAgreementTemplateOutput realnameAuthAgreementTemplateOutput = new RealnameAuthAgreementTemplateOutput();
        realnameAuthAgreementTemplateOutput.setTemplateFileId(realnameAuthAgreementTemplate.getTemplateFileId());
        realnameAuthAgreementTemplateOutput.setFileName(realnameAuthAgreementTemplate.getFileName());
        return success(realnameAuthAgreementTemplateOutput);
    }

}

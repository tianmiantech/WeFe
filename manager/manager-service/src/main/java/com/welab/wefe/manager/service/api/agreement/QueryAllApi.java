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

package com.welab.wefe.manager.service.api.agreement;

import com.welab.wefe.common.data.mongodb.entity.union.AuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.AuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 *
 * @author yuxin.zhang
 */
@Api(path = "auth/agreement/template/query", name = "auth/agreement/template/query")
public class QueryAllApi extends AbstractApi<BaseInput, JObject> {

    @Autowired
    private AuthAgreementTemplateMongoRepo authAgreementTemplateMongoRepo;

    @Override
    protected ApiResult<JObject> handle(BaseInput input) {
        List<AuthAgreementTemplate> list = authAgreementTemplateMongoRepo.findAll();
        return success(JObject.create("list", JObject.toJSON(list)));
    }

}

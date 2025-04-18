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

import com.welab.wefe.common.data.mongodb.repo.TrustCertsMongoRepo;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseQueryInput;
import com.welab.wefe.manager.service.dto.cert.TrustCertsQueryOutput;
import com.welab.wefe.manager.service.mapper.TrustCertsMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 * @date 2022/08/11
 */
@Api(path = "trust/certs/query", name = "trust_cert_query")
public class QueryTrustCertApi extends AbstractApi<BaseQueryInput, JObject> {
    @Autowired
    protected TrustCertsMongoRepo trustCertsMongoRepo;

    protected TrustCertsMapper mMapper = Mappers.getMapper(TrustCertsMapper.class);


    @Override
    protected ApiResult<JObject> handle(BaseQueryInput input) throws Exception {
        List<TrustCertsQueryOutput> list = trustCertsMongoRepo.findAll(input.getStatus())
                .stream()
                .map(trustCerts -> mMapper.transfer(trustCerts))
                .collect(Collectors.toList());

        return success(JObject.create("list", JObject.toJSON(list)));
    }
}

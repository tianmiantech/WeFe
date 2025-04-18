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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.webank.cert.mgr.model.vo.CertKeyVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.webank.cert.mgr.utils.TransformUtils;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertKeyInfo;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.PageInput;

@Api(path = "cert_key/query", name = "query cert key")
public class QueryKeyApi extends AbstractApi<PageInput, PageOutput<CertKeyVO>> {

    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<PageOutput<CertKeyVO>> handle(PageInput input) throws Exception {
        PageOutput<CertKeyInfo> pageOutput = certOperationService.findKeys(null, input.getPageIndex(),
                input.getPageSize());

        List<CertKeyVO> list = TransformUtils.simpleTransform(pageOutput.getList(), CertKeyVO.class);

        return success(new PageOutput<>(pageOutput.getPageIndex(), pageOutput.getTotal(), pageOutput.getPageSize(),
                pageOutput.getTotalPage(), list));
    }

}

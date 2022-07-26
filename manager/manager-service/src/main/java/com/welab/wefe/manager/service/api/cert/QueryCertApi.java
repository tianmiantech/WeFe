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

import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.webank.cert.mgr.utils.TransformUtils;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.api.cert.QueryCertApi.QueryCertInput;
import com.welab.wefe.manager.service.dto.base.PageInput;

/**
 * 证书查询
 * 
 * @author winter.zou
 *
 */
@Api(path = "cert/query", name = "query cert")
public class QueryCertApi extends AbstractApi<QueryCertInput, PageOutput<CertVO>> {

    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<PageOutput<CertVO>> handle(QueryCertInput input) throws Exception {
        PageOutput<CertInfo> pageOutput = certOperationService.findCertList(input.getUserId(), input.getpCertId(), null,
                null, input.getPageIndex(), input.getPageSize());

        List<CertVO> list = TransformUtils.simpleTransform(pageOutput.getList(), CertVO.class);

        return success(new PageOutput<>(pageOutput.getPageIndex(), pageOutput.getTotal(), pageOutput.getPageSize(),
                pageOutput.getTotalPage(), list));
    }

    public static class QueryCertInput extends PageInput {
        // 用户ID
        private String userId;
        // 签发机构证书ID
        private String pCertId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getpCertId() {
            return pCertId;
        }

        public void setpCertId(String pCertId) {
            this.pCertId = pCertId;
        }
    }

}

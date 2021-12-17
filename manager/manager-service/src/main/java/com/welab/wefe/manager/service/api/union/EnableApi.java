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

package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.union.UnionNodeEnableInput;
import com.welab.wefe.manager.service.service.UnionNodeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "union/node/enable", name = "union_node_enable")
public class EnableApi extends AbstractApi<UnionNodeEnableInput, AbstractApiOutput> {

    @Autowired
    private UnionNodeContractService unionNodeContractService;
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UnionNodeEnableInput input) throws StatusCodeWithException {
        LOG.info("union node enable handle..");
        try {
            UnionNode node = unionNodeMongoRepo.findByNodeId(input.getNodeId());
            if (node == null) {
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "nodeId");
            }

            if (StringUtil.isEmpty(node.getBaseUrl())) {
                throw new StatusCodeWithException("请设置union base url", StatusCode.MISSING_DATA);
            }

            boolean isValid = HttpRequest.create(node.getBaseUrl()).get().success();
            if (!isValid) {
                throw new StatusCodeWithException("无效的union base url", StatusCode.MISSING_DATA);
            }

            unionNodeContractService.enable(input);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

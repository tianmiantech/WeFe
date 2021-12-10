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

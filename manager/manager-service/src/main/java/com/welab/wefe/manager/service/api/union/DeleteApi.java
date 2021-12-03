package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.union.UnionNodeDeleteInput;
import com.welab.wefe.manager.service.service.UnionNodeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "union/node/delete", name = "union_node_delete")
public class DeleteApi extends AbstractApi<UnionNodeDeleteInput, AbstractApiOutput> {

    @Autowired
    private UnionNodeContractService unionNodeContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UnionNodeDeleteInput input) throws StatusCodeWithException {
        LOG.info("DeleteApi handle..");
        try {

            unionNodeContractService.deleteByUnionNodeId(input.getNodeId());
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

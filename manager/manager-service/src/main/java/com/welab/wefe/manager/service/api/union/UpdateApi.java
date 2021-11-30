package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.tag.DataSetDefaultTagUpdateInput;
import com.welab.wefe.manager.service.dto.union.UnionNodeUpdateInput;
import com.welab.wefe.manager.service.service.UnionNodeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "union/node/update", name = "union_node_update", login = false)
public class UpdateApi extends AbstractApi<UnionNodeUpdateInput, AbstractApiOutput> {

    @Autowired
    private UnionNodeContractService unionNodeContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UnionNodeUpdateInput input) throws StatusCodeWithException {
        LOG.info("UpdateApi handle..");
        try {
            boolean isValid = HttpRequest.create(input.getUnionBaseUrl()).get().success();
            if (!isValid) {
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "unionBaseUrl");
            }
            unionNodeContractService.update(input);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

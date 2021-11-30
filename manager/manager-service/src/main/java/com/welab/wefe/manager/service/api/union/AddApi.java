package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.union.UnionNodeAddInput;
import com.welab.wefe.manager.service.mapper.UnionNodeMapper;
import com.welab.wefe.manager.service.service.UnionNodeContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "union/node/add", name = "union_node_add", login = false)
public class AddApi extends AbstractApi<UnionNodeAddInput, AbstractApiOutput> {

    @Autowired
    private UnionNodeContractService unionNodeContractService;
    protected UnionNodeMapper unionNodeMapper = Mappers.getMapper(UnionNodeMapper.class);

    @Override
    protected ApiResult<AbstractApiOutput> handle(UnionNodeAddInput input) throws StatusCodeWithException {
        LOG.info("AddApi handle..");
        try {
            boolean isValid = HttpRequest.create(input.getUnionBaseUrl()).get().success();
            if (!isValid) {
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "unionBaseUrl");
            }
            unionNodeContractService.add(unionNodeMapper.transferAddInput(input));
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

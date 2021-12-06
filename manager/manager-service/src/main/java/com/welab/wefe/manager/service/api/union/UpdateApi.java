package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.util.StringUtil;
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
@Api(path = "union/node/update", name = "union_node_update")
public class UpdateApi extends AbstractApi<UnionNodeUpdateInput, AbstractApiOutput> {

    @Autowired
    private UnionNodeContractService unionNodeContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UnionNodeUpdateInput input) throws StatusCodeWithException {
        LOG.info("UpdateApi handle..");
        try {

            if (StringUtil.isEmpty(input.getBaseUrl())) {
                throw new StatusCodeWithException("请设置union base url", StatusCode.MISSING_DATA);
            }

            boolean isValid = HttpRequest.create(input.getBaseUrl()).get().success();
            if (!isValid) {
                throw new StatusCodeWithException("无效的union base url", StatusCode.MISSING_DATA);
            }


            unionNodeContractService.update(input);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

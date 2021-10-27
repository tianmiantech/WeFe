package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberOutput;
import com.welab.wefe.manager.service.dto.tag.DataSetDefaultTagUpdateInput;
import com.welab.wefe.manager.service.service.DatSetDefaultTagContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "union/node/update", name = "union_node_update", login = false)
public class UpdateApi extends AbstractApi<DataSetDefaultTagUpdateInput, MemberOutput> {

    @Autowired
    private DatSetDefaultTagContractService datSetDefaultTagContractService;

    @Override
    protected ApiResult<MemberOutput> handle(DataSetDefaultTagUpdateInput input) throws StatusCodeWithException {
        LOG.info("UpdateApi handle..");
        try {
            datSetDefaultTagContractService.updateByTagId(input);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

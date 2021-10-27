package com.welab.wefe.manager.service.api.union;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberOutput;
import com.welab.wefe.manager.service.dto.tag.DatSetDefaultTagDeleteInput;
import com.welab.wefe.manager.service.service.DatSetDefaultTagContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "union/node/delete", name = "union_node_delete", login = false)
public class DeleteApi extends AbstractApi<DatSetDefaultTagDeleteInput, MemberOutput> {

    @Autowired
    private DatSetDefaultTagContractService datSetDefaultTagContractService;

    @Override
    protected ApiResult<MemberOutput> handle(DatSetDefaultTagDeleteInput input) throws StatusCodeWithException {
        LOG.info("DeleteApi handle..");
        try {

            datSetDefaultTagContractService.deleteByTagId(input.getTagId());
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

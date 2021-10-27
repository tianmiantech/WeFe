package com.welab.wefe.manager.service.api.member.authtype;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberAuthTypeDeleteInput;
import com.welab.wefe.manager.service.service.MemberAuthTypeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "member/authtype/delete", name = "member_authtype_delete", login = false)
public class DeleteApi extends AbstractApi<MemberAuthTypeDeleteInput, AbstractApiOutput> {

    @Autowired
    private MemberAuthTypeContractService memberAuthTypeContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(MemberAuthTypeDeleteInput input) throws StatusCodeWithException {
        LOG.info("DeleteApi handle..");
        try {

            memberAuthTypeContractService.deleteByTypeId(input.getTypeId());
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

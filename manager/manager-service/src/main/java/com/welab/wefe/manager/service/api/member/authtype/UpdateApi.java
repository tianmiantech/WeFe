package com.welab.wefe.manager.service.api.member.authtype;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.member.MemberAuthTypeUpdateInput;
import com.welab.wefe.manager.service.dto.member.MemberOutput;
import com.welab.wefe.manager.service.dto.tag.DataSetDefaultTagUpdateInput;
import com.welab.wefe.manager.service.service.MemberAuthTypeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "member/authtype/update", name = "member_authtype_update", login = false)
public class UpdateApi extends AbstractApi<MemberAuthTypeUpdateInput, MemberOutput> {

    @Autowired
    private MemberAuthTypeContractService memberAuthTypeContractService;

    @Override
    protected ApiResult<MemberOutput> handle(MemberAuthTypeUpdateInput input) throws StatusCodeWithException {
        LOG.info("UpdateApi handle..");
        try {
            memberAuthTypeContractService.updateByTypeId(input);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

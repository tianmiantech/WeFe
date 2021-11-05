package com.welab.wefe.manager.service.api.authtype;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.MemberAuthType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.authtype.MemberAuthTypeAddInput;
import com.welab.wefe.manager.service.service.MemberAuthTypeContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/authtype/add", name = "member_authtype_add")
public class AddApi extends AbstractApi<MemberAuthTypeAddInput, AbstractApiOutput> {

    @Autowired
    private MemberAuthTypeContractService memberAuthTypeContractService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(MemberAuthTypeAddInput input) throws StatusCodeWithException {
        LOG.info("AddApi handle..");
        try {
            MemberAuthType memberAuthType = new MemberAuthType();
            memberAuthType.setTypeName(input.getTypeName());
            memberAuthType.setExtJson(input.getExtJson());
            memberAuthTypeContractService.add(memberAuthType);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}

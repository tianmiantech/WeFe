package com.welab.wefe.serving.service.api.partner;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.PartnerService;

@Api(path = "partner/init", name = "init partner", login = false)
public class InitPartnerApi extends AbstractNoneInputApi<InitPartnerApi.Output> {

    @Autowired
    private PartnerService partnerService;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        partnerService.init();
        return success();
    }

    public static class Output {

    }

}

package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.account.RegisterInput;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
public class RegisterApi extends AbstractApi<RegisterInput, AbstractApiOutput> {
    @Override
    protected ApiResult<AbstractApiOutput> handle(RegisterInput input) throws StatusCodeWithException, IOException {
        return null;
    }
}

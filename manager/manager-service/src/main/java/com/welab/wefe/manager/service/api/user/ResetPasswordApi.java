package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.user.ChangePasswordInput;
import com.welab.wefe.manager.service.dto.user.ResetPasswordInput;
import com.welab.wefe.manager.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "user/reset/password", name = "password reset")
public class ResetPasswordApi extends AbstractApi<ResetPasswordInput, AbstractApiOutput> {
    @Autowired
    private UserService userService;


    @Override
    protected ApiResult<AbstractApiOutput> handle(ResetPasswordInput input) throws StatusCodeWithException, IOException {
        userService.resetPassword(input.getUserId());
        return success();
    }
}

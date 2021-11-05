package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.user.ChangePasswordInput;
import com.welab.wefe.manager.service.dto.user.RegisterInput;
import com.welab.wefe.manager.service.mapper.UserMapper;
import com.welab.wefe.manager.service.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "user/change/password", name = "password change")
public class ChangePasswordApi extends AbstractApi<ChangePasswordInput, AbstractApiOutput> {
    @Autowired
    private UserService userService;


    @Override
    protected ApiResult<AbstractApiOutput> handle(ChangePasswordInput input) throws StatusCodeWithException, IOException {
        userService.changePassword(input.getUserId(),input.getPassword());
        return success();
    }
}

package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.user.UserUpdateInput;
import com.welab.wefe.manager.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "user/udpate", name = "user update")
public class UpdateApi extends AbstractApi<UserUpdateInput, AbstractApiOutput> {
    @Autowired
    private UserService userService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UserUpdateInput input) throws StatusCodeWithException, IOException {
        userService.update(input);
        return success();
    }
}

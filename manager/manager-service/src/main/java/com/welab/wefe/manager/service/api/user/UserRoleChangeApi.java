package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.user.RegisterInput;
import com.welab.wefe.manager.service.dto.user.UserRoleChangeInput;
import com.welab.wefe.manager.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/5
 */
@Api(path = "user/role/change", name = "user role change", login = false)
public class UserRoleChangeApi extends AbstractApi<UserRoleChangeInput, AbstractApiOutput> {
    @Autowired
    private UserService userService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UserRoleChangeInput input) throws StatusCodeWithException, IOException {
        userService.changeUserRole(input.getUserId(),input.isAdminRole());
        return success();
    }
}

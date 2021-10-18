package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.account.LoginInput;
import com.welab.wefe.manager.service.dto.account.LoginOutput;
import com.welab.wefe.manager.service.entity.User;
import com.welab.wefe.manager.service.mapper.UserMapper;
import com.welab.wefe.manager.service.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author Jervis
 * @Date 2020-06-04
 **/
@Api(path = "user/login", name = "登录", login = false)
public class LoginApi extends AbstractApi<LoginInput, LoginOutput> {

    @Autowired
    private UserService mUserService;
    private UserMapper mUserMapper = Mappers.getMapper(UserMapper.class);

    @Override
    protected ApiResult<LoginOutput> handle(LoginInput input) throws StatusCodeWithException {


        User user = mUserService.find(input.getAccount(), input.getPassword());
        if (user == null) {
            return fail("账号不存在, 请注册");
        }


        LoginOutput output = mUserMapper.transfer(user);
        String token = CurrentAccount.generateToken();
        output.setToken(token);

        CurrentAccount.logined(token, user.getId(), user.getAccount());

        return success(output);
    }
}

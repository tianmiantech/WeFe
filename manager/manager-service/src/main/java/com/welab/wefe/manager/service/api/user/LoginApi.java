/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.manager.service.api.user;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.user.LoginInput;
import com.welab.wefe.manager.service.dto.user.LoginOutput;
import com.welab.wefe.manager.service.mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/2
 */
@Api(path = "user/login", name = "登录", login = false)
public class LoginApi extends AbstractApi<LoginInput, LoginOutput> {

    @Autowired
    private UserMongoRepo userMongoRepo;
    private UserMapper mUserMapper = Mappers.getMapper(UserMapper.class);

    @Override
    protected ApiResult<LoginOutput> handle(LoginInput input) throws StatusCodeWithException {


        User user = userMongoRepo.findByAccount(input.getAccount());
        if (user == null) {
            return fail("账号不存在, 请注册");
        }

        if (!user.getPassword().equals(Md5.of(input.getPassword() + user.getSalt()))) {
            return fail("密码错误, 请重新输入");
        }


        LoginOutput output = mUserMapper.transfer(user);
        String token = CurrentAccount.generateToken();
        output.setToken(token);
        CurrentAccount.logined(token, user.getUserId(), user.getAccount(), user.isAdminRole(), user.isSuperAdminRole());
        return success(output);
    }
}

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

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.user.QueryUserInput;
import com.welab.wefe.manager.service.dto.user.QueryUserOutput;
import com.welab.wefe.manager.service.mapper.UserMapper;
import com.welab.wefe.manager.service.service.UserService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/5
 */
@Api(path = "user/query", name = "query")
public class QueryApi extends AbstractApi<QueryUserInput, PageOutput<QueryUserOutput>> {
    @Autowired
    private UserService userService;
    private UserMapper mUserMapper = Mappers.getMapper(UserMapper.class);

    @Override
    protected ApiResult<PageOutput<QueryUserOutput>> handle(QueryUserInput input) throws StatusCodeWithException, IOException {
        PageOutput<User> pageOutput = userService.findList(input);
        List<QueryUserOutput> list = pageOutput.getList().stream()
                .map(mUserMapper::transferUserToQueryUserOutput)
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
    }
}

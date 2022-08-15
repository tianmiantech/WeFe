/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.api.account;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.AccountListAllOutputModel;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Zane
 */
@Api(path = "account/list_all", name = "获取全量的用户列表")
public class ListAllApi extends AbstractApi<ListAllApi.Input, ListAllApi.Output> {

    @Autowired
    AccountService service;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        List<AccountListAllOutputModel> list = service.listAll(input);
        return success(new Output(list));
    }

    public static class Input extends PagingInput {
        private String nickname;

        //region getter/setter

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        //endregion
    }

    public static class Output {
        public List<AccountListAllOutputModel> list;

        public Output() {
        }

        public Output(List<AccountListAllOutputModel> list) {
            this.list = list;
        }
    }
}

/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.api.message;

/**
 * @author Zane
 */

import com.welab.wefe.board.service.database.entity.MessageMysqlModel;
import com.welab.wefe.board.service.database.repository.MessageRepository;
import com.welab.wefe.board.service.dto.entity.MessageOutputModel;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 */
@Api(path = "message/detail", name = "get a message detail")
public class DetailApi extends AbstractApi<DetailApi.Input, MessageOutputModel> {

    @Autowired
    MessageRepository repo;

    @Override
    protected ApiResult<MessageOutputModel> handle(DetailApi.Input input) throws StatusCodeWithException {
        MessageMysqlModel model = repo.findById(input.getId()).orElse(null);
        MessageOutputModel output = ModelMapper.map(model, MessageOutputModel.class);
        return success(output);
    }

    public static class Input extends AbstractApiInput {
        private String id;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        //endregion
    }
}

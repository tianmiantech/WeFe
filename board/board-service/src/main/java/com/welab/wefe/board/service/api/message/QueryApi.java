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

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.MessageOutputModel;
import com.welab.wefe.board.service.service.MessageService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "message/query", name = "query messages by pagination")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<MessageOutputModel>> {

    @Autowired
    MessageService service;

    @Override
    protected ApiResult<PagingOutput<MessageOutputModel>> handle(QueryApi.Input input) throws StatusCodeWithException {
        return success(service.query(input));
    }

    public static class Input extends PagingInput {
        @Check(name = "消息级别")
        private String level;

        @Check(name = "是否未读")
        private Boolean unread;

        //region getter/setter

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Boolean getUnread() {
            return unread;
        }

        public void setUnread(Boolean unread) {
            this.unread = unread;
        }


        //endregion
    }
}

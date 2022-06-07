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

package com.welab.wefe.board.service.service;


import com.welab.wefe.board.service.api.message.QueryApi;
import com.welab.wefe.board.service.database.entity.MessageMysqlModel;
import com.welab.wefe.board.service.database.repository.MessageRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.MessageOutputModel;
import com.welab.wefe.board.service.dto.vo.message.AbstractMessageContent;
import com.welab.wefe.board.service.dto.vo.message.TextMessageContent;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.wefe.enums.MessageEvent;
import com.welab.wefe.common.wefe.enums.ProducerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * @author Zane
 */
@Service
public class MessageService extends AbstractService {

    @Autowired
    MessageRepository repo;

    public PagingOutput<MessageOutputModel> query(QueryApi.Input input) {

        Specification<MessageMysqlModel> where = Where
                .create()
                .equal("todo", input.todo)
                .equal("todoComplete", input.todoComplete)
                .equal("level", input.level)
                .equal("unread", input.unread)
                .in("event", input.eventList)
                .build(MessageMysqlModel.class);

        return repo.paging(where, input, MessageOutputModel.class);
    }

    public void read(String id) {
        repo.updateById(id, "unread", false, MessageMysqlModel.class);
    }

    public void add(MessageEvent event, String title, String content) {
        TextMessageContent messageContent = new TextMessageContent();
        messageContent.message = content;
        add(event, title, content);
    }

    public void add(MessageEvent event, AbstractMessageContent content) {
        add(event, null, content);
    }

    public void add(MessageEvent event, String title, AbstractMessageContent content) {
        MessageMysqlModel model = new MessageMysqlModel();
        model.setEvent(event);
        model.setProducer(ProducerType.board);
        model.setLevel(event.getLevel());
        model.setTitle(title);
        model.setContent(content.toString());
        model.setUnread(true);

        repo.save(model);
    }

}

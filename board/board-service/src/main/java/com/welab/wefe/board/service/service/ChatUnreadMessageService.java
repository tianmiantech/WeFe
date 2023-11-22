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

import com.welab.wefe.board.service.database.entity.chat.ChatUnreadMessageMySqlModel;
import com.welab.wefe.board.service.database.entity.chat.MemberChatMySqlModel;
import com.welab.wefe.board.service.database.repository.ChatUnreadMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Unread message service
 *
 * @author aaron.li
 **/
@Service
public class ChatUnreadMessageService {

    @Autowired
    private ChatUnreadMessageRepository chatUnreadMessageRepository;


    /**
     * Unread message plus 1
     */
    public void addChatUnreadMessage(MemberChatMySqlModel memberChatModel) {
        ChatUnreadMessageMySqlModel unreadMessageModel = new ChatUnreadMessageMySqlModel();
        unreadMessageModel.setFromAccountId(memberChatModel.getFromAccountId());
        unreadMessageModel.setFromMemberId(memberChatModel.getFromMemberId());
        unreadMessageModel.setToAccountId(memberChatModel.getToAccountId());
        unreadMessageModel.setToMemberId(memberChatModel.getToMemberId());
        unreadMessageModel.setCreatedTime(new Date());
        unreadMessageModel.setUpdatedTime(new Date());

        ChatUnreadMessageMySqlModel model = chatUnreadMessageRepository.findByFromAccountIdAndAndToAccountId(memberChatModel.getFromAccountId(), memberChatModel.getToAccountId());
        if (null == model) {
            unreadMessageModel.setNum(1);
            chatUnreadMessageRepository.save(unreadMessageModel);
        } else {
            model.setNum(model.getNum() + 1);
            chatUnreadMessageRepository.save(model);
        }
    }

    /**
     * Delete unread message record
     *
     * @param fromAccountId Sender account ID
     * @param toAccountId   Receiver account ID
     * @return Number of records deleted
     */
    public int delete(String fromAccountId, String toAccountId) {
        return chatUnreadMessageRepository.deleteByFromAccountIdAndToAccountId(fromAccountId, toAccountId);
    }

}

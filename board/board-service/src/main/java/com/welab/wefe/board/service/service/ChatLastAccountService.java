/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.chat.AddChatLastAccountApi;
import com.welab.wefe.board.service.api.chat.DeleteChatLastAccountApi;
import com.welab.wefe.board.service.database.entity.chat.ChatLastAccountMysqlModel;
import com.welab.wefe.board.service.database.entity.chat.ChatUnreadMessageMySqlModel;
import com.welab.wefe.board.service.database.repository.ChatLastAccountRepository;
import com.welab.wefe.board.service.database.repository.ChatUnreadMessageRepository;
import com.welab.wefe.board.service.dto.entity.ChatLastAccountOutputModel;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Recent chat account service
 *
 * @author aaron.li
 **/
@Service
public class ChatLastAccountService extends AbstractService {

    @Autowired
    private ChatLastAccountRepository chatLastAccountRepository;

    @Autowired
    private ChatUnreadMessageRepository chatUnreadMessageRepository;

    /**
     * Query the list of all recent chat accounts
     *
     * @param accountId Primary account ID
     */
    public List<ChatLastAccountOutputModel> query(String accountId) {
        // Result list
        List<ChatLastAccountOutputModel> resultList = new ArrayList<>();

        // Query recent chat account
        Specification<ChatLastAccountMysqlModel> where = Where
                .create()
                .equal("accountId", accountId)
                .orderBy("updatedTime", OrderBy.desc)
                .build(ChatLastAccountMysqlModel.class);
        List<ChatLastAccountMysqlModel> chatLastAccountMysqlModelList = chatLastAccountRepository.findAll(where);
        if (CollectionUtils.isEmpty(chatLastAccountMysqlModelList)) {
            return resultList;
        }

        List<Object> liaisonAccountIdList = new ArrayList<>();
        ChatLastAccountOutputModel chatLastAccountOutputModel = null;
        for (ChatLastAccountMysqlModel model : chatLastAccountMysqlModelList) {
            liaisonAccountIdList.add(model.getLiaisonAccountId());
            chatLastAccountOutputModel = new ChatLastAccountOutputModel();
            BeanUtils.copyProperties(model, chatLastAccountOutputModel);
            resultList.add(chatLastAccountOutputModel);
        }


        // Query the number of unread messages of this account
        Specification<ChatUnreadMessageMySqlModel> where2 = Where.create()
                .in("fromAccountId", liaisonAccountIdList)
                .equal("toAccountId", accountId)
                .build(ChatUnreadMessageMySqlModel.class);
        List<ChatUnreadMessageMySqlModel> chatUnreadMessageMySqlModelList = chatUnreadMessageRepository.findAll(where2);
        if (CollectionUtils.isEmpty(chatUnreadMessageMySqlModelList)) {
            return resultList;
        }

        // Set the number of unread messages
        for (ChatUnreadMessageMySqlModel model : chatUnreadMessageMySqlModelList) {
            for (ChatLastAccountOutputModel resultModel : resultList) {
                // Found unread messages from this contact
                if (resultModel.getLiaisonAccountId().equals(model.getFromAccountId())) {
                    resultModel.setUnreadNum(model.getNum());
                    break;
                }
            }
        }

        return resultList;
    }

    /**
     * Add a recent chat account record
     */
    public void add(ChatLastAccountMysqlModel model) {
        if (null == model) {
            return;
        }

        Specification<ChatLastAccountMysqlModel> where = Where
                .create()
                .equal("accountId", model.getAccountId())
                .equal("liaisonAccountId", model.getLiaisonAccountId())
                .build(ChatLastAccountMysqlModel.class);
        ChatLastAccountMysqlModel chatLastAccountMysqlModel = chatLastAccountRepository.findOne(where).orElse(null);
        chatLastAccountMysqlModel = (null == chatLastAccountMysqlModel ? model : chatLastAccountMysqlModel);
        chatLastAccountMysqlModel.setUpdatedTime(new Date());
        chatLastAccountMysqlModel.setAccountName(model.getAccountName());
        chatLastAccountMysqlModel.setMemberName(model.getMemberName());
        chatLastAccountMysqlModel.setLiaisonAccountName(model.getLiaisonAccountName());
        chatLastAccountMysqlModel.setLiaisonMemberName(model.getLiaisonMemberName());
        chatLastAccountRepository.save(chatLastAccountMysqlModel);
    }

    /**
     * Add a recent chat account record
     */
    public void add(AddChatLastAccountApi.Input input) {
        ChatLastAccountMysqlModel model = new ChatLastAccountMysqlModel();
        model.setAccountId(input.getAccountId());
        model.setAccountName(input.getAccountName());
        model.setMemberId(input.getMemberId());
        model.setMemberName(input.getMemberName());
        model.setLiaisonAccountId(input.getLiaisonAccountId());
        model.setLiaisonAccountName(input.getLiaisonAccountName());
        model.setLiaisonMemberId(input.getLiaisonMemberId());
        model.setLiaisonMemberName(input.getLiaisonMemberName());

        add(model);
    }

    /**
     * Delete records according to account ID and contact account ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteChatLastAccountApi.Input input) {
        chatLastAccountRepository.deleteByAccountIdEqualsAndLiaisonAccountIdEquals(input.getAccountId(), input.getLiaisonAccountId());
    }
}

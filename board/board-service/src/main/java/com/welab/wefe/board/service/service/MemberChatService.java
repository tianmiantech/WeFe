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

import com.welab.wefe.board.service.api.chat.QueryChatDetailApi;
import com.welab.wefe.board.service.api.chat.UpdateToReadApi;
import com.welab.wefe.board.service.constant.ChatConstant;
import com.welab.wefe.board.service.database.entity.chat.ChatLastAccountMysqlModel;
import com.welab.wefe.board.service.database.entity.chat.MemberChatMySqlModel;
import com.welab.wefe.board.service.database.entity.chat.MessageQueueMySqlModel;
import com.welab.wefe.board.service.database.repository.ChatUnreadMessageRepository;
import com.welab.wefe.board.service.database.repository.MemberChatRepository;
import com.welab.wefe.board.service.database.repository.MessageQueueRepository;
import com.welab.wefe.board.service.database.repository.MessageRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.MemberChatOutputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.common.wefe.enums.ProducerType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Member chat service
 *
 * @author Johnny.lin
 */
@Service
public class MemberChatService extends AbstractService {

    @Autowired
    MemberChatRepository memberChatRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ChatUnreadMessageRepository chatUnreadMessageRepository;

    @Autowired
    MessageQueueRepository messageQueueRepository;

    @Autowired
    private ChatLastAccountService chatLastAccountService;

    @Autowired
    private ChatUnreadMessageService chatUnreadMessageService;


    /**
     * Send the message to the target gateway through its own gateway
     *
     * @param fromAccountId   Sender account id
     * @param fromAccountName Sender account name
     * @param toMemberId      Receiver member id
     * @param toAccountId     Receiver account id
     * @param content         message content
     */
    @Transactional(rollbackFor = Exception.class)
    public JObject sendMessage(String fromAccountId, String fromAccountName, String toMemberId, String toAccountId, String toMemberName, String toAccountName, String content) throws StatusCodeWithException {
        JObject ret = JObject.create().append(ChatConstant.KEY_CODE, StatusCode.SUCCESS.getCode());


        String fromMemberId = CacheObjects.getMemberId();

        // Add a recent chat account record
        ChatLastAccountMysqlModel model = new ChatLastAccountMysqlModel();
        model.setAccountId(fromAccountId);
        model.setMemberId(CacheObjects.getMemberId());
        model.setAccountName(fromAccountName);
        model.setMemberName(CacheObjects.getMemberName());
        model.setLiaisonAccountId(toAccountId);
        model.setLiaisonMemberId(toMemberId);
        model.setLiaisonAccountName(toAccountName);
        model.setLiaisonMemberName(toMemberName);
        model.setUpdatedTime(new Date());
        chatLastAccountService.add(model);


        // wrap message
        String messageId = generateMessageId();
        String data = JObject.create(ChatConstant.KEY_FROM_MEMBER_ID, fromMemberId)
                .append(ChatConstant.KEY_FROM_MEMBER_NAME, CacheObjects.getMemberName())
                .append(ChatConstant.KEY_FROM_ACCOUNT_ID, fromAccountId)
                .append(ChatConstant.KEY_FROM_ACCOUNT_NAME, fromAccountName)
                .append(ChatConstant.KEY_TO_MEMBER_ID, toMemberId)
                .append(ChatConstant.KEY_TO_ACCOUNT_ID, toAccountId)
                .append(ChatConstant.KEY_TO_MEMBER_NAME, toMemberName)
                .append(ChatConstant.KEY_TO_ACCOUNT_NAME, toAccountName)
                .append(ChatConstant.KEY_MESSAGE_ID, messageId)
                .append(ChatConstant.KEY_CONTENT, content)
                .append(ChatConstant.KEY_CREATED_TIME, System.currentTimeMillis())
                .toString();


        // Push the message to the destination member through the gateway
        ApiResult<?> result = gatewayService.sendToOtherGateway(toMemberId, GatewayActionType.create_chat_msg, data, GatewayProcessorType.dbChatTableProcessor);

        Date createdTime = new Date();
        // Message detail object
        MemberChatMySqlModel memberChatModel = new MemberChatMySqlModel();
        memberChatModel.setFromAccountId(fromAccountId);
        memberChatModel.setFromAccountName(fromAccountName);
        memberChatModel.setFromMemberId(fromMemberId);
        memberChatModel.setFromMemberName(CacheObjects.getMemberName());
        memberChatModel.setToMemberId(toMemberId);
        memberChatModel.setToMemberName(toMemberName);
        memberChatModel.setToAccountId(toAccountId);
        memberChatModel.setToAccountName(toAccountName);
        memberChatModel.setContent(content);
        memberChatModel.setStatus(ChatConstant.MESSAGE_STATUS_SEND_SUCCESS);
        memberChatModel.setDirection(ChatConstant.MESSAGE_DIRECTION_SEND);
        memberChatModel.setCreatedTime(createdTime);
        memberChatModel.setUpdatedTime(createdTime);
        memberChatModel.setMessageId(messageId);

        // Message sending failed
        if (!result.success()) {
            memberChatModel.setStatus(ChatConstant.MESSAGE_STATUS_SEND_FAIL);
            ret.append(ChatConstant.KEY_CODE, StatusCode.SYSTEM_ERROR.getCode())
                    .append(ChatConstant.KEY_MESSAGE, result.getMessage());
        }
        ret.append(ChatConstant.KEY_MEMBER_CHAT_ID, memberChatModel.getId());

        // Save message details
        memberChatRepository.save(memberChatModel);

        return ret;
    }


    /**
     * send message
     */
    @Transactional(rollbackFor = Exception.class)
    public JObject sendMessage(String toMemberId, String toMemberName, String toAccountId, String toAccountName,
                               String content) throws StatusCodeWithException {

        CurrentAccount.Info info = CurrentAccount.get();
        if (null == info) {
            throw new StatusCodeWithException("请登录后访问", StatusCode.LOGIN_REQUIRED);
        }
        String fromAccountId = info.id;
        String fromAccountName = CacheObjects.getAccountMap().get(fromAccountId);
        return sendMessage(fromAccountId, fromAccountName, toMemberId, toAccountId, toMemberName, toAccountName, content);
    }

    /**
     * Failed message resend
     *
     * @param memberChatId Message primary key ID of back-end database
     */
    public void resendMessage(String memberChatId) throws StatusCodeWithException {
        MemberChatMySqlModel model = memberChatRepository.findById(memberChatId).orElse(null);
        if (null == model) {
            throw new StatusCodeWithException("该消息无效", StatusCode.DATA_NOT_FOUND);
        }
        if (null == model.getStatus() || ChatConstant.MESSAGE_STATUS_SEND_FAIL != model.getStatus()) {
            throw new StatusCodeWithException("该消息为非发送失败状态，禁止重发", StatusCode.ILLEGAL_REQUEST);
        }
        // splicing messages
        String data = JObject.create(ChatConstant.KEY_FROM_MEMBER_ID, model.getFromMemberId())
                .append(ChatConstant.KEY_FROM_MEMBER_NAME, model.getFromMemberName())
                .append(ChatConstant.KEY_FROM_ACCOUNT_ID, model.getFromAccountId())
                .append(ChatConstant.KEY_FROM_ACCOUNT_NAME, model.getFromAccountName())
                .append(ChatConstant.KEY_TO_MEMBER_ID, model.getToMemberId())
                .append(ChatConstant.KEY_TO_ACCOUNT_ID, model.getToAccountId())
                .append(ChatConstant.KEY_TO_MEMBER_NAME, model.getToMemberName())
                .append(ChatConstant.KEY_TO_ACCOUNT_NAME, model.getToAccountName())
                .append(ChatConstant.KEY_MESSAGE_ID, model.getMessageId())
                .append(ChatConstant.KEY_CONTENT, model.getContent())
                .append(ChatConstant.KEY_CREATED_TIME, System.currentTimeMillis())
                .toString();

        // Push the message to the destination member through the gateway
        ApiResult<?> result = gatewayService.sendToOtherGateway(model.getToMemberId(), GatewayActionType.create_chat_msg, data, GatewayProcessorType.dbChatTableProcessor);
        // Message sending failed
        if (!result.success()) {
            throw new StatusCodeWithException(result.getMessage(), StatusCode.RPC_ERROR);
        }
        // Update message status is successful
        memberChatRepository.updateById(model.getId(), "status", ChatConstant.MESSAGE_STATUS_SEND_SUCCESS, MemberChatMySqlModel.class, false);
    }

    /**
     * Query chat details
     *
     * <p>
     * Note: the page turning of this interface is special: the value of the created_time field passed in from the front end will change,
     * so only the first page will be returned
     * </p>
     */
    public PagingOutput<MemberChatOutputModel> queryChatDetail(QueryChatDetailApi.Input input) {
        String fromAccountId = input.getFromAccountId();
        String toAccountId = input.getToAccountId();
        Specification<MemberChatMySqlModel> where = (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            Predicate predicate1 = cb.equal(root.get(ChatConstant.KEY_FROM_ACCOUNT_ID), fromAccountId);
            Predicate predicate2 = cb.equal(root.get(ChatConstant.KEY_TO_ACCOUNT_ID), toAccountId);
            Predicate predicate3 = cb.equal(root.get(ChatConstant.KEY_DIRECTION), 1);
            list.add(predicate1);
            list.add(predicate2);
            list.add(predicate3);
            Predicate predicateAnd1 = cb.and(list.toArray(new Predicate[list.size()]));

            List<Predicate> list2 = new ArrayList<>();
            Predicate predicate11 = cb.equal(root.get(ChatConstant.KEY_FROM_ACCOUNT_ID), toAccountId);
            Predicate predicate22 = cb.equal(root.get(ChatConstant.KEY_TO_ACCOUNT_ID), fromAccountId);
            Predicate predicate33 = cb.equal(root.get(ChatConstant.KEY_DIRECTION), 0);
            list2.add(predicate11);
            list2.add(predicate22);
            list2.add(predicate33);
            Predicate predicateAnd2 = cb.and(list2.toArray(new Predicate[list2.size()]));

            Predicate predicateOr = cb.or(predicateAnd1, predicateAnd2);

            Long limitCreateTime = input.getLimitCreateTime();
            if (null != limitCreateTime && limitCreateTime > 0) {
                Predicate predicate00 = cb.lessThan(root.get("createdTime"), new Date(limitCreateTime));
                predicateOr = cb.and(predicate00, predicateOr);
            }
            return predicateOr;
        };

        // Return to page 1 only
        Pageable pageable = PageRequest
                .of(
                        0,
                        input.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdTime")
                );

        Page<MemberChatMySqlModel> page = memberChatRepository.findAll(where, pageable);

        LOG.info("result: " + JObject.toJSONString(page));
        LOG.info("page.getContent(): " + JObject.toJSONString(page.getContent()));

        List<MemberChatMySqlModel> models = page.getContent()
                .stream()
                .map(x -> ModelMapper.map(x, MemberChatMySqlModel.class))
                .collect(Collectors.toList());

        // Wrap the returned chat content
        List<MemberChatOutputModel> outputsList = new ArrayList<>();
        MemberChatOutputModel outputModel = null;
        for (MemberChatMySqlModel memberChat : models) {
            outputModel = new MemberChatOutputModel();
            outputModel.setId(memberChat.getId());
            outputModel.setFromAccountId(memberChat.getFromAccountId());
            outputModel.setFromMemberId(memberChat.getFromMemberId());
            outputModel.setToAccountId(memberChat.getToAccountId());
            outputModel.setToMemberId(memberChat.getToMemberId());
            outputModel.setContent(memberChat.getContent());
            outputModel.setStatus(memberChat.getStatus());
            outputModel.setCreatedTime(memberChat.getCreatedTime());

            outputsList.add(outputModel);
        }

        return PagingOutput.of(
                page.getTotalElements(),
                outputsList,
                MemberChatOutputModel.class
        );
    }

    /**
     * Get an unhandled message
     */
    public MessageQueueMySqlModel getOneMessage() {
        Specification<MessageQueueMySqlModel> queryCondtion = Where
                .create()
                .equal("producer", ProducerType.gateway)
                .equal("action", GatewayActionType.create_chat_msg)
                .orderBy("createdTime", OrderBy.asc)
                .build(MessageQueueMySqlModel.class);

        Page<MessageQueueMySqlModel> list = messageQueueRepository.findAll(queryCondtion, PageRequest.of(0, 1));
        if (list == null || CollectionUtils.isEmpty(list.getContent())) {
            return null;
        }

        return list.getContent().get(0);
    }

    /**
     * Process received messages
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleChatMessage(MessageQueueMySqlModel message) {
        if (!StringUtil.isEmpty(message.getParams())) {
            JObject jObject = JObject.create(message.getParams());
            String fromMemberId = jObject.getString(ChatConstant.KEY_FROM_MEMBER_ID);
            String fromMemberName = jObject.getString(ChatConstant.KEY_FROM_MEMBER_NAME);
            String fromAccountId = jObject.getString(ChatConstant.KEY_FROM_ACCOUNT_ID);
            String fromAccountName = jObject.getString(ChatConstant.KEY_FROM_ACCOUNT_NAME);
            String toMemberId = jObject.getString(ChatConstant.KEY_TO_MEMBER_ID);
            String toMemberName = jObject.getString(ChatConstant.KEY_TO_MEMBER_NAME);
            String toAccountId = jObject.getString(ChatConstant.KEY_TO_ACCOUNT_ID);
            String toAccountName = jObject.getString(ChatConstant.KEY_TO_ACCOUNT_NAME);
            String content = jObject.getString(ChatConstant.KEY_CONTENT);
            String createdTimeStr = jObject.getString(ChatConstant.KEY_CREATED_TIME);
            String messageId = jObject.getString(ChatConstant.KEY_MESSAGE_ID);

            Date createdTime = new Date(Long.parseLong(createdTimeStr));

            /************ 1.Chat details ************/
            MemberChatMySqlModel memberChatModel = new MemberChatMySqlModel();
            memberChatModel.setFromAccountId(fromAccountId);
            memberChatModel.setFromAccountName(fromAccountName);
            memberChatModel.setFromMemberId(fromMemberId);
            memberChatModel.setFromMemberName(fromMemberName);
            memberChatModel.setToMemberId(toMemberId);
            memberChatModel.setToMemberName(toMemberName);
            memberChatModel.setToAccountId(toAccountId);
            memberChatModel.setToAccountName(toAccountName);
            memberChatModel.setContent(content);
            memberChatModel.setDirection(ChatConstant.MESSAGE_DIRECTION_RECV);
            memberChatModel.setStatus(ChatConstant.MESSAGE_STATUS_RECV_READ);
            memberChatModel.setCreatedTime(createdTime);
            memberChatModel.setUpdatedTime(new Date());
            memberChatModel.setMessageId(messageId);


            /************** 2.Push to specified user**************/
            if (!WebSocketServer.sendToOnline(toAccountId, JObject.create(JObject.toJSON(memberChatModel)))) {
                memberChatModel.setStatus(ChatConstant.MESSAGE_STATUS_RECV_UNREAD);
                // If push fails, save the message as unread
                chatUnreadMessageService.addChatUnreadMessage(memberChatModel);
            }

            // Save details
            memberChatRepository.save(memberChatModel);

            /************ 3.Delete the processed message from the queue ************/
            messageQueueRepository.delete(message);

            // Add a recent chat account record
            ChatLastAccountMysqlModel model = new ChatLastAccountMysqlModel();
            model.setAccountId(toAccountId);
            model.setMemberId(toMemberId);
            model.setAccountName(toAccountName);
            model.setMemberName(toMemberName);
            model.setLiaisonAccountId(fromAccountId);
            model.setLiaisonMemberId(fromMemberId);
            model.setLiaisonAccountName(fromAccountName);
            model.setLiaisonMemberName(fromMemberName);
            model.setUpdatedTime(new Date());
            chatLastAccountService.add(model);
        }
    }

    /**
     * Update message status is read
     */
    @Transactional(rollbackFor = Exception.class)
    public void messageUpdateToRead(UpdateToReadApi.Input input) {
        int deleteCount = chatUnreadMessageService.delete(input.getToAccountId(), input.getFromAccountId());
        if (deleteCount > 0) {
            memberChatRepository.updateMessageStatus(input.getToAccountId(), input.getFromAccountId(), ChatConstant.MESSAGE_STATUS_RECV_UNREAD, ChatConstant.MESSAGE_STATUS_RECV_READ);
        }
    }


    /**
     * Generate message ID
     */
    private String generateMessageId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

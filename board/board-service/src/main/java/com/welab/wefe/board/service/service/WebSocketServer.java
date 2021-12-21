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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.constant.ChatConstant;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Websocket service class, serverendpoint is the URL exposed to the client connection
 *
 * @author Johnny.lin
 */
@ServerEndpoint("/chatserver/{token}")
@Component
public class WebSocketServer {

    public static Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    public static MemberChatService memberChatService;

    /**
     * Static variable, used to record the current number of online connections
     */
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    /**
     * The concurrenthashmap stores the websocketserver object corresponding to each client
     */
    public static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

    /**
     * The connection session with a client needs to send data to the client through it
     */
    private Session session;

    /**
     * token
     */
    private String token = "";
    /**
     * Current account ID
     */
    private String accountId;

    /**
     * Call this method after successful connection establishment.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        this.session = session;
        this.token = token;

        CurrentAccount.Info info = CurrentAccount.get(token);
        if (info == null) {
            log.error("Illegal user, the token does not exist: " + token);
            try {
                sendMessage(responseNonchatMessage(StatusCode.LOGIN_REQUIRED.getCode(), "token无效，请重新登录再试", null));
            } catch (IOException e) {
                log.error("token: " + this.token + ", network exception!");
            }
            return;
        }
        accountId = info.id;
        if (!webSocketMap.containsKey(accountId)) {
            //Online number plus 1
            ONLINE_COUNT.incrementAndGet();
        }
        webSocketMap.put(accountId, this);

        log.info("User connection: " + info.phoneNumber + "，token: " + this.token + ", the number of people currently online is:" + ONLINE_COUNT.get());

        try {
            sendMessage(responseNonchatMessage(StatusCode.SUCCESS.getCode(), "连接成功", null));
        } catch (IOException e) {
            log.error("token: " + this.token + ",network exception!");
        }
    }

    /**
     * Call this method when the connection is closed
     */
    @OnClose
    public void onClose() {
        if (StringUtil.isEmpty(accountId)) {
            return;
        }
        if (webSocketMap.containsKey(accountId)) {
            webSocketMap.remove(accountId);
            ONLINE_COUNT.decrementAndGet();
        }

        CurrentAccount.Info info = CurrentAccount.get(token);
        log.info("User exit: " + info.phoneNumber + ", token: " + token + ",the number of people currently online is:" + ONLINE_COUNT.get());
    }

    /**
     * Call this method after receiving client message.
     *
     * @param message Messages sent by the client
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        log.info("User message: {},content: {}", token, message);

        CurrentAccount.Info info = CurrentAccount.get(token);
        if (info == null) {
            sendMessage(responseNonchatMessage(StatusCode.LOGIN_REQUIRED.getCode(), "token无效，请重新登录再试", null));
            return;
        }

        log.info("user：" + info.phoneNumber);
        if (StringUtil.isEmpty(message)) {
            sendMessage(responseNonchatMessage(StatusCode.ILLEGAL_REQUEST.getCode(), "消息不能为空", null));
            return;
        }
        // Business data returned to the front end
        JObject data = JObject.create();
        try {
            // If it is a Ping, it indicates that it is the heartbeat sent by the client and directly returns a pong
            if ("ping".equals(message)) {
                sendMessage(responseNonchatMessage(StatusCode.SUCCESS.getCode(), "pong", null));
                return;
            }

            //Parsing message
            JSONObject jsonObject = JSON.parseObject(message);

            String fromAccountId = info.getId();
            String toMemberId = jsonObject.getString(ChatConstant.KEY_TO_MEMBER_ID);
            String toMemberName = jsonObject.getString(ChatConstant.KEY_TO_MEMBER_NAME);
            String toAccountId = jsonObject.getString(ChatConstant.KEY_TO_ACCOUNT_ID);
            String toAccountName = jsonObject.getString(ChatConstant.KEY_TO_ACCOUNT_NAME);
            String content = jsonObject.getString(ChatConstant.KEY_CONTENT);
            // The message ID is returned to the front end for use
            String messageId = jsonObject.getString(ChatConstant.KEY_MESSAGE_ID);
            data = JObject.create().append(ChatConstant.KEY_MESSAGE_ID, messageId);

            // Check the validity of the data
            if (StringUtil.isEmpty(fromAccountId)) {
                throw new StatusCodeWithException("fromAccountId is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isEmpty(toMemberId)) {
                throw new StatusCodeWithException("toMemberId is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }
            if (StringUtil.isEmpty(toAccountId)) {
                throw new StatusCodeWithException("toAccountId is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isEmpty(toMemberName)) {
                throw new StatusCodeWithException("toMemberName is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isEmpty(toAccountName)) {
                throw new StatusCodeWithException("toAccountName is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isEmpty(content)) {
                throw new StatusCodeWithException("content is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (StringUtil.isEmpty(messageId)) {
                throw new StatusCodeWithException("messageId is empty", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (fromAccountId.equals(toAccountId)) {
                throw new StatusCodeWithException("自已不能发送消息给自己", StatusCode.PARAMETER_VALUE_INVALID);
            }

            String nickName = CacheObjects.getAccountMap().get(fromAccountId);
            if (StringUtil.isEmpty(nickName)) {
                throw new StatusCodeWithException(nickName + " is empty", StatusCode.INVALID_USER);
            }

            // send message
            JObject ret = memberChatService.sendMessage(fromAccountId, nickName, toMemberId, toAccountId, toMemberName, toAccountName, content);
            // send fail
            if (!"0".equals(ret.getString(ChatConstant.KEY_CODE))) {
                sendMessage(responseNonchatMessage(StatusCode.SYSTEM_ERROR.getCode(), ret.getString(ChatConstant.KEY_MESSAGE), data.append(ChatConstant.KEY_MEMBER_CHAT_ID, ret.getString(ChatConstant.KEY_MEMBER_CHAT_ID))));
            } else {
                sendMessage(responseNonchatMessage(StatusCode.SUCCESS.getCode(), "发送成功", data));
            }
        } catch (StatusCodeWithException e) {
            sendMessage(responseNonchatMessage(e.getStatusCode().getCode(), e.getMessage(), data));
        } catch (Exception e) {
            log.error("User: " + info.phoneNumber + ", token: " + this.token + ", handle client message exception", e);
            sendMessage(responseNonchatMessage(StatusCode.SYSTEM_ERROR.getCode(), e.getMessage(), data));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("Error : " + this.token + ",reason: ", error);
    }

    /**
     * Server active push
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * Send a chat message to an online user
     *
     * @param data Chat data
     */
    public static boolean sendToOnline(String accountId, JObject data) {
        WebSocketServer webSocketServer = webSocketMap.get(accountId);
        if (null == webSocketServer) {
            log.info("Current user：{} not online, push message：{} fail.", accountId, data.toString());
            return false;
        }
        try {
            webSocketServer.sendMessage(responseChatMessage(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMessage(), JObject.create().append("message", data)));
            return true;
        } catch (Exception e) {
            log.error("Current user：" + accountId + ", push message: " + data.toString() + ", exception：", e);
        }
        return false;
    }


    /**
     * Message sending status in response to the front end
     *
     * @param code    status code
     * @param message message content
     * @param data    business data
     */
    private static String responseNonchatMessage(int code, String message, JObject data) {
        return responseMessage(code, ChatConstant.KEY_NON_CHAT, message, data);
    }

    private static String responseChatMessage(int code, String message, JObject data) {
        return responseMessage(code, ChatConstant.KEY_CHAT, message, data);
    }

    private static String responseMessage(int code, String type, String message, JObject data) {
        data = (null == data ? JObject.create() : data);
        return JObject.create().append(ChatConstant.KEY_CODE, code)
                .append(ChatConstant.KEY_TYPE, type)
                .append(ChatConstant.KEY_MESSAGE, message)
                .append(ChatConstant.KEY_DATA, data).toJSONString();
    }
}

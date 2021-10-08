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

package com.welab.wefe.board.service.constant;

/**
 * Chat module related constants
 *
 * @author aaron.li
 **/
public class ChatConstant {
    /**
     * Message direction: close
     */
    public final static int MESSAGE_DIRECTION_RECV = 0;
    /**
     * Message direction: send
     */
    public final static int MESSAGE_DIRECTION_SEND = 1;

    /**
     * Have read
     */
    public final static int MESSAGE_STATUS_RECV_READ = 0;
    /**
     * unread
     */
    public final static int MESSAGE_STATUS_RECV_UNREAD = 1;
    /**
     * Sent successfully
     */
    public final static int MESSAGE_STATUS_SEND_SUCCESS = 2;
    /**
     * Failed to send
     */
    public final static int MESSAGE_STATUS_SEND_FAIL = 3;

    public final static String KEY_CODE = "code";
    public final static String KEY_MESSAGE = "message";
    public final static String KEY_MEMBER_CHAT_ID = "member_chat_id";
    public final static String KEY_TYPE = "type";
    public final static String KEY_DATA = "data";
    public final static String KEY_FROM_ACCOUNT_ID = "fromAccountId";
    public final static String KEY_FROM_ACCOUNT_NAME = "fromAccountName";
    public final static String KEY_FROM_MEMBER_ID = "fromMemberId";
    public final static String KEY_FROM_MEMBER_NAME = "fromMemberName";
    public final static String KEY_TO_MEMBER_ID = "toMemberId";
    public final static String KEY_TO_MEMBER_NAME = "toMemberName";
    public final static String KEY_TO_ACCOUNT_ID = "toAccountId";
    public final static String KEY_TO_ACCOUNT_NAME = "toAccountName";
    public final static String KEY_CONTENT = "content";
    public final static String KEY_MESSAGE_ID = "messageId";
    public final static String KEY_CHAT = "chat";
    public final static String KEY_NON_CHAT = "non-chat";
    public final static String KEY_CREATED_TIME = "createdTime";
    public final static String KEY_DIRECTION = "direction";
}

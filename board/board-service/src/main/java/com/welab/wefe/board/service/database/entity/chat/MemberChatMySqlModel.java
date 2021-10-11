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

package com.welab.wefe.board.service.database.entity.chat;

import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;

import javax.persistence.Entity;

/**
 * Member chat details
 *
 * @author Johnny.lin
 */
@Entity(name = "member_chat")
public class MemberChatMySqlModel extends AbstractMySqlModel {
    /**
     * Sender account ID
     */
    private String fromAccountId;
    /**
     * Sender account name
     */
    private String fromAccountName;

    /**
     * Sender member ID
     */
    private String fromMemberId;
    /**
     * Sender member name
     */
    private String fromMemberName;

    /**
     * Receiver member ID
     */
    private String toMemberId;
    /**
     * Recipient member name
     */
    private String toMemberName;
    /**
     * Receiver account ID
     */
    private String toAccountId;
    /**
     * Receiver account name
     */
    private String toAccountName;

    /**
     * Chat content
     */
    private String content;
    /**
     * Direction: receive: 0 or send: 1
     */
    private Integer direction;
    /**
     * Status: (0: read, 1: unread, 2: sent successfully, 3: sent failed, 4: read by the other party)
     */
    private Integer status;
    /**
     * Message ID
     */
    private String messageId;

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getFromMemberId() {
        return fromMemberId;
    }

    public void setFromMemberId(String fromMemberId) {
        this.fromMemberId = fromMemberId;
    }

    public String getToMemberId() {
        return toMemberId;
    }

    public void setToMemberId(String toMemberId) {
        this.toMemberId = toMemberId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFromAccountName() {
        return fromAccountName;
    }

    public void setFromAccountName(String fromAccountName) {
        this.fromAccountName = fromAccountName;
    }

    public String getFromMemberName() {
        return fromMemberName;
    }

    public void setFromMemberName(String fromMemberName) {
        this.fromMemberName = fromMemberName;
    }

    public String getToMemberName() {
        return toMemberName;
    }

    public void setToMemberName(String toMemberName) {
        this.toMemberName = toMemberName;
    }

    public String getToAccountName() {
        return toAccountName;
    }

    public void setToAccountName(String toAccountName) {
        this.toAccountName = toAccountName;
    }
}

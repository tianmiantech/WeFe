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
 * Member chat unread messages
 *
 * @author Johnny.lin
 */
@Entity(name = "chat_unread_message")
public class ChatUnreadMessageMySqlModel extends AbstractMySqlModel {
    /**
     * Sender account ID
     */
    private String fromAccountId;

    /**
     * Sender member ID
     */
    private String fromMemberId;

    /**
     * Receiver member ID
     */
    private String toMemberId;

    /**
     * Receiver account ID
     */
    private String toAccountId;

    /**
     * Number of unread messages
     */
    private Integer num;

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

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}

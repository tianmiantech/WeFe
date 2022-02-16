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

package com.welab.wefe.board.service.dto.entity;

import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * 聊天详情输出对象
 *
 * @author aaron.li
 **/
public class MemberChatOutputModel extends AbstractOutputModel {

    @Check(name = "发送方的账号id")
    private String fromAccountId;
    @Check(name = "发送方成员ID")
    private String fromMemberId;

    @Check(name = "接收方的账号id")
    private String toAccountId;
    @Check(name = "发送方成员名称")
    private String toMemberId;

    @Check(name = "聊天内容")
    private String content;
    @Check(name = "状态：（0：已读、1：未读、2、发送成功、3、发送失败）")
    private Integer status;
    @Check(name = "消息ID")
    private String messageId;

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}

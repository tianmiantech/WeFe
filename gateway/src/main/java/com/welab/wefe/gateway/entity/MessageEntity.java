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

package com.welab.wefe.gateway.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractUniqueIDEntity;
import com.welab.wefe.common.wefe.enums.MessageEvent;
import com.welab.wefe.common.wefe.enums.MessageLevel;
import com.welab.wefe.common.wefe.enums.ProducerType;

import javax.persistence.*;

/**
 * Message body entity
 *
 * @author aaron.li
 **/
@Table(name = "message")
@Entity
public class MessageEntity extends AbstractUniqueIDEntity {

    /**
     * Creator
     */
    @Column(name = "created_by")
    private String createdBy;

    /**
     * Updater
     */
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Message producer enumeration（board/gateway）
     */
    @Column(name = "producer")
    @Enumerated(EnumType.STRING)
    private ProducerType producer;

    /**
     * Message level enumeration（info/success/error/warning）
     */
    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private MessageLevel level;
    /**
     * 消息关联的事件
     */
    @Enumerated(EnumType.STRING)
    private MessageEvent event;
    /**
     * title
     */
    @Column(name = "title")
    private String title;

    /**
     * content
     */
    @Column(name = "content")
    private String content;

    /**
     * Unread
     */
    @Column(name = "unread")
    private Boolean unread;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ProducerType getProducer() {
        return producer;
    }

    public void setProducer(ProducerType producer) {
        this.producer = producer;
    }

    public MessageLevel getLevel() {
        return level;
    }

    public void setLevel(MessageLevel level) {
        this.level = level;
    }

    public MessageEvent getEvent() {
        return event;
    }

    public void setEvent(MessageEvent event) {
        this.event = event;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getUnread() {
        return unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }
}

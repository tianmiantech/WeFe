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

package com.welab.wefe.board.service.database.entity;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.MessageLevel;
import com.welab.wefe.common.enums.ProducerType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Zane
 */
@Entity(name = "message")
public class MessageMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 消息生产者;枚举（board/gateway）
     */
    @Enumerated(EnumType.STRING)
    private ProducerType producer;
    /**
     * 消息级别;枚举（info/success/error/warning）
     */
    @Enumerated(EnumType.STRING)
    private MessageLevel level;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 未读
     */
    private Boolean unread;

    //region getter/setter

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


    //endregion
}

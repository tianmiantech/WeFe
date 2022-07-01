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

package com.welab.wefe.board.service.database.entity;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.wefe.enums.MessageEvent;
import com.welab.wefe.common.wefe.enums.MessageLevel;
import com.welab.wefe.common.wefe.enums.ProducerType;

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
     * 消息关联的事件
     */
    @Enumerated(EnumType.STRING)
    private MessageEvent event;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 是否未读
     */
    private Boolean unread;
    /**
     * 是否是待办事项
     */
    private Boolean todo;
    /**
     * 待办事项是否已完成
     */
    private Boolean todoComplete;
    /**
     * 待办事项关联对象Id1
     */
    private String todoRelatedId1;
    /**
     * 待办事项关联对象Id2
     */
    private String todoRelatedId2;

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

    public Boolean getTodo() {
        return todo;
    }

    public void setTodo(Boolean todo) {
        this.todo = todo;
    }

    public Boolean getTodoComplete() {
        return todoComplete;
    }

    public void setTodoComplete(Boolean todoComplete) {
        this.todoComplete = todoComplete;
    }

    public String getTodoRelatedId1() {
        return todoRelatedId1;
    }

    public void setTodoRelatedId1(String todoRelatedId1) {
        this.todoRelatedId1 = todoRelatedId1;
    }

    public String getTodoRelatedId2() {
        return todoRelatedId2;
    }

    public void setTodoRelatedId2(String todoRelatedId2) {
        this.todoRelatedId2 = todoRelatedId2;
    }

    //endregion
}

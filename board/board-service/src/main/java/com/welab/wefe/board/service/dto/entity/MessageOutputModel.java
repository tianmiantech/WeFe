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
import com.welab.wefe.common.wefe.enums.MessageEvent;
import com.welab.wefe.common.wefe.enums.MessageLevel;

/**
 * @author Zane
 */
public class MessageOutputModel extends AbstractOutputModel {

    @Check(name = "消息生产者;枚举（board/gateway）")
    private String producer;
    @Check(name = "消息级别;枚举（info/success/error/warning）")
    private MessageLevel level;
    @Check(name = "消息关联的事件")
    private MessageEvent event;
    @Check(name = "标题")
    private String title;
    @Check(name = "内容")
    private String content;
    @Check(name = "未读")
    private Boolean unread;
    @Check(name = "是否是待办事项")
    private Boolean todo;
    @Check(name = "待办事项是否已完成")
    private Boolean todoComplete;

    //region getter/setter

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
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

    public MessageEvent getEvent() {
        return event;
    }

    public void setEvent(MessageEvent event) {
        this.event = event;
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

    //endregion
}

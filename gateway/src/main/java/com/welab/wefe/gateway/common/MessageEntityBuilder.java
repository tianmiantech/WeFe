/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.common;


import com.welab.wefe.common.wefe.enums.MessageLevel;
import com.welab.wefe.common.wefe.enums.ProducerType;
import com.welab.wefe.gateway.entity.MessageEntity;

/**
 * System message constructor
 *
 * @author aaron.li
 **/
public class MessageEntityBuilder {

    public static MessageEntity createDefault() {
        MessageEntity entity = new MessageEntity();
        entity.setProducer(ProducerType.gateway.name());
        entity.setLevel(MessageLevel.info.name());
        entity.setUnread(true);
        entity.setUpdatedTime(null);
        return entity;
    }

    public static MessageEntity create(MessageLevel messageLevel, String title, String content) {
        MessageEntity entity = createDefault();
        entity.setLevel(messageLevel.name());
        entity.setTitle(title);
        entity.setContent(content);
        entity.setUnread(true);
        entity.setUpdatedTime(null);
        return entity;
    }

    public static MessageEntity createSuccess(String title, String content) {
        MessageEntity entity = createDefault();
        entity.setLevel(MessageLevel.success.name());
        entity.setTitle(title);
        entity.setContent(content);
        entity.setUnread(true);
        entity.setUpdatedTime(null);
        return entity;
    }

    public static MessageEntity createError(String title, String content) {
        MessageEntity entity = createSuccess(title, content);
        entity.setLevel(MessageLevel.error.name());
        return entity;
    }

}

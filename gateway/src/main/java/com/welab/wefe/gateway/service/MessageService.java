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

package com.welab.wefe.gateway.service;

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.common.MessageEntityBuilder;
import com.welab.wefe.gateway.entity.MessageEntity;
import com.welab.wefe.gateway.repository.MessageRepository;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author aaron.li
 **/
@Service
public class MessageService {
    /**
     * Cache is used for de duplication of gateway messages to avoid writing a large number of duplicate messages in a short time
     */
    private static ExpiringMap<Integer, String> MESSAGE_CACHE = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(60, TimeUnit.SECONDS)
            .maxSize(100)
            .build();

    private final Logger LOG = LoggerFactory.getLogger(SendTransferMetaService.class);

    @Autowired
    private MessageRepository mMessageRepository;

    public void save(MessageEntity messageEntity) {

        int hash = messageEntity.getProducer().hashCode()
                + messageEntity.getLevel().hashCode()
                + messageEntity.getTitle().hashCode();

        // If the message has been sent recently, it will not be sent again.
        if (MESSAGE_CACHE.containsKey(hash)) {
            return;
        }

        MESSAGE_CACHE.put(hash, "");

        mMessageRepository.save(messageEntity);
    }

    public void saveError(String title, BasicMetaProto.ReturnStatus returnStatus, GatewayMetaProto.TransferMeta transferMeta) {
        try {
            save(MessageEntityBuilder.createError("网关：" + title, "发送方：" + transferMeta.getSrc().getMemberName() + "，接收方：" + transferMeta.getDst().getMemberName() + "，session id: " + transferMeta.getSessionId() + "，错误原因：" + returnStatus.getMessage()));
        } catch (Exception e) {
            LOG.error("MessageService saveError exception: ", e);
        }
    }

    public void saveError(String title, String errorMsg, GatewayMetaProto.TransferMeta transferMeta) {
        try {
            save(MessageEntityBuilder.createError("网关：" + title, "发送方：" + transferMeta.getSrc().getMemberName() + "，接收方：" + transferMeta.getDst().getMemberName() + "，session id: " + transferMeta.getSessionId() + "，错误原因：" + errorMsg));
        } catch (Exception e) {
            LOG.error("MessageService saveError exception: ", e);
        }
    }

    public void saveError(String title, String content) {
        try {
            save(MessageEntityBuilder.createError("网关：" + title, content));
        } catch (Exception e) {
            LOG.error("MessageService saveError exception: ", e);
        }
    }
}

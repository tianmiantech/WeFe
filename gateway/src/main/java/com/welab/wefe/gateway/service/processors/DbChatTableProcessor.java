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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.common.enums.ProducerType;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.entity.MessageQueueEntity;
import com.welab.wefe.gateway.service.MessageQueueService;

/**
 * The message is saved to the exchange center queue list processor of MySQL
 *
 * @author aaron.li
 **/
@Processor(name = "dbChatTableProcessor", desc = "The message is saved to the exchange center queue list processor of MySQL")
public class DbChatTableProcessor extends AbstractProcessor {

    @Override
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            save(transferMeta);
        } catch (Exception e) {
            LOG.error("DBChatTableEndProcessor fail, exception：", e);
            return ReturnStatusBuilder.sysExc(e.getMessage(), transferMeta.getSessionId());
        }

        return ReturnStatusBuilder.ok(transferMeta.getSessionId());
    }

    private void save(GatewayMetaProto.TransferMeta transferMeta) {
        MessageQueueEntity messageQueueEntity = new MessageQueueEntity();
        messageQueueEntity.setCreatedBy(ProducerType.gateway.name());
        messageQueueEntity.setUpdatedBy(null);
        messageQueueEntity.setProducer(ProducerType.gateway.name());
        messageQueueEntity.setUpdatedTime(null);
        messageQueueEntity.setPriority(0);
        messageQueueEntity.setAction(transferMeta.getAction());
        messageQueueEntity.setParams(transferMeta.getContent().getObjectData());
        messageQueueEntity.setChannel(transferMeta.getChannel());
        GatewayServer.CONTEXT.getBean(MessageQueueService.class).save(messageQueueEntity);
    }
}

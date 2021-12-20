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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.cache.SendTransferMetaCache;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.common.ReturnStatusEnum;
import com.welab.wefe.gateway.common.StorageConstant;
import com.welab.wefe.gateway.service.MessageService;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaCachePersistentService;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSink;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSource;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Forwarding big data transmission processor (such as CK database data)
 *
 * @author aaron.li
 **/
@Processor(name = "dSourceProcessor", desc = "Forwarding big data transmission processor (such as CK database data)")
public class DsourceProcessor extends AbstractProcessor {

    @Autowired
    private AbstractTransferMetaDataSource transferMetaDataSource;

    @Autowired
    private AbstractTransferMetaDataSink transferMetaDataSink;

    @Override
    public BasicMetaProto.ReturnStatus preSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        JObject objectData = null;
        try {
            objectData = JObject.create(transferMeta.getContent().getObjectData());
        } catch (Exception e) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "ObjectData Illegal data structure ", transferMeta.getSessionId());
        }

        // Database name and table name
        String namespace = objectData.getString(StorageConstant.NAMESPACE_KEY);
        String name = objectData.getString(StorageConstant.NAME_KEY);
        if (StringUtil.isEmpty(namespace)) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "namespace cannot be empty.", transferMeta.getSessionId());
        }
        if (StringUtil.isEmpty(name)) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "name cannot be empty.", transferMeta.getSessionId());
        }

        // Persistent message
        AbstractSendTransferMetaCachePersistentService persistentService = GatewayServer.CONTEXT.getBean(AbstractSendTransferMetaCachePersistentService.class);
        StatusCodeWithException statusCodeWithException = persistentService.save(transferMeta);
        if (statusCodeWithException.getStatusCode().equals(StatusCode.SUCCESS)) {
            // After the persistence is successful, it is added to the cache and wait for the asynchronous thread to process the forwarding
            SendTransferMetaCache.getInstance().add(transferMeta);
            LOG.info("Message cache succeeded：{}", TransferMetaUtil.toMessageString(transferMeta));
            return ReturnStatusBuilder.ok(transferMeta.getSessionId());
        } else {
            LOG.error("Message caching failed：{} ---> {}", TransferMetaUtil.toMessageString(transferMeta), statusCodeWithException.getMessage());
            GatewayServer.CONTEXT.getBean(MessageService.class).saveError("消息缓存失败", statusCodeWithException.getMessage(), transferMeta);
            return ReturnStatusBuilder.sysExc("持久化消息失败," + statusCodeWithException.getMessage(), transferMeta.getSessionId());
        }
    }

    /**
     * Push streaming data to the remote end
     *
     * @param transferMeta Message（Contains the location where the data to be forwarded is stored）
     * @return Response results
     */
    public BasicMetaProto.ReturnStatus pushStreamDateToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        return transferMetaDataSource.getDataAndPushToRemote(transferMeta);
    }

    /**
     * The method called by the remote end after the remote end receives the message
     *
     * @param transferMeta Streaming data
     */
    public void recvStreamDateHandle(GatewayMetaProto.TransferMeta transferMeta) throws Exception {
        transferMetaDataSink.sink(transferMeta);
    }
}

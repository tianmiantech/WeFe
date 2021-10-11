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

package com.welab.wefe.gateway.service;

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaCachePersistentService;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaService;
import com.welab.wefe.gateway.service.processors.DsourceProcessor;
import com.welab.wefe.gateway.service.processors.ProcessorContext;
import com.welab.wefe.gateway.util.ReturnStatusUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author aaron.li
 **/
@Service
public class SendTransferMetaService extends AbstractSendTransferMetaService {
    private final Logger LOG = LoggerFactory.getLogger(SendTransferMetaService.class);

    @Autowired
    private MessageService mMessageService;

    @Autowired
    private AbstractSendTransferMetaCachePersistentService sendTransferMetaCachePersistentService;

    @Autowired
    private DsourceProcessor dsourceProcessor;


    @Override
    public BasicMetaProto.ReturnStatus doHandle(GatewayMetaProto.TransferMeta transferMeta) {
        return ProcessorContext.preToRemoteExecute(transferMeta);
    }

    @Override
    public BasicMetaProto.ReturnStatus doHandleCache(GatewayMetaProto.TransferMeta transferMeta) {
        BasicMetaProto.ReturnStatus returnStatus = dsourceProcessor.pushStreamDateToRemote(transferMeta);
        if (ReturnStatusUtil.ok(returnStatus)) {
            LOG.info("Message push succeeded: {}", TransferMetaUtil.toMessageString(transferMeta));
        } else {
            LOG.error("Message push failed：{} ---> {}", TransferMetaUtil.toMessageString(transferMeta), returnStatus.getMessage());
            mMessageService.saveError("向 " + transferMeta.getDst().getMemberName() + " 推送消息失败", returnStatus, transferMeta);
        }
        sendTransferMetaCachePersistentService.delete(transferMeta);
        return returnStatus;
    }
}

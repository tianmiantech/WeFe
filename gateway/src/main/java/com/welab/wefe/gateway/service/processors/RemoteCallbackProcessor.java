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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Notify the other party of an error message
 */
@Processor(type = GatewayProcessorType.remoteCallbackProcessor, desc = "Notify the other party of an error message")
public class RemoteCallbackProcessor extends AbstractProcessor {
    @Autowired
    private MessageService messageService;

    @Override
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        String memberName = transferMeta.getSrc().getMemberName();
        String message = transferMeta.getContent().getStrData();
        messageService.saveError("远端错误消息", "成员[" + memberName + "]响应错误内容：" + message);
        return super.remoteProcess(transferMeta);
    }
}

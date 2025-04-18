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
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.init.InitStorageManager;

/**
 * Refresh persistent storage processor
 */
@Processor(type = GatewayProcessorType.refreshPersistentStorageProcessor, desc = "Refresh persistent storage processor")
public class RefreshPersistentStorageProcessor extends AbstractProcessor {
    @Override
    public BasicMetaProto.ReturnStatus beforeSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        if (InitStorageManager.initPersistent(true)) {
            return ReturnStatusBuilder.ok(transferMeta.getSessionId());
        } else {
            return ReturnStatusBuilder.sysExc("刷新持久层服务失败.", transferMeta.getSessionId());
        }
    }
}

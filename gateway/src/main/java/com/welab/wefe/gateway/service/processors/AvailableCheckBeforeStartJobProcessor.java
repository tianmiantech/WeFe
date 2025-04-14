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
import com.welab.wefe.gateway.common.ReturnStatusEnum;
import com.welab.wefe.gateway.init.InitStorageManager;

/**
 * Available check before start job processor
 */
@Processor(type = GatewayProcessorType.availableCheckBeforeStartJobProcessor, desc = "Available check before start job processor")
public class AvailableCheckBeforeStartJobProcessor extends AbstractProcessor {

    @Override
    public BasicMetaProto.ReturnStatus beforeSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        if (InitStorageManager.PERSISTENT_INIT.get()) {
            return ReturnStatusBuilder.ok(transferMeta.getSessionId());
        }
        return ReturnStatusBuilder.create(ReturnStatusEnum.SYS_EXCEPTION.getCode(), "Persistent service uninitialized.", transferMeta.getSessionId());
    }
}

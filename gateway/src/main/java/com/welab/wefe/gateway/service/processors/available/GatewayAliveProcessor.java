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

package com.welab.wefe.gateway.service.processors.available;

import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.service.processors.AbstractProcessor;

/**
 * Gateway survival processor
 *
 * @author aaron.li
 **/
@Processor(type = GatewayProcessorType.gatewayAliveProcessor, desc = "Gateway survival processor")
public class GatewayAliveProcessor extends AbstractProcessor {
    @Override
    public BasicMetaProto.ReturnStatus beforeSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member srcMember = transferMeta.getSrc();
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        String srcUri = EndpointBuilder.generateUri(srcMember.getEndpoint());
        String dstUri = EndpointBuilder.generateUri(dstMember.getEndpoint());
        // 证明是测试内网连接通性
        if (srcMember.getMemberId().equals(dstMember.getMemberId()) && srcUri.equals(dstUri)) {
            return ReturnStatusBuilder.ok(transferMeta.getSessionId());
        }
        // 测试外网连接通性
        return toRemote(transferMeta);
    }
}

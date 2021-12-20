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
package com.welab.wefe.gateway.test;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.TransferServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.UUID;

/**
 * @author zane
 * @date 2021/12/20
 */
public class Client {

    public static String send(String dstMemberId, String dstMemberName, GatewayActionType action, String data, GatewayProcessorType processorType) throws Exception {
        GatewayMetaProto.TransferMeta transferMeta = buildTransferMeta(dstMemberId, dstMemberName, action, data, processorType);
        ManagedChannel grpcChannel = ManagedChannelBuilder
                .forTarget("localhost:50051")
                .usePlaintext()
                .build();
        TransferServiceGrpc.TransferServiceBlockingStub clientStub = TransferServiceGrpc.newBlockingStub(grpcChannel);
        BasicMetaProto.ReturnStatus returnStatus = clientStub.send(transferMeta);
        if (returnStatus.getCode() != 0) {
            throw new Exception(returnStatus.getMessage());
        }

        return returnStatus.getData();

    }


    private static GatewayMetaProto.TransferMeta buildTransferMeta(String dstMemberId, String dstMemberName, GatewayActionType action, String data, GatewayProcessorType processorType) {
        GatewayMetaProto.Member.Builder builder = GatewayMetaProto.Member.newBuilder()
                .setMemberId(dstMemberId);

        if (StringUtil.isNotBlank(dstMemberName)) {
            builder.setMemberName(dstMemberName);
        }

        GatewayMetaProto.Member dstMember = builder
                .build();

        GatewayMetaProto.Content content = GatewayMetaProto.Content.newBuilder()
                .setObjectData(data)
                .build();

        return GatewayMetaProto.TransferMeta.newBuilder()
                .setAction(action.name())
                .setDst(dstMember)
                .setContent(content)
                .setSessionId(UUID.randomUUID().toString().replaceAll("-", ""))
                .setProcessor(processorType.name())
                .build();

    }
}

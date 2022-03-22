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

    public static String callLocalGateway(GatewayProcessorType processorType, String data) throws Exception {
        return callGateway(
                // dev03
                "290007c2a71d470ba00f486b18875d31",
                "local_test",
                GatewayActionType.none,
                processorType,
                data
        );
    }

    public static String callGateway(String dstMemberId, String dstMemberName, GatewayActionType action, GatewayProcessorType processorType, String data) throws Exception {
        GatewayMetaProto.TransferMeta transferMeta = buildTransferMeta(dstMemberId, dstMemberName, action, data, processorType);
        ManagedChannel grpcChannel = ManagedChannelBuilder
                .forTarget("127.0.0.1:50051")
                .usePlaintext()
                .build();
        TransferServiceGrpc.TransferServiceBlockingStub clientStub = TransferServiceGrpc.newBlockingStub(grpcChannel);
        BasicMetaProto.ReturnStatus result = clientStub.send(transferMeta);

        System.out.println("sessionId: " + result.getSessionId());
        System.out.println("code: " + result.getCode());
        System.out.println("message: " + result.getMessage());
        System.out.println("data: " + result.getData());

        if (result.getCode() != 0) {
            throw new Exception(result.getMessage());
        }

        return result.getData();

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

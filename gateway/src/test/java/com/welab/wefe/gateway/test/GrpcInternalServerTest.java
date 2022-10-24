package com.welab.wefe.gateway.test;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.TransferServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.UUID;

public class GrpcInternalServerTest {

    public static void main(String[] args) {
        ManagedChannel grpcChannel = ManagedChannelBuilder.forTarget("127.0.0.1:50052").usePlaintext().build();
        TransferServiceGrpc.TransferServiceBlockingStub clientStub = TransferServiceGrpc.newBlockingStub(grpcChannel);
        BasicMetaProto.ReturnStatus result = clientStub.send(buildTransferMeta());
        System.out.println(result.getMessage());
    }


    private static GatewayMetaProto.TransferMeta buildTransferMeta() {
        String dstMemberId = "";
        String dstMemberName = "";
        String data = JObject.create().toString();
        GatewayProcessorType processorType = GatewayProcessorType.gatewayAliveProcessor;
        GatewayMetaProto.Member.Builder builder = GatewayMetaProto.Member.newBuilder().setMemberId(dstMemberId);

        if (StringUtil.isNotBlank(dstMemberName)) {
            builder.setMemberName(dstMemberName);
        }

        GatewayMetaProto.Member dstMember = builder.build();

        GatewayMetaProto.Content content = GatewayMetaProto.Content.newBuilder().setStrData(data).build();

        return GatewayMetaProto.TransferMeta.newBuilder().setDst(dstMember).setContent(content)
                .setSessionId(UUID.randomUUID().toString().replaceAll("-", "")).setProcessor(processorType.name())
                .build();

    }
}

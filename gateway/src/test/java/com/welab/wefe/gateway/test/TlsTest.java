package com.welab.wefe.gateway.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.cache.CaCertificateCache;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.util.ExceptionUtil;
import com.welab.wefe.gateway.util.GrpcUtil;
import com.welab.wefe.gateway.util.TlsUtil;
import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TlsTest {
    public static void main(String[] args) {
        try {


            String filePath = "D:\\workspace\\idea\\gateway\\src\\test\\java\\com\\welab\\wefe\\gateway\\test\\ca.json";
            JObject fileContent = JObject.create(FileUtil.readAllText(filePath));
            JSONArray caCertificateDataArray = JObject.parseArray(fileContent.getStringByPath("data.list"));
            List<CaCertificateCache.CaCertificate> resultList = new ArrayList<>();
            CaCertificateCache.CaCertificate caCertificate = null;
            for (int i = 0; i < caCertificateDataArray.size(); i++) {
                caCertificate = new CaCertificateCache.CaCertificate();
                JSONObject caCertificateDataObj = caCertificateDataArray.getJSONObject(i);
                caCertificate.setId(caCertificateDataObj.getString("serial_number"));
                caCertificate.setName(caCertificateDataObj.getString("subject_cn"));
                caCertificate.setContent(caCertificateDataObj.getString("cert_content"));

                resultList.add(caCertificate);
            }
            ManagedChannel originalChannel = GrpcUtil.getSslManagedChannel(EndpointBuilder.create("10.5.210.112:50051"), TlsUtil.buildCertificates(Arrays.asList(resultList.get(0))));
            //Channel channel = ClientInterceptors.intercept(originalChannel, new SystemTimestampVerifyClientInterceptor(), new SignVerifyClientInterceptor(), new AntiTamperClientInterceptor());
            NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceBlockingStub clientStub = NetworkDataTransferProxyServiceGrpc.newBlockingStub(originalChannel);
            BasicMetaProto.ReturnStatus returnStatus = clientStub.push(buildTransferMeta("087973c99d26410683944bf3f46c8635", "test", JObject.create("a", "b").toString(), GatewayProcessorType.gatewayAliveProcessor));

            System.out.println(returnStatus.getMessage());
        } catch (Exception e) {
            System.out.println("===================================================");
            System.out.println(ExceptionUtil.getStackTraceInfo(e));
            String message = e.getMessage();

        }

    }

    private static GatewayMetaProto.TransferMeta buildTransferMeta(String dstMemberId, String dstMemberName, String data, GatewayProcessorType processorType) {
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

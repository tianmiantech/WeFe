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

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLException;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.TransferServiceGrpc;
import com.welab.wefe.gateway.cache.CaCertificateCache;
import com.welab.wefe.gateway.util.TlsUtil;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * @author zane
 * @date 2021/12/20
 */
public class Client {

    public static void main(String[] args) throws Exception {
        Client.callLocalGateway(GatewayProcessorType.gatewayAliveProcessor, "");
    }

    public static String callLocalGateway(GatewayProcessorType processorType, String data) throws Exception {
        return callGateway1("d0f47307804844898ecfc65b875abe87", "local_test", GatewayActionType.none, processorType,
                data);
    }

    public static String callGateway1(String dstMemberId, String dstMemberName, GatewayActionType action,
            GatewayProcessorType processorType, String data) throws Exception {
        GatewayMetaProto.TransferMeta transferMeta = buildTransferMeta(dstMemberId, dstMemberName, action, data,
                processorType);
        List<CaCertificateCache.CaCertificate> caCertificateList = new ArrayList<>();
        CaCertificateCache.CaCertificate ca = new CaCertificateCache.CaCertificate();
        String content = "-----BEGIN CERTIFICATE-----\nMIIDOzCCAiOgAwIBAgIJAOzRWqoiV0gfMA0GCSqGSIb3DQEBCwUAMDoxFjAUBgNV\nBAMMDVdlbGFiIFJvb3QgQ0ExEzARBgNVBAoMCldlbGFiIEluYy4xCzAJBgNVBAsM\nAklUMB4XDTIyMDgxNzAyMzYxMFoXDTMyMDgxNDAyMzYxMFowOjEWMBQGA1UEAwwN\nV2VsYWIgUm9vdCBDQTETMBEGA1UECgwKV2VsYWIgSW5jLjELMAkGA1UECwwCSVQw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCTFQ/4Dw3u6RpFnuzAP1YE\nMrSwGI+48cK9JE/GVA9C3fVbRU/t0bkZSmUQ2a7iM8diEj0OnjXcIqQN2fW6BR38\nItqegWM9i15CWLO6Pw94AKSGKnYBi4YQsGD+KstfesOYsnYQtneX6S7bgMu3wZRf\nPYkkkKaRjVBiZV2J9LSAW4cHJBej+GP/bfDT0VD7A9ibx4YAjKbwEz/1f9zCnqEC\nexcOyeTUHv34xEY9t9sD2ql7horvvIMU+HvnQv4g+0WNi0+ItOMwRsUXZLK+frto\nno0b1IcwtNlYy46R1OMmX6GTl+UgKu3yLzaBRG7uBkvO2wI2z5nlDQRk/Bf2JsJr\nAgMBAAGjRDBCMAwGA1UdEwQFMAMBAf8wCwYDVR0PBAQDAgQQMCUGA1UdEQQeMByC\nGndlZmUudGlhbm1pYW50ZWNoLmNvbS50ZXN0MA0GCSqGSIb3DQEBCwUAA4IBAQB5\n1qkzM84uZUecDOo79bnXHlY5q1NObVwLz/tKpGV/qWTpDxW8eSqr0y1enmJc3wzr\nD/Avo5xtsK2rFR3LATp5CDrmU7MTiApGJuKZwTtGVbICr0qvyppIKOB2Y8LTE5xy\nN2YQ/hSIoAHozQgizuuzWUWA+hJFjDe/1T9sd5X+TdmJ456pRMWAAQCgmb0INc0y\nW0QyMyI/3omZ4Q/xHGaLU41x7aPwLPJghPO0AaUVeHlvl4WFR8pKTCswflqIKY1K\nN1Xyl1AdC14Z+zKg+o2GfWyIKSLoD3Id51R/uS2PNsg0AFDRqAuSLDGcVtsrpGWG\n/IhCd+sX0mBQm2UBuxn+\n-----END CERTIFICATE-----";
        ca.setContent(content);
        caCertificateList.add(ca);
        ManagedChannel grpcChannel = getSslManagedChannel("10.2.210.6", 50050,
                TlsUtil.buildCertificates(caCertificateList));
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

    public static ManagedChannel getSslManagedChannel(String ip, int port, X509Certificate[] x509Certificates)
            throws SSLException {
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();
        if (null == x509Certificates || x509Certificates.length == 0) {
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        } else {
            sslContextBuilder.trustManager(x509Certificates);
        }
        return NettyChannelBuilder.forTarget(ip + ":" + port).negotiationType(NegotiationType.TLS)
                .sslContext(sslContextBuilder.build()).overrideAuthority(DEFAULT_DOMAIN)
                .maxInboundMetadataSize(2000 * 1024 * 1024).build();
    }

    public static String callGateway(String dstMemberId, String dstMemberName, GatewayActionType action,
            GatewayProcessorType processorType, String data) throws Exception {
        GatewayMetaProto.TransferMeta transferMeta = buildTransferMeta(dstMemberId, dstMemberName, action, data,
                processorType);
        ManagedChannel grpcChannel = ManagedChannelBuilder.forTarget("127.0.0.1:50051").usePlaintext().build();
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

    private static GatewayMetaProto.TransferMeta buildTransferMeta(String dstMemberId, String dstMemberName,
            GatewayActionType action, String data, GatewayProcessorType processorType) {
        GatewayMetaProto.Member.Builder builder = GatewayMetaProto.Member.newBuilder().setMemberId(dstMemberId);

        if (StringUtil.isNotBlank(dstMemberName)) {
            builder.setMemberName(dstMemberName);
        }

        GatewayMetaProto.Member dstMember = builder.build();

        GatewayMetaProto.Content content = GatewayMetaProto.Content.newBuilder().setObjectData(data).build();

        return GatewayMetaProto.TransferMeta.newBuilder().setAction(action.name()).setDst(dstMember).setContent(content)
                .setSessionId(UUID.randomUUID().toString().replaceAll("-", "")).setProcessor(processorType.name())
                .build();

    }

    public static final String DEFAULT_DOMAIN = "wefe.tianmiantech.com.test";
}

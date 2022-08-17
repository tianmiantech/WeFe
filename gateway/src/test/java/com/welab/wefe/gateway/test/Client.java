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
        String content = "-----BEGIN CERTIFICATE-----\nMIIDLjCCAhagAwIBAgIJAIpEr7/Bjx+LMA0GCSqGSIb3DQEBCwUAMDoxFjAUBgNV\nBAMMDVdlbGFiIFJvb3QgQ0ExEzARBgNVBAoMCldlbGFiIEluYy4xCzAJBgNVBAsM\nAklUMB4XDTIyMDgxNzAyMzYwOFoXDTMyMDgxNDAyMzYwOFowOjEWMBQGA1UEAwwN\nV2VsYWIgUm9vdCBDQTETMBEGA1UECgwKV2VsYWIgSW5jLjELMAkGA1UECwwCSVQw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCH6vi+VosXUlixlRphvN5q\nOcXhvb5T1ZNzbYZFhJtkJ53/mo4Smh5iXuOTfyA+4y0MaZ1yZhzoFpxWGHcRcK7D\n4yJ9965vdRBq0Ei8pIHjWCSCN+MHOQl9kST9+b8SJ+ceiMbvaFv0EgPbgAl7irdW\nl2D0yb3EUvOpknIEJpIcFee0c3wzRGzzsGZd2V9li/YHldwyIA9Gek26u28XT/4P\nBKJc6i3vcD5XLBULPil+KcLJz31NTYsP50UKi+dmezUi+XuEJk1KO8YFRThqRg+u\nmDaNcwDW94fLyQ3A5DbhvIY+Ix0kSOGZjPEdCtM1S/HdIoUBGGBDZMunJGcuYPUB\nAgMBAAGjNzA1MAwGA1UdEwQFMAMBAf8wJQYDVR0RBB4wHIIad2VmZS50aWFubWlh\nbnRlY2guY29tLnRlc3QwDQYJKoZIhvcNAQELBQADggEBAEWc0vYnm1SVJU+X2C/E\nEXZmllJ1UdnCTxivW0DoXLfKY09T1YH2lf+nMq0Imuz7FNRCI9ueXrUZUlxluBkL\nB1lKikBcGLkSwGDsPPWjtV7k5oK8vNdTK44OMwBtcx/KtNnyN+pHjBmvmOI+WzVV\n4yh7Jkq1JMerBueIt6DnsnvbY8+NJbOo9ER0BFI/B3MVujsEYw8yE7WyNDvU1vmR\nAPhlX31BFF7woX30bJJ5p7BMD4dP2GmVjj88NYy9r90iVnVmHE3z5H0qdNvuQ7Dw\nv4mdKQJOhdhrC4K1MrTGEpTZ7psOz7MHoH7L9C+CDsNrZMtc7wRmn57NN08seR32\nA8s=\n-----END CERTIFICATE-----";
        ca.setContent(content);
        caCertificateList.add(ca);
        ManagedChannel grpcChannel = getSslManagedChannel("10.2.210.6", 50051,
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

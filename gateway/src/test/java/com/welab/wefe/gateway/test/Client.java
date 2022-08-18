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
        String content = "-----BEGIN CERTIFICATE-----\nMIIDLjCCAhagAwIBAgIJAKvu16gOT8hXMA0GCSqGSIb3DQEBCwUAMDoxFjAUBgNV\nBAMMDVdlbGFiIENBIFJPT1QxEzARBgNVBAoMCldlbGFiIEluYy4xCzAJBgNVBAsM\nAklUMB4XDTIyMDgxODAzNDM0M1oXDTMyMDgxNTAzNDM0M1owOjEWMBQGA1UEAwwN\nV2VsYWIgQ0EgUk9PVDETMBEGA1UECgwKV2VsYWIgSW5jLjELMAkGA1UECwwCSVQw\nggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCqFEql0n4Lfo4hYFMUDt95\nQBAy8WuogoEGTSK9R5SAST9wTQ4tF+wbYeiPfRA0wa5Z+eyNl8uQa+D8rppxtN0W\newsZBWvrCDIs+HNuL6UmZ2hPKSItZ/Jnrl62ELsk+1aQFnx7JYSJIb9KF+im62k4\n9aEUG7SNSpWpjxGhkFinCmwoziRGibdE+3JmfwMU0uC4tT1wzoeark/VBpQZo8AO\niqUoIupEGfmNXtnY1C3ftTjDDNp+bzlHht7C5c24f0k63i9ImDxmBGj3cuzAGpHK\njq+0kNJY62ubMjFOLInIuV4i5RvjenewefkyCbQSbvB8j6ja2Oqb+JZ64dChsehv\nAgMBAAGjNzA1MAwGA1UdEwQFMAMBAf8wJQYDVR0RBB4wHIIad2VmZS50aWFubWlh\nbnRlY2guY29tLnRlc3QwDQYJKoZIhvcNAQELBQADggEBADDEfBHLnVQ7M6BK8QYM\niCnyTh0Vx37ejT9FXq1lwcbpWhc2KWE5pUT2kGxYa1QW59x0YvVoEMe8VGrWBUut\n8WUg9nl1B5qv147fruT6eqcD40pT7mF7uGztaJygUvUxN8PCuglsedoY5kZDunDw\nHEBeWXSJj0k2hQnOg1i4t61R1PL0fshvqY2VbA4cljRyz9UdA7vML74eKjKsIu3P\nPv2a+4Bj5wVEbA9LuBzp6ExfGcN2CxjByP0yGbAQMCogX5MLCjUOb4ETdsTBCXj3\nWcf8WYL0CMQgGIFZqbS3L10434XAI0TqwV5hGR0Gec0VkcsKm4pyAgnWsEFuPwjX\naVc=\n-----END CERTIFICATE-----\n";
        ca.setContent(content);
        caCertificateList.add(ca);
        X509Certificate[] certs = TlsUtil.buildCertificates(caCertificateList);
        System.out.println(certs[0]);
        ManagedChannel grpcChannel = getSslManagedChannel("127.0.0.1", 50051, certs);
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

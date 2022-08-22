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
        String content = "-----BEGIN CERTIFICATE-----\nMIIDNDCCAhygAwIBAgIJAOQYbKajYL2zMA0GCSqGSIb3DQEBCwUAMDYxEjAQBgNV\nBAMMCVdlZmVfUk9PVDETMBEGA1UECgwKV2VsYWIgSW5jLjELMAkGA1UECwwCSVQw\nHhcNMjIwODE5MDU0ODA4WhcNMzIwODE2MDU0ODA4WjA3MRMwEQYDVQQDDApXZWxh\nYl9XZWZlMRMwEQYDVQQKDApXZWxhYiBJbmMuMQswCQYDVQQLDAJJVDCCASIwDQYJ\nKoZIhvcNAQEBBQADggEPADCCAQoCggEBALgxnX7Xp39dg+SyjPYM9BLXjzU1dkoL\nmkULV4MqVlLQFIdlvvG7gAgl3y3b1mkOyPi6ENP8AfoJeTA3m9/rK+0J2JjTWY6b\nv8WL0tS69fyeARDV356qKk36foTvLbDETD2nvVq8s7iVLr9I69Bnc0W8R6TbKhEx\nK12SgqSkYxkKmLPp2fwiSb7hwU0BHOKKejkQ6WET6ncxw6/570mONjBSE3cZJONd\nxQnhNHGIrIPDsGQPYD0lQCdHzvZ/xV6rYJV8/naODqSTDZKfUwJsncuoDmP7woue\na3aKLyrFu2RB18kKy6uIgEQ9iIUexm2Z7prIBOm6HPIcA51ai/3DjU8CAwEAAaNE\nMEIwDAYDVR0TBAUwAwEB/zALBgNVHQ8EBAMCBBAwJQYDVR0RBB4wHIIad2VmZS50\naWFubWlhbnRlY2guY29tLnRlc3QwDQYJKoZIhvcNAQELBQADggEBAIs+ir/VtO7j\n805XPwlQapqU8cloOJLvkk06G3OsETW7xC1pIoc6EgQxASawjAatSGyfYlb0cyFA\nktjRRw98HR1/4XR8g10Dzt9qVbm1rRlVjwc+AXO5zQWAb0DYlIfc/fUVE6cT2udk\njeHdDLUtOoTtEfyHYZh8xUHzLF4PfUkxowyvc9MS1F9+mIm7ZQGQcOXxhK+X/wkj\nNIPZnKWj5X2Pl9uZWrpau6p2S1drZyeGZP78lTGm3Zph/dyWXcIrP1p0WFxrSoiM\nOImNKS+HT7qxZ8br7laTz4zvNx3vWPKOyI00mqxJe0xmWJfUz/4LTIqirkjtHGEO\nuMsE4rLnNyA=\n-----END CERTIFICATE-----\n";
        ca.setContent(content);
        caCertificateList.add(ca);
        X509Certificate[] certs = TlsUtil.buildCertificates(caCertificateList);
        ManagedChannel grpcChannel = getSslManagedChannel("127.0.0.1", 50050, certs);
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
        ManagedChannel grpcChannel = ManagedChannelBuilder.forTarget("127.0.0.1:50050").usePlaintext().build();
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

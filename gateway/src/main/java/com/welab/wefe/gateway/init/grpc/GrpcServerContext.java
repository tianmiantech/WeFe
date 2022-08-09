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
package com.welab.wefe.gateway.init.grpc;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.common.GrpcServerScopeEnum;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.service.MemberService;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;

/**
 * Grpc server context
 */
public class GrpcServerContext {
    protected final static Logger LOG = LoggerFactory.getLogger(GrpcServerContext.class);

    private static GrpcServerContext context = new GrpcServerContext();
    /**
     * Inner grpc server
     */
    private GrpcServer internalGrpcServer;
    /**
     * Outer grpc server
     */
    private GrpcServer externalGrpcServer;

    private GrpcServerContext() {
    }

    public static GrpcServerContext getInstance() {
        return context;
    }

    /**
     * Start grpc server
     */
    public boolean start() {
        try {
            ConfigProperties config = GatewayServer.CONTEXT.getBean(ConfigProperties.class);
            int internalPort = config.getGrpcServerInternalPort();
            int externalPort = config.getGrpcServerExternalPort();
            if ((internalPort == externalPort)) {
                LOG.error("Grpc server start fail, the internal network port is the same as the external network port[" + internalPort + "]");
                return false;
            }

            internalGrpcServer = buildInternalGrpcServer(internalPort);
            externalGrpcServer = buildExternalGrpcServer(externalPort);
            if (!internalGrpcServer.start()) {
                return false;
            }
            return externalGrpcServer.start();
        } catch (Exception e) {
            LOG.error("Start grpc server fail: ", e);
            if (null != internalGrpcServer) {
                internalGrpcServer.stop();
            }
            if (null != externalGrpcServer) {
                externalGrpcServer.stop();
            }
        }

        return false;
    }

    /**
     * Restart external grpc server
     */
    public boolean restartExternalGrpcServer() {
        try {
            MemberService memberService = GatewayServer.CONTEXT.getBean(MemberService.class);
            boolean tlsEnable = memberService.getMemberGatewayTlsEnable();
            if (tlsEnable) {
                externalGrpcServer.setSslContext(buildSslContext());
            }
            externalGrpcServer.setTlsEnable(tlsEnable);
            return externalGrpcServer.restart();
        } catch (Exception e) {
            LOG.error("Restart external grpc server exception: ", e);
        }
        return false;
    }


    /**
     * Build internal grpc server
     */
    private GrpcServer buildInternalGrpcServer(int port) {
        GrpcServer grpcServer = new GrpcServer(port);
        grpcServer.setName("INTERNAL");
        grpcServer.setTlsEnable(false);
        grpcServer.setUseScope(GrpcServerScopeEnum.INTERNAL);
        return grpcServer;
    }

    /**
     * Build external grpc server
     */
    private GrpcServer buildExternalGrpcServer(int port) throws SSLException {
        GrpcServer grpcServer = new GrpcServer(port);
        grpcServer.setName("EXTERNAL");
        grpcServer.setUseScope(GrpcServerScopeEnum.EXTERNAL);
        MemberService memberService = GatewayServer.CONTEXT.getBean(MemberService.class);
        boolean tlsEnable = memberService.getMemberGatewayTlsEnable();
        grpcServer.setTlsEnable(tlsEnable);
        if (grpcServer.isTlsEnable()) {
            grpcServer.setSslContext(buildSslContext());
        }
        return grpcServer;
    }

    /**
     * Build ssl context
     */
    private SslContext buildSslContext() throws SSLException {
        // TODO 待完善中
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File("a"), new File("b"));
        sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL);
        return sslContextBuilder.build();
    }

    public GrpcServer getExternalGrpcServer() {
        return externalGrpcServer;
    }
}

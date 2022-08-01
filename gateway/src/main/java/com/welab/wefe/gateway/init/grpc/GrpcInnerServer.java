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

import com.welab.wefe.gateway.base.RpcServerAnnotate;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.common.RpcServerStatusEnum;
import com.welab.wefe.gateway.common.RpcServerUseScopeEnum;
import com.welab.wefe.gateway.util.ClassUtil;
import io.grpc.*;
import io.grpc.netty.NettyServerBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Grpc inner server
 */
public class GrpcInnerServer extends AbstractGrpcServer {
    private final Logger LOG = LoggerFactory.getLogger(GrpcInnerServer.class);

    /**
     * Grpc service
     */
    private Server server;

    /**
     * Grpc service status
     */
    private RpcServerStatusEnum status = RpcServerStatusEnum.SHUTDOWN;


    /**
     * Start gRpc server
     */
    public void start(int port) throws Exception {
        try {
            Map<String, RpcServerAnnotate> gRpcServerBeans = ClassUtil.loadRpcClassBeans(RpcServerUseScopeEnum.INNER);
            if (gRpcServerBeans.isEmpty()) {
                throw new Exception("start inner gRpc server fail, is not exist available server.");
            }

            // Binding port
            NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
            for (Map.Entry<String, RpcServerAnnotate> entry : gRpcServerBeans.entrySet()) {
                RpcServerAnnotate rpcServerAnnotateConfig = entry.getValue();
                BindableService rpcService = rpcServerAnnotateConfig.getRpcBean();
                List<Class<? extends ServerInterceptor>> interceptors = rpcServerAnnotateConfig.getInterceptors();
                if (CollectionUtils.isNotEmpty(interceptors)) {
                    serverBuilder.addService(ServerInterceptors.intercept(rpcService, listToInstanceArray(interceptors)));
                } else {
                    serverBuilder.addService(rpcService);
                }
            }

            // Set the maximum message that the server can receive（2000M）
            serverBuilder.maxInboundMessageSize(GrpcConstant.MAX_BOUND_MESSAGE_SIZE * 1024 * 1024);
            serverBuilder.compressorRegistry(CompressorRegistry.getDefaultInstance());
            serverBuilder.decompressorRegistry(DecompressorRegistry.getDefaultInstance());
            serverBuilder.keepAliveTimeout(30, TimeUnit.SECONDS);
            // Maximum space time
            serverBuilder.maxConnectionIdle(120, TimeUnit.SECONDS);
            serverBuilder.maxConnectionAge(120, TimeUnit.SECONDS);
            serverBuilder.maxConnectionAgeGrace(180, TimeUnit.SECONDS);
            // Start service
            server = serverBuilder.build().start();
            status = RpcServerStatusEnum.RUNNING;
            // Registration tick
            Runtime.getRuntime().addShutdownHook(new Thread(GrpcInnerServer.this::stop));
            // Start daemon
            blockUntilShutdown();
        } catch (Exception e) {
            LOG.error("Start inner gRpc server start fail:", e);
            throw new Exception("Start inner gRpc server start fail.");
        }
    }


    @Override
    public void restart() {

    }

    @Override
    protected void stop() {
        try {
            LOG.info("start shutting down rpc server.....");
            status = RpcServerStatusEnum.SHUTDOWN;
            if (server != null) {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                server = null;
            }
            LOG.info("shutting down rpc server end.");
        } catch (Exception e) {
            status = RpcServerStatusEnum.RUNNING;
            LOG.error("rpc server shut down exception", e);
        }
    }

    @Override
    protected void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}

/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.init;

import com.welab.wefe.gateway.base.RpcServerAnnotate;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.util.ClassUtil;
import io.grpc.*;
import io.grpc.netty.NettyServerBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Initialize grpc server
 *
 * @author aaron.li
 **/
@Component
public class InitRpcServer {
    private final Logger LOG = LoggerFactory.getLogger(InitRpcServer.class);

    @Autowired
    private ConfigProperties configProperties;

    /**
     * Grpc service
     */
    private Server rpcServer;

    /**
     * Does grpc service stop
     */
    public static boolean SERVER_IS_SHUTDOWN = false;


    /**
     * Start service
     */
    public void start() throws Exception {
        try {
            Map<String, RpcServerAnnotate> rpcClassBeans = ClassUtil.loadRpcClassBeans();
            if (rpcClassBeans.isEmpty()) {
                throw new Exception("start rpc server fail, is not exist available gRpc server.");
            }
            // Binding port
            NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(configProperties.getRpcServerPort());

            for (Map.Entry<String, RpcServerAnnotate> entry : rpcClassBeans.entrySet()) {
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
            serverBuilder.maxInboundMessageSize(2000 * 1024 * 1024);
            serverBuilder.maxInboundMetadataSize(2000 * 1024 * 1024);
            serverBuilder.compressorRegistry(CompressorRegistry.getDefaultInstance());
            serverBuilder.decompressorRegistry(DecompressorRegistry.getDefaultInstance());
            serverBuilder.keepAliveTimeout(30, TimeUnit.SECONDS);
            // Maximum space time
            serverBuilder.maxConnectionIdle(120, TimeUnit.SECONDS);
            serverBuilder.maxConnectionAge(120, TimeUnit.SECONDS);
            serverBuilder.maxConnectionAgeGrace(180, TimeUnit.SECONDS);
            // Start service
            rpcServer = serverBuilder.build().start();
            // Registration tick
            Runtime.getRuntime().addShutdownHook(new Thread(InitRpcServer.this::stop));
            // Start daemon
            blockUntilShutdown();
        } catch (Exception e) {
            LOG.error("rpc server start fail:", e);
            throw new Exception("rpc server start fail:");
        }
    }


    /**
     * Stop grpc service
     */
    private void stop() {
        try {
            LOG.info("start shutting down rpc server.....");
            SERVER_IS_SHUTDOWN = true;
            if (rpcServer != null) {
                rpcServer.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                rpcServer = null;
            }
            LOG.info("shutting down rpc server end.");
        } catch (Exception e) {
            LOG.error("rpc server shut down exception", e);
        }
    }

    /**
     * Start daemon
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (rpcServer != null) {
            rpcServer.awaitTermination();
        }
    }

    /**
     * The interceptor class is converted to the corresponding instance
     *
     * @param interceptors interceptor class list
     * @return Interceptor instance list
     */
    private ServerInterceptor[] listToInstanceArray(List<Class<? extends ServerInterceptor>> interceptors) throws IllegalAccessException, InstantiationException {
        ServerInterceptor[] instanceArray = new ServerInterceptor[interceptors.size()];
        for (int i = 0; i < interceptors.size(); i++) {
            instanceArray[i] = interceptors.get(i).newInstance();
        }

        return instanceArray;
    }
}

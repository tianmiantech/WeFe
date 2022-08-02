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
import io.netty.handler.ssl.SslContext;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract grpc server
 */
public abstract class AbstractGrpcServer {
    private final Logger LOG = LoggerFactory.getLogger(AbstractGrpcServer.class);
    /**
     * Grpc server port
     */
    private int port;

    /**
     * Grpc server object
     */
    private Server server;

    /**
     * Grpc service status
     */
    private RpcServerStatusEnum status = RpcServerStatusEnum.SHUTDOWN;

    public AbstractGrpcServer(int port) {
        this.port = port;
    }


    /**
     * Start grpc server
     */
    public boolean start() throws Exception {
        RpcServerUseScopeEnum useScope = useScope();
        Map<String, RpcServerAnnotate> gRpcServerBeans = ClassUtil.loadRpcClassBeans(useScope);
        if (gRpcServerBeans.isEmpty()) {
            LOG.error("Start " + useScope + " gRpc server fail, is not exist available server.");
            return false;
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
        if (tlsEnable()) {
            serverBuilder.sslContext(buildSslContext());
        }
        // Start service
        server = serverBuilder.build().start();
        status = RpcServerStatusEnum.RUNNING;
        // Registration tick
        Runtime.getRuntime().addShutdownHook(new Thread(AbstractGrpcServer.this::stop));
        // Start daemon
        //blockUntilShutdown();
        return true;
    }

    protected RpcServerUseScopeEnum useScope() {
        return RpcServerUseScopeEnum.BOTH;
    }


    /**
     * restart server
     */
    public boolean restart() throws Exception {
        if (null != server && status.equals(RpcServerStatusEnum.RUNNING)) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        return start();
    }

    /**
     * Start daemon
     */
    protected void blockUntilShutdown() throws InterruptedException {
        if (null != server) {
            server.awaitTermination();
        }
    }


    /**
     * Stop grpc service
     */
    protected void stop() {
        RpcServerUseScopeEnum useScope = useScope();
        try {
            LOG.info("start shutting down " + useScope + " gRpc server.....");
            status = RpcServerStatusEnum.SHUTDOWN;
            if (server != null) {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                server = null;
            }
            LOG.info("shutting down " + useScope + " gRpc server end.");
        } catch (Exception e) {
            status = RpcServerStatusEnum.RUNNING;
            LOG.error(useScope + " gRpc server shut down exception:", e);
        }
    }

    /**
     * Whether the TLS function is enabled in the service
     */
    protected boolean tlsEnable() {
        return false;
    }

    /**
     * build ssl context
     * <p>
     * When the TLS service is enabled, the subclass needs to override this method
     * </p>
     */
    protected SslContext buildSslContext() throws SSLException {
        return null;
    }


    /**
     * The interceptor class is converted to the corresponding instance
     *
     * @param interceptors interceptor class list
     * @return Interceptor instance list
     */
    protected ServerInterceptor[] listToInstanceArray(List<Class<? extends ServerInterceptor>> interceptors) throws IllegalAccessException, InstantiationException {
        ServerInterceptor[] instanceArray = new ServerInterceptor[interceptors.size()];
        for (int i = 0; i < interceptors.size(); i++) {
            instanceArray[i] = interceptors.get(i).newInstance();
        }

        return instanceArray;
    }
}

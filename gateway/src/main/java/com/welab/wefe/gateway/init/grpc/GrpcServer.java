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

import com.welab.wefe.gateway.base.GrpcServerAnnotate;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.common.RpcServerStatusEnum;
import com.welab.wefe.gateway.common.GrpcServerScopeEnum;
import com.welab.wefe.gateway.util.ClassUtil;
import io.grpc.*;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract grpc server
 */
public class GrpcServer {
    private final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);
    /**
     * Grpc server port
     */
    private int port;

    /**
     * server name
     */
    private String name;

    /**
     * Server user scope
     */
    private GrpcServerScopeEnum useScope;

    /**
     * Whether to enable TLS
     */
    private boolean tlsEnable;

    /**
     * To enable TLS, you need to instantiate the sslcontext attribute
     */
    private SslContext sslContext;

    /**
     * Grpc server object
     */
    private Server server;

    /**
     * Grpc service status
     */
    private RpcServerStatusEnum status = RpcServerStatusEnum.SHUTDOWN;


    public GrpcServer(int port) {
        this.port = port;
    }


    /**
     * Start grpc server
     */
    public boolean start() {
        try {
            LOG.info("Start 【" + name + "】 grpc server..........");
            Map<String, GrpcServerAnnotate> gRpcServerBeans = ClassUtil.loadRpcClassBeans(useScope);
            if (gRpcServerBeans.isEmpty()) {
                LOG.error("Start 【" + name + "】 grpc server fail, is not exist available server.");
                return false;
            }
            // Binding port
            NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
            for (Map.Entry<String, GrpcServerAnnotate> entry : gRpcServerBeans.entrySet()) {
                GrpcServerAnnotate rpcServerAnnotateConfig = entry.getValue();
                BindableService rpcService = rpcServerAnnotateConfig.getRpcBean();
                List<Class<? extends ServerInterceptor>> interceptors = rpcServerAnnotateConfig.getInterceptors();
                if (CollectionUtils.isNotEmpty(interceptors)) {
                    serverBuilder.addService(ServerInterceptors.interceptForward(rpcService, listToInstanceArray(interceptors)));
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
            if (tlsEnable) {
                serverBuilder.sslContext(sslContext);
            }
            // Start service
            server = serverBuilder.build().start();
            status = RpcServerStatusEnum.RUNNING;
            // Registration tick
            Runtime.getRuntime().addShutdownHook(new Thread(GrpcServer.this::stop));
            LOG.info("Start 【{}】 grpc server success, binding port is: {}, tls enable: {}", name, port, tlsEnable);
            return true;
        } catch (Exception e) {
            LOG.error("Start 【" + name + "】 grpc server fail: ", e);
        }
        return false;
    }


    /**
     * restart server
     */
    public boolean restart() {
        try {
            LOG.info("Restart 【" + name + "】 grpc server..........");
            if (null != server && RpcServerStatusEnum.RUNNING.equals(status)) {
                server.shutdown().awaitTermination(3, TimeUnit.SECONDS);
            }
            server = null;
            boolean ret = start();
            if (ret) {
                LOG.info("Restart 【{}】 grpc server success, tls enable: {}", name, tlsEnable);
            } else {
                LOG.error("Restart 【" + name + "】 grpc server fail.");
            }
            return ret;
        } catch (Exception e) {
            LOG.error("Restart 【" + name + "】 grpc server fail: ", e);
        }
        return false;
    }


    /**
     * Stop grpc service
     */
    protected void stop() {
        try {
            LOG.info("start shutting down 【" + name + "】 gRpc server.....");
            if (null != server && RpcServerStatusEnum.RUNNING.equals(status)) {
                server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            }
            status = RpcServerStatusEnum.SHUTDOWN;
            server = null;
            LOG.info("shutting down 【" + name + "】 gRpc server end.");
        } catch (Exception e) {
            LOG.error("shutting down 【" + name + "】 gRpc server exception:", e);
        }
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public GrpcServerScopeEnum getUseScope() {
        return useScope;
    }

    public void setUseScope(GrpcServerScopeEnum useScope) {
        this.useScope = useScope;
    }

    public boolean isTlsEnable() {
        return tlsEnable;
    }

    public void setTlsEnable(boolean tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    public RpcServerStatusEnum getStatus() {
        return status;
    }

    public void setStatus(RpcServerStatusEnum status) {
        this.status = status;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

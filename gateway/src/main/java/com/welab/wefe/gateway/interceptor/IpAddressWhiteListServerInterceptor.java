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

package com.welab.wefe.gateway.interceptor;

import com.welab.wefe.common.util.IpAddressUtil;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.SystemConfigCache;
import com.welab.wefe.gateway.service.MessageService;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server IP address whitelist interceptor
 *
 * @author aaron.li
 **/
public class IpAddressWhiteListServerInterceptor extends AbstractServerInterceptor {
    private final Logger LOG = LoggerFactory.getLogger(IpAddressWhiteListServerInterceptor.class);
    /**
     * Maximum concurrent number of flushed cache
     */
    private static final int MAX_REFRESH_CACHE_CONCURRENT_COUNT = 3;
    /**
     * Flush cache current concurrency
     */
    private static AtomicInteger CURRENT_CONCURRENT_COUNT = new AtomicInteger(0);

    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> intercept(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // Local IP address
        InetSocketAddress localSocketAddress = (InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR);
        // Remote IP address
        InetSocketAddress remoteSocketAddress = (InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);

        ServerCall.Listener<ReqT> nextListener = next.startCall(call, headers);
        // Intercept the request message before calling the target method
        ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> reqInterceptor =
                new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(nextListener) {
                    // Client IP
                    String remoteIpAddr = null;

                    @Override
                    public void onMessage(ReqT message) {
                        // Is the request invalid
                        boolean reqInvalid = false;
                        try {
                            remoteIpAddr = IpAddressUtil.getIpAddress(remoteSocketAddress);
                            GatewayMetaProto.TransferMeta transferMeta = (GatewayMetaProto.TransferMeta) message;
                            // It is equivalent to Ping and does not intercept the request
                            String ping = GatewayProcessorType.gatewayAliveProcessor.name();
                            if (!ping.equals(transferMeta.getProcessor()) && !isValidRemoteAddr(localSocketAddress, remoteSocketAddress)) {
                                LOG.info("IpAddressServerInterceptor called, localIpAddr: {}, remoteIpAddr: {}", IpAddressUtil.getIpAddress(localSocketAddress), remoteIpAddr);
                                LOG.info("The remote address is illegal. Close this connection");
                                reqInvalid = true;
                            }
                        } catch (Exception e) {
                            LOG.error("IpAddressWhiteListServerInterceptor called, verify IP whitelist exception：", e);
                            // Record exception message
                            MessageService messageService = GatewayServer.CONTEXT.getBean(MessageService.class);
                            messageService.saveError("IP白名单检查异常", "客户端IP：" + remoteIpAddr + " 不在白名单内，禁止访问");
                            reqInvalid = true;
                        }
                        // The request message is invalid
                        if (reqInvalid) {
                            // The setting request message is invalid
                            setReqInvalid(headers);
                            call.close(Status.PERMISSION_DENIED, headers);
                        } else {
                            super.onMessage(message);
                        }
                    }
                };
        return reqInterceptor;
    }


    /**
     * Is it a legal IP address
     *
     * @param localSocketAddress  Local IP address
     * @param remoteSocketAddress Remote IP address
     * @return true：yes; false：no
     */
    private boolean isValidRemoteAddr(InetSocketAddress localSocketAddress, InetSocketAddress remoteSocketAddress) {
        String localIpAddr = IpAddressUtil.getIpAddress(localSocketAddress);
        String remoteIpAddr = IpAddressUtil.getIpAddress(remoteSocketAddress);
        MessageService messageService = GatewayServer.CONTEXT.getBean(MessageService.class);

        SystemConfigCache cache = SystemConfigCache.getInstance();
        // If the IP white list is not set, the system will only allow clients in the same network segment as the server to access,
        // so as to prevent users from forgetting to set the white list
        if (cache.cacheIsEmpty()) {
            boolean isSameNetworkSegment = IpAddressUtil.isSameNetworkSegment(localSocketAddress, remoteSocketAddress);
            if (!isSameNetworkSegment) {
                LOG.error("isValidRemoteAddr, client ip：" + remoteIpAddr + " and server ip：" + localIpAddr + " different in one network segment, denied access!");
                messageService.saveError("白名单检查不通过", "客户端IP：" + remoteIpAddr + " 与服务端IP：" + localIpAddr + " 不在同一网段内，禁止访问");
            }

            return isSameNetworkSegment;
        }

        // If the client IP is the same as the server IP, access is allowed
        if (localIpAddr.equals(remoteIpAddr)) {
            return true;
        }

        // Is the client IP in the whitelist configuration
        boolean isExistWhiteListCache = cache.isExistIp(remoteIpAddr);
        // Avoid that the gateway starts first and the client starts later, and then the client actively notifies the gateway to refresh the white list configuration.
        // When the client is not in the white list cache, the gateway refreshes the cache again for inspection
        if (!isExistWhiteListCache && !isRefreshCacheAgain()) {
            return false;
        }

        isExistWhiteListCache = cache.isExistIp(remoteIpAddr);
        if (!isExistWhiteListCache) {
            LOG.error("client IP：" + remoteIpAddr + " not in the white list,denied access!");
            messageService.saveError("白名单检查不通过", "客户端IP：" + remoteIpAddr + " 不在白名单内，禁止访问");
        }
        return isExistWhiteListCache;
    }


    /**
     * Check whether the cache is refreshed again
     */
    private boolean isRefreshCacheAgain() {
        if (CURRENT_CONCURRENT_COUNT.get() <= MAX_REFRESH_CACHE_CONCURRENT_COUNT) {
            try {
                CURRENT_CONCURRENT_COUNT.getAndIncrement();
                SystemConfigCache.getInstance().refreshCache();
            } finally {
                CURRENT_CONCURRENT_COUNT.getAndDecrement();
            }
            return true;
        } else {
            LOG.info("The request to refresh the IP whitelist cache is too frequent. Please try again later.");
            return false;
        }
    }
}

/**
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
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.base.RpcServerAnnotate;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.service.MessageService;
import io.grpc.*;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Server interceptor basic class
 *
 * @author aaron.li
 **/
public abstract class AbstractServerInterceptor implements ServerInterceptor {
    /**
     * Exclude the method name to be intercepted, which is higher than the annotation configuration optimization level
     */
    private List<String> excludeInterceptMethods = new ArrayList<>();

    public AbstractServerInterceptor() {
    }

    public AbstractServerInterceptor(List<String> excludeInterceptMethods) {
        this.excludeInterceptMethods = excludeInterceptMethods;
    }


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // Target method description object
        MethodDescriptor methodDescriptor = call.getMethodDescriptor();
        // Full name of target class
        String serviceName = methodDescriptor.getServiceName();
        // Target method name
        String targetMethodName = methodDescriptor.getFullMethodName().split("/")[1];

        // If the verification of the non intercepting target or the previous interceptor is invalid,
        // go to the next step directly and skip the code judgment of the intercepting part
        if (!isInterceptTarget(serviceName, targetMethodName) || isReqInvalid(headers)) {
            return next.startCall(call, headers);
        }

        return intercept(call, headers, next);
    }


    /**
     * Subclass overrides the interception method
     */
    protected abstract <ReqT, RespT> ServerCall.Listener<ReqT> intercept(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next);


    /**
     * Is it an interception target
     *
     * @param serviceName      Target service name
     * @param targetMethodName Target method name
     * @return true, false
     */
    private boolean isInterceptTarget(String serviceName, String targetMethodName) {
        Map<String, RpcServerAnnotate> rpcConfigMap = RpcServerAnnotate.RPC_SERVER_MAP;
        RpcServerAnnotate rpcServerAnnotateConfig = rpcConfigMap.get(serviceName);
        if (null == rpcServerAnnotateConfig || CollectionUtils.isEmpty(rpcServerAnnotateConfig.getInterceptors())) {
            return false;
        }
        // If it is within the excluded method target, it is not intercepted
        if (!CollectionUtils.isEmpty(excludeInterceptMethods) && excludeInterceptMethods.contains(targetMethodName)) {
            return false;
        }

        // If the specific interception method name is not configured, it means that all methods under the target class need to be checked
        List<String> methods = rpcServerAnnotateConfig.getInterceptMethods();
        return CollectionUtils.isEmpty(methods) || methods.contains(targetMethodName);
    }

    /**
     * Save the signature verification failure message to the database
     */
    protected void saveSignVerifyFailInfo(String remoteIpAddr) {
        MessageService messageService = GatewayServer.CONTEXT.getBean(MessageService.class);
        messageService.saveError("客户端验签不通过，关闭此连接", "对来自客户端IP: " + remoteIpAddr + " 的签名验证失败，禁止访问");
    }

    /**
     * Get client IP address
     */
    protected <ReqT, RespT> String getClientIpAddr(ServerCall<ReqT, RespT> call) {
        return IpAddressUtil.getIpAddress((InetSocketAddress) call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));
    }

    /**
     * Is the request message invalid
     *
     * @param headers request headers
     * @return true：invalid, false：valid
     */
    protected boolean isReqInvalid(Metadata headers) {
        String isInvalid = headers.get(GrpcConstant.INTERCEPTOR_VERIFIED_REQ_INVALID_HEADER_KEY);
        return "true".equalsIgnoreCase(isInvalid);
    }

    /**
     * The identity message is invalid
     */
    protected void setReqInvalid(Metadata headers) {
        headers.put(GrpcConstant.INTERCEPTOR_VERIFIED_REQ_INVALID_HEADER_KEY, "true");
    }

    public List<String> getExcludeInterceptMethods() {
        return excludeInterceptMethods;
    }

    public void setExcludeInterceptMethods(List<String> excludeInterceptMethods) {
        this.excludeInterceptMethods = excludeInterceptMethods;
    }
}

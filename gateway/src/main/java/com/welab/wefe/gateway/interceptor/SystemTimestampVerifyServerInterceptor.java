/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.service.MessageService;
import io.grpc.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * System timestamp verification server interceptor
 *
 * @author aaron.li
 **/
public class SystemTimestampVerifyServerInterceptor extends AbstractServerInterceptor {
    private final Logger LOG = LoggerFactory.getLogger(SystemTimestampVerifyServerInterceptor.class);

    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> intercept(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // Timestamp of client submission
        String clientTimestampStr = headers.get(GrpcConstant.SYSTEM_TIMESTAMP_HEADER_KEY);

        ServerCall.Listener<ReqT> nextListener = next.startCall(call, headers);
        // Intercept the request message before calling the target method
        ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> reqInterceptor =
                new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(nextListener) {
                    String memberName = null;

                    @Override
                    public void onMessage(ReqT message) {
                        boolean reqInvalid = false;
                        try {
                            String errorMsg;
                            GatewayMetaProto.TransferMeta transferMeta = (GatewayMetaProto.TransferMeta) message;
                            memberName = transferMeta.getSrc().getMemberName();
                            if (StringUtil.isEmpty(clientTimestampStr)) {
                                errorMsg = "Member：" + memberName + " submitted system timestamp is empty. If the server uses nginx for forwarding, please make sure nginx option [underscores_in_headers] value is [on]";
                                GatewayServer.CONTEXT.getBean(MessageService.class).saveError("非法消息", errorMsg);
                                reqInvalid = true;
                            } else {
                                long clientTimestamp = NumberUtils.toLong(clientTimestampStr);
                                // If the system time difference between the client and the server exceeds the maximum value, access is prohibited
                                if ((System.currentTimeMillis() - clientTimestamp) > (GrpcConstant.MAX_SYSTEM_TIMESTAMP_DIFF * 1000L)) {
                                    LOG.error("SystemTimestampVerifyServerInterceptor called, client member name: " + memberName + " , system timestamp: " + clientTimestamp + " invalid.");
                                    errorMsg = "Member：" + memberName + ", timestamp：" + DateUtil.toString(new Date(clientTimestamp), DateUtil.YYYY_MM_DD_HH_MM_SS2)
                                            + ", with your machine system timestamp：" + DateUtil.toString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS2) + " difference exceeds：" + GrpcConstant.MAX_SYSTEM_TIMESTAMP_DIFF + " second, this message was rejected";
                                    GatewayServer.CONTEXT.getBean(MessageService.class).saveError("非法消息", errorMsg);
                                    reqInvalid = true;
                                }
                            }

                        } catch (Exception e) {
                            LOG.error("SystemTimestampVerifyServerInterceptor called, system timestamp verification exception：", e);
                            GatewayServer.CONTEXT.getBean(MessageService.class).saveError("非法消息", "验证系统时间截异常：" + e.getMessage());
                            reqInvalid = true;
                        }
                        if (reqInvalid) {
                            setReqInvalid(headers);
                            call.close(Status.FAILED_PRECONDITION, headers);
                        } else {
                            super.onMessage(message);
                        }
                    }
                };
        return reqInterceptor;
    }
}

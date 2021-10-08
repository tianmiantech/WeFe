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

package com.welab.wefe.gateway.util;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.interceptor.AntiTamperClientInterceptor;
import com.welab.wefe.gateway.interceptor.SignVerifyClientInterceptor;
import com.welab.wefe.gateway.interceptor.SystemTimestampVerifyClientInterceptor;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Grpc tool class
 *
 * @author aaron.li
 **/
public class GrpcUtil {
    private final static Logger LOG = LoggerFactory.getLogger(GrpcUtil.class);

    public static ManagedChannel getManagedChannel(BasicMetaProto.Endpoint endpoint) {
        return getManagedChannel(endpoint.getIp(), endpoint.getPort());
    }

    public static ManagedChannel getManagedChannel(String ip, int port) {
        return ManagedChannelBuilder.forTarget(ip + ":" + port).usePlaintext().build();
    }

    public static String toJsonString(MessageOrBuilder message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (Exception e) {
            e.getMessage();
        }

        return null;
    }

    /**
     * Check whether the connection is unavailable
     *
     * @param e Exception object returned by gateway
     * @return true：yes, false：no
     */
    public static boolean checkIsConnectionDisableExp(StatusRuntimeException e) {
        String errorMsg = e.getMessage();
        return StringUtil.isEmpty(errorMsg) || errorMsg.contains(GrpcConstant.CONNECTION_DISABLE_EXP_MSG);
    }

    /**
     * Check whether it is an IP whitelist exception
     *
     * @param e Exception object returned by gateway
     * @return true：yes, false：no
     */
    public static boolean checkIsIpPermissionExp(StatusRuntimeException e) {
        String errorMsg = e.getMessage();
        return StringUtil.isNotEmpty(errorMsg) && errorMsg.contains(GrpcConstant.IP_PERMISSION_EXP_MSG);
    }

    /**
     * Check whether it is a signature exception
     *
     * @param e Exception object returned by gateway
     * @return true：yes, false：no
     */
    public static boolean checkIsSignPermissionExp(StatusRuntimeException e) {
        String errorMsg = e.getMessage();
        return StringUtil.isNotEmpty(errorMsg) && errorMsg.contains(GrpcConstant.SIGN_PERMISSION_EXP_MSG);
    }

    /**
     * Check whether the time cut difference is too large exception
     *
     * @param e Exception object returned by gateway
     * @return true：yes, false：no
     */
    public static boolean checkSystemTimestampPermissionExp(StatusRuntimeException e) {
        String errorMsg = e.getMessage();
        return StringUtil.isNotEmpty(errorMsg) && errorMsg.contains(GrpcConstant.SYSTEM_TIMESTAMP_PERMISSION_EXP_MSG);
    }

    /**
     * Check Whether it is tamper proof exception (non streaming)
     *
     * @param e Exception object returned by gateway
     * @return true：yes, false：no
     */
    public static boolean checkAntiTamperExp(StatusRuntimeException e) {
        String errorMsg = e.getMessage();
        return StringUtil.isNotEmpty(errorMsg) && errorMsg.contains(GrpcConstant.INVALID_ARGUMENT_EXP_MSG);
    }

    /**
     * Check whether it is tamper proof exception（streaming）
     *
     * @param e Exception object returned by gateway
     * @return true：yes, false：no
     */
    public static boolean checkAntiTamperExp(ExecutionException e) {
        String errorMsg = e.getMessage();
        return StringUtil.isNotEmpty(errorMsg) && errorMsg.contains(GrpcConstant.INVALID_ARGUMENT_EXP_MSG);
    }

    /**
     * Protobuffer byte array of the message
     */
    public static byte[] getMessageProtobufferByte(GatewayMetaProto.TransferMeta transferMeta) {
        return (null == transferMeta) ? new byte[0] : transferMeta.toByteArray();
    }


    /**
     * Push message to remote end
     */
    public static BasicMetaProto.ReturnStatus pushToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member member = transferMeta.getDst();
        ManagedChannel originalChannel = null;
        BasicMetaProto.ReturnStatus returnStatus = null;
        long startTime = System.currentTimeMillis();
        try {
            Channel channel = null;
            NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceBlockingStub clientStub = null;
            // Failed retries count
            int failTryCount = 3;
            int sleepInterval = 1;
            for (int i = 1; i <= failTryCount; i++) {
                try {
                    // Binding generated signature, system time, tamper proof interceptor
                    originalChannel = GrpcUtil.getManagedChannel(member.getEndpoint());
                    channel = ClientInterceptors.intercept(originalChannel, new SystemTimestampVerifyClientInterceptor(), new SignVerifyClientInterceptor(), new AntiTamperClientInterceptor());
                    clientStub = NetworkDataTransferProxyServiceGrpc.newBlockingStub(channel);
                    return clientStub.push(transferMeta);
                } catch (StatusRuntimeException e) {
                    LOG.error("Message push failed, message info: " + GrpcUtil.toJsonString(transferMeta) + ",exception：", e);
                    GatewayMetaProto.Member dstMember = transferMeta.getDst();
                    String dstName = dstMember.getMemberName();
                    String endpoint = dstMember.getEndpoint().getIp() + ":" + dstMember.getEndpoint().getPort();
                    // At present, there is no IP whitelist restriction between gateways, so only check the signature and connectivity exceptions
                    if (GrpcUtil.checkIsSignPermissionExp(e)) {
                        // Sign in failed, return directly
                        returnStatus = ReturnStatusBuilder.sysExc("成员方[" + dstName + "]对您的签名验证不通过，请检查您的公私钥是否匹配以及公钥是否已上报", transferMeta.getSessionId());
                        return returnStatus;
                    }
                    if (GrpcUtil.checkIsConnectionDisableExp(e)) {
                        //The connection is unavailable. The address may have been updated. You need to refresh the destination address and try again
                        MemberEntity dstMemberEntity = MemberCache.getInstance().refreshCacheById(member.getMemberId());
                        if (null != dstMemberEntity) {
                            // Close the original channel
                            if (null != originalChannel) {
                                originalChannel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS);
                            }
                            // Reset destination member IP and port
                            member = member.toBuilder().setEndpoint(BasicMetaProto.Endpoint.newBuilder().setIp(dstMemberEntity.getIp()).setPort(dstMemberEntity.getPort()).build()).build();
                        } else {
                            LOG.error("Message push failed,re obtain destination address information is empty, dst member id is:" + member.getMemberId());
                        }

                        // Record the last error message
                        if (i >= failTryCount) {
                            returnStatus = ReturnStatusBuilder.sysExc("访问成员方[" + dstName + "]的网关[" + endpoint + "]不通，请检查网络连接是否正常以及对方网关是否已启动", transferMeta.getSessionId());
                            return returnStatus;
                        }
                    } else if (GrpcUtil.checkSystemTimestampPermissionExp(e)) {
                        // The system time between the two exceeds the allowable range
                        returnStatus = ReturnStatusBuilder.sysExc("成员方[" + dstName + "]与您的机器系统时间(" + DateUtil.toString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS2) + ")差超过 " + GrpcConstant.MAX_SYSTEM_TIMESTAMP_DIFF + " 秒，请对时后重试。", transferMeta.getSessionId());
                        return returnStatus;
                    } else if (GrpcUtil.checkAntiTamperExp(e)) {
                        // If the message is tampered with, try again
                        returnStatus = ReturnStatusBuilder.sysExc("成员方[" + dstName + "]验证您发送的消息已被篡改，对方拒绝接收", transferMeta.getSessionId());
                    } else {
                        returnStatus = ReturnStatusBuilder.sysExc("推送消息失败, 异常信息:" + e.getMessage(), transferMeta.getSessionId());
                    }
                } catch (Exception e) {
                    LOG.error("Message push failed, message info: " + GrpcUtil.toJsonString(transferMeta) + ", exception：", e);
                    returnStatus = ReturnStatusBuilder.sysExc("推送消息失败, 异常信息:" + e.getMessage(), transferMeta.getSessionId());
                } finally {
                    if (null != originalChannel) {
                        originalChannel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS);
                    }
                }
                ThreadUtil.sleep(sleepInterval * 1000);
                sleepInterval++;
            }
        } catch (Exception e) {
            LOG.error("Message push failed, message info: " + GrpcUtil.toJsonString(transferMeta) + ", exception：", e);
            returnStatus = ReturnStatusBuilder.sysExc("推送消息失败, 异常信息:" + e.getMessage(), transferMeta.getSessionId());
        }

        LOG.info("Transfer transferMeta duration, session id: {}, times: {}.", transferMeta.getSessionId(), (System.currentTimeMillis() - startTime));
        return returnStatus;
    }

}

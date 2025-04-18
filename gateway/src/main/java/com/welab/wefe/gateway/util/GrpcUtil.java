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

package com.welab.wefe.gateway.util;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import com.welab.wefe.gateway.cache.PartnerConfigCache;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.cache.GrpcChannelCache;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.interceptor.AntiTamperMetadataBuilder;
import com.welab.wefe.gateway.interceptor.ClientCallCredentials;
import com.welab.wefe.gateway.interceptor.SignVerifyMetadataBuilder;
import com.welab.wefe.gateway.interceptor.SystemTimestampMetadataBuilder;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

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
        return ManagedChannelBuilder.forTarget(ip + ":" + port).maxInboundMessageSize(Integer.MAX_VALUE).usePlaintext().build();
    }

    public static ManagedChannel getSslManagedChannel(BasicMetaProto.Endpoint endpoint, X509Certificate[] x509Certificates) throws SSLException {
        return getSslManagedChannel(endpoint.getIp(), endpoint.getPort(), x509Certificates);
    }

    public static ManagedChannel getSslManagedChannel(String ip, int port, X509Certificate[] x509Certificates) throws SSLException {
        boolean certificatesIsEmpty = (null == x509Certificates || x509Certificates.length == 0);
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();
        if (certificatesIsEmpty) {
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        } else {
            sslContextBuilder.trustManager(x509Certificates);
        }

        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(ip + ":" + port).negotiationType(NegotiationType.TLS)
                .sslContext(sslContextBuilder.build()).maxInboundMessageSize(Integer.MAX_VALUE);
        if (!certificatesIsEmpty) {
            builder.overrideAuthority("wefe.tianmiantech.com.test");
        }
        return builder.build();
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
     * 检查是否是ssl异常
     */
    public static boolean checkIsSslConnectionDisableExp(StatusRuntimeException e) {
        List<String> sslExceptionMarks = Arrays.asList("SSLHandshakeException", "unable to find valid certification");
        String stackTraceInfo = ExceptionUtil.getStackTraceInfo(e);
        if (StringUtil.isNotEmpty(stackTraceInfo) && stackTraceInfo.contains(GrpcConstant.CONNECTION_DISABLE_EXP_MSG)) {
            for (String sslExceptionMark : sslExceptionMarks) {
                if (stackTraceInfo.contains(sslExceptionMark)) {
                    return true;
                }
            }
        }
        return false;
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
     * Protobuffer byte array of the message
     */
    public static byte[] getMessageProtobufferByte(GatewayMetaProto.TransferMeta transferMeta) {
        return (null == transferMeta) ? new byte[0] : transferMeta.toByteArray();
    }


    /**
     * Push message to remote end
     */
    public static BasicMetaProto.ReturnStatus pushToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        ManagedChannel originalChannel = null;
        BasicMetaProto.ReturnStatus returnStatus = null;
        GrpcChannelCache channelCache = GrpcChannelCache.getInstance();
        String dstGatewayUri = EndpointBuilder.endpointToUri(dstMember.getEndpoint());
        long startTime = System.currentTimeMillis();
        try {
            boolean tlsEnable = checkTlsEnable(transferMeta);
            // Failed retries count
            int failTryCount = 5;
            for (int i = 0; i <= failTryCount; i++) {
                try {
                    originalChannel = channelCache.getNonNull(dstGatewayUri, tlsEnable, TlsUtil.getAllCertificates(tlsEnable));
                    NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceBlockingStub clientStub = NetworkDataTransferProxyServiceGrpc.newBlockingStub(originalChannel)
                            .withCallCredentials(new ClientCallCredentials(transferMeta,
                                    new AntiTamperMetadataBuilder(transferMeta),
                                    new SignVerifyMetadataBuilder(transferMeta),
                                    new SystemTimestampMetadataBuilder(transferMeta)));

                    return clientStub.push(transferMeta);
                } catch (StatusRuntimeException e) {
                    LOG.error("Message push failed, message info: " + GrpcUtil.toJsonString(transferMeta) + ",exception：" + e.getMessage(), e);
                    String dstName = dstMember.getMemberName();
                    String endpoint = EndpointBuilder.generateUri(dstMember.getEndpoint());
                    // At present, there is no IP whitelist restriction between gateways, so only check the signature and connectivity exceptions
                    if (GrpcUtil.checkIsSignPermissionExp(e)) {
                        // Sign in failed, return directly
                        return ReturnStatusBuilder.sysExc("成员方[" + dstName + "]对您的签名验证不通过;　存在以下可能性：1、请检查您的公私钥是否匹配以及公钥是否已上报到 union. 2、请确认双方机器系统时间差是否超过5分钟. 3、如果对方的网关使用了nginx做负载转发,请确认对方的nginx配置项[underscores_in_headers]的值是否为[on]", transferMeta.getSessionId());
                    }
                    if (GrpcUtil.checkIsSslConnectionDisableExp(e)) {
                        return ReturnStatusBuilder.sysExc("访问成员方[" + dstName + "]的网关[" + endpoint + "]不通, 其网关启用了SSL通道,请确认CA证书的有效性.", transferMeta.getSessionId());
                    }
                    if (GrpcUtil.checkIsConnectionDisableExp(e)) {
                        //The connection is unavailable. The address may have been updated. You need to refresh the destination address and try again
                        // 用户没指定具体的专有网络地址（因为如果指定的专有网络则不能使用公开的地址，则没必要刷新）
                        if (null == PartnerConfigCache.getInstance().get(dstMember.getMemberId())) {
                            MemberEntity dstMemberEntity = MemberCache.getInstance().refreshCacheById(dstMember.getMemberId());
                            if (null != dstMemberEntity) {
                                channelCache.remove(dstGatewayUri);
                                // Reset destination member IP and port
                                dstMember = dstMember.toBuilder().setEndpoint(EndpointBuilder.create(dstMemberEntity.getGatewayExternalUri())).build();

                            } else {
                                LOG.error("Message push failed,re obtain destination address information is empty, dst member id is:" + dstMember.getMemberId());
                            }

                            // Record the last error message
                            if (i >= failTryCount) {
                                return ReturnStatusBuilder.sysExc("访问成员方[" + dstName + "]的网关[" + endpoint + "]不通，请检查网络连接是否正常以及对方网关是否已启动" + (tlsEnable ? "(PS:该网关启用了SSL通道)" : ""), transferMeta.getSessionId());
                            }
                        }
                    } else if (GrpcUtil.checkSystemTimestampPermissionExp(e)) {
                        // The system time between the two exceeds the allowable range
                        return ReturnStatusBuilder.sysExc("成员方[" + dstName + "]与您的机器系统时间(" + DateUtil.toString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS2) + ")差超过 " + GrpcConstant.MAX_SYSTEM_TIMESTAMP_DIFF + " 秒，请对时后重试。", transferMeta.getSessionId());
                    } else if (GrpcUtil.checkAntiTamperExp(e)) {
                        // If the message is tampered with, try again
                        return ReturnStatusBuilder.sysExc("成员方[" + dstName + "]验证您发送的消息已被篡改，对方拒绝接收", transferMeta.getSessionId());
                    } else {
                        returnStatus = ReturnStatusBuilder.sysExc("推送消息失败, 异常信息:" + e.getMessage(), transferMeta.getSessionId());
                    }
                } catch (Exception e) {
                    LOG.error("Message push failed, message info: " + GrpcUtil.toJsonString(transferMeta) + ", exception：" + e.getMessage(), e);
                    returnStatus = ReturnStatusBuilder.sysExc("推送消息失败, 异常信息:" + e.getMessage(), transferMeta.getSessionId());
                }
                ThreadUtil.sleep(i * 500L);
            }
        } catch (Exception e) {
            LOG.error("Message push failed, message info: " + GrpcUtil.toJsonString(transferMeta) + ", exception：" + e.getMessage(), e);
            returnStatus = ReturnStatusBuilder.sysExc("推送消息失败, 异常信息:" + e.getMessage(), transferMeta.getSessionId());
        }

        LOG.info("Transfer transferMeta duration, session id: {}, times: {}.", transferMeta.getSessionId(), (System.currentTimeMillis() - startTime));
        return returnStatus;
    }

    /**
     * Check whether the gateway address format is valid
     */
    public static boolean checkGatewayUriValid(String gatewayUri) {
        String separator = ":";
        if (StringUtil.isEmpty(gatewayUri) || gatewayUri.split(separator).length != 2
                || StringUtil.isEmpty(gatewayUri.split(separator)[0]) || !NumberUtils.isDigits(gatewayUri.split(separator)[1])) {
            return false;
        }
        return true;
    }

    /**
     * 关闭通道
     */
    public static void closeManagedChannel(ManagedChannel channel) {
        if (null == channel) {
            return;
        }
        try {
            channel.shutdownNow().awaitTermination(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("关闭Grpc channel异常：", e);
        }
    }

    /**
     * 判断目的地址是否需启用TLS
     */
    public static boolean checkTlsEnable(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        MemberEntity dstMemberEntity = MemberCache.getInstance().get(dstMember.getMemberId());
        boolean tlsEnable = dstMemberEntity.tlsEnable();
        // 目的地址与自身内网IP地址相同,则证明接收方也是自身且使用了内网,连路直接走内网即可,而内网开启的是非tls服务（此处不能直接拿dst member id对比来判断,因为调用方可以指定dst的地址）
        MemberEntity selfMemberEntity = MemberCache.getInstance().getSelfMember();
        String dstGatewayUri = EndpointBuilder.endpointToUri(dstMember.getEndpoint());
        return !dstGatewayUri.equals(selfMemberEntity.getGatewayInternalUri()) && tlsEnable;
    }

}

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

package com.welab.wefe.gateway.interceptor;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.service.MessageService;
import com.welab.wefe.gateway.util.GrpcUtil;
import io.grpc.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Server message tamper proof verification interceptor
 *
 * @author aaron.li
 **/
public class AntiTamperServerInterceptor extends AbstractServerInterceptor {
    private final Logger LOG = LoggerFactory.getLogger(SystemTimestampVerifyServerInterceptor.class);
    /**
     * Target method for excluding interception.
     * Due to the special reasons of streaming mode, the data does not arrive in the sending order, and tamper prevention cannot be realized temporarily.
     * Therefore, the interception of streaming interface is excluded
     */
    private final static List<String> EXCLUDE_INTERCEPT_METHODS = Arrays.asList("pushData");

    public AntiTamperServerInterceptor() {
        super(EXCLUDE_INTERCEPT_METHODS);
    }

    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> intercept(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // Remote IP address
        String remoteIpAddr = getClientIpAddr(call);
        // Signature information submitted by the client
        String reqDataHashSign = headers.get(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY);
        // Signature verification failed
        if (!signVerify(reqDataHashSign)) {
            LOG.error("AntiTamperServerInterceptor called, Client IP: {}, sign info: {}, sign verify fail，close the connection.", remoteIpAddr, reqDataHashSign);
            // Save error message
            saveSignVerifyFailInfo(remoteIpAddr);

            call.close(Status.UNAUTHENTICATED, headers);
            // The setting request message is invalid
            setReqInvalid(headers);
            return next.startCall(call, headers);
        }

        ServerCall.Listener<ReqT> nextListener = next.startCall(call, headers);
        // Intercept the request message before calling the target method
        ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> reqInterceptor =
                new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(nextListener) {
                    @Override
                    public void onMessage(ReqT message) {
                        boolean reqInvalid = false;
                        try {
                            JObject dataObj = JObject.create(JObject.create(reqDataHashSign).getString(GrpcConstant.SIGN_KEY_DATA));
                            // Original message MD5 value
                            String origMessageMd5 = dataObj.getString(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY.name());
                            long startTime = System.currentTimeMillis();
                            byte[] messageByteArray = GrpcUtil.getMessageProtobufferByte((GatewayMetaProto.TransferMeta) message);
                            int sizeKB = BigDecimal.valueOf(messageByteArray.length)
                                    .divide(BigDecimal.valueOf(1024), 0, RoundingMode.HALF_UP)
                                    .intValue();
                            // MD5 value of received message
                            String messageMd5 = DigestUtils.md5Hex(messageByteArray);
                            LOG.info("AntiTamperGenerateServerInterceptor called, message size：{} KB, convert to MD5: {}, time spend：{} millisecond.", sizeKB, messageMd5, (System.currentTimeMillis() - startTime));
                            // Unequal, proving that the message has been tampered with
                            if (!origMessageMd5.equals(messageMd5)) {
                                LOG.error("AntiTamperGenerateServerInterceptor called, verify that the message has been tampered.The MD5 value of the original message is：" + origMessageMd5 + ", the received message MD5 value is: " + messageMd5);
                                reqInvalid = true;
                                GatewayServer.CONTEXT.getBean(MessageService.class).saveError("消息已被篡改", "收到客户端：" + remoteIpAddr + " 提交过来的消息已被篡改，拒绝接收");
                            }
                        } catch (Exception e) {
                            LOG.error("AntiTamperGenerateServerInterceptor called,  verifying message tamper exception：", e);
                            GatewayServer.CONTEXT.getBean(MessageService.class).saveError("消息已被篡改", "验证客户端：" + remoteIpAddr + " 提交过来的消息是否被篡改时异常，拒绝接收");
                            reqInvalid = true;
                        }

                        // The request is invalid
                        if (reqInvalid) {
                            setReqInvalid(headers);
                            call.close(Status.INVALID_ARGUMENT, headers);
                        } else {
                            super.onMessage(message);
                        }
                    }
                };

        return reqInterceptor;
    }


    /**
     * Signature verification
     */
    private boolean signVerify(String signInfo) {
        if (StringUtil.isEmpty(signInfo)) {
            LOG.error("No signature information was received from the client. Signature verification failed. If the server uses nginx for forwarding, please make sure nginx option [underscores_in_headers] value is [on]");
            return false;
        }

        JObject signInfoObj = null;
        JObject dataObj = null;
        String sign = null;
        try {
            signInfoObj = JObject.create(signInfo);
            dataObj = JObject.create(signInfoObj.getString(GrpcConstant.SIGN_KEY_DATA));
            sign = signInfoObj.getString(GrpcConstant.SIGN_KEY_SIGN);
        } catch (Exception e) {
            LOG.error("The client's signature information conversion JSON exception, signature verification failed：", e);
            return false;
        }

        if (null == dataObj || dataObj.isEmpty() || StringUtil.isEmpty(sign)) {
            LOG.error("The signature field of the client's signature information is invalid. Signature verification failed");
            return false;
        }

        String memberId = dataObj.getString(GrpcConstant.SIGN_KEY_MEMBER_ID);
        String messageMd5 = dataObj.getString(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY.name());
        if (StringUtil.isEmpty(memberId) || StringUtil.isEmpty(messageMd5)) {
            LOG.error("The signature field of the client's signature information is invalid. Signature verification failed");
            return false;
        }

        MemberEntity memberEntity = MemberCache.getInstance().get(memberId);
        if (null == memberEntity) {
            LOG.error("Invalid member ID, signature verification failed");
            return false;
        }

        try {
            return SignUtil.verify(signInfoObj.getString(GrpcConstant.SIGN_KEY_DATA).getBytes(StandardCharsets.UTF_8.toString()), memberEntity.getPublicKey(), sign, memberEntity.getSecretKeyType());
        } catch (Exception e) {
            LOG.error("Signature verification exception：", e);
        }
        return false;
    }
}

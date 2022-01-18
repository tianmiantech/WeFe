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

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.SignUtil;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * Client tamper proof message interceptor
 *
 * @author aaron.li
 **/
public class AntiTamperClientInterceptor implements ClientInterceptor {
    private final Logger LOG = LoggerFactory.getLogger(AntiTamperClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new CustomClientCall<>(next.newCall(method, callOptions));
    }

    /**
     * Define client call object
     */
    public class CustomClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {
        private Metadata headers;

        protected CustomClientCall(ClientCall<ReqT, RespT> delegate) {
            super(delegate);
        }

        @Override
        public void start(Listener responseListener, Metadata headers) {
            this.headers = headers;
            super.start(responseListener, headers);
        }


        @Override
        public void sendMessage(ReqT message) {
            try {
                long startTime = System.currentTimeMillis();

                // Get message byte size
                byte[] messageByteArray = GrpcUtil.getMessageProtobufferByte((GatewayMetaProto.TransferMeta) message);
                long spentTime = System.currentTimeMillis() - startTime;
                // Convert to KB measurement
                int sizeKB = BigDecimal.valueOf(messageByteArray.length)
                        .divide(BigDecimal.valueOf(1024), 0, RoundingMode.HALF_UP)
                        .intValue();
                // MD5
                startTime = System.currentTimeMillis();
                String messageMd5 = DigestUtils.md5Hex(messageByteArray);
                LOG.info("AntiTamperClientInterceptor　called, message intercepted, Size：{} KB, calculate message byte size time consuming：{} millisecond, Byte to MD5: {} time consuming：{} millisecond", sizeKB, spentTime, messageMd5, (System.currentTimeMillis() - startTime));
                // 签名参数
                this.headers.put(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY, generateSign(messageMd5));
            } catch (Exception e) {
                LOG.error("Check message tamper proof exception：", e);

                MessageService messageService = GatewayServer.CONTEXT.getBean(MessageService.class);
                messageService.saveError("检查消息防篡改失败", "原因：" + e.getMessage());
            }
            super.sendMessage(message);
        }


    }


    /**
     * Generate signature
     */
    private String generateSign(String messageMd5) throws Exception {
        MemberEntity memberEntity = MemberCache.getInstance().getSelfMember();
        // Signature parameters
        Map<String, String> signParam = new TreeMap<>();
        signParam.put(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY.name(), messageMd5);
        signParam.put(GrpcConstant.SIGN_KEY_MEMBER_ID, MemberCache.getInstance().getSelfMember().getId());
        String signParamStr = JObject.create(signParam).toString();

        try {
            return JObject.create()
                    .append(GrpcConstant.SIGN_KEY_SIGN, SignUtil.sign(signParamStr, memberEntity.getPrivateKey(), memberEntity.getSecretKeyType()))
                    .append(GrpcConstant.SIGN_KEY_DATA, signParamStr).toString();
        } catch (Exception e) {
            LOG.error("Failed to generate signature for message MD5：", e);
            throw e;
        }

    }

}

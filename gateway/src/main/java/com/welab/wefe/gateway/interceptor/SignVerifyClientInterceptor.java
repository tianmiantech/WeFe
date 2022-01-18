/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.interceptor;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.entity.MemberEntity;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Signature verification client interceptor
 *
 * @author aaron.li
 **/
public class SignVerifyClientInterceptor implements ClientInterceptor {
    private final Logger LOG = LoggerFactory.getLogger(SignVerifyClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(GrpcConstant.SIGN_HEADER_KEY, generateSign());
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }


    /**
     * Generate signature
     */
    private String generateSign() {
        // Generate a signature using its own member private key
        MemberEntity memberEntity = MemberCache.getInstance().getSelfMember();
        String memberId = memberEntity.getId();
        String privateKey = memberEntity.getPrivateKey();

        // Signature parameters
        Map<String, String> signParam = new TreeMap<>();
        signParam.put(GrpcConstant.SIGN_KEY_MEMBER_ID, memberId);
        signParam.put(GrpcConstant.SIGN_KEY_TIMESTAMP, System.currentTimeMillis() + "");
        signParam.put(GrpcConstant.SIGN_KEY_UUID, UUID.randomUUID().toString());

        String signParamStr = JObject.create(signParam).toString();
        SecretKeyType secretKeyType = getSecretKeyType();
        try {
            return JObject.create()
                    .append(GrpcConstant.SIGN_KEY_SIGN, SignUtil.sign(signParamStr, privateKey, secretKeyType))
                    .append(GrpcConstant.SIGN_KEY_DATA, signParamStr).toString();
        } catch (Exception e) {
            LOG.error("Failed to generate signature：", e);
        }

        return "";
    }

    private SecretKeyType getSecretKeyType() {
        MemberEntity memberEntity = MemberCache.getInstance().getSelfMember();
        return memberEntity.getSecretKeyType();
    }
}

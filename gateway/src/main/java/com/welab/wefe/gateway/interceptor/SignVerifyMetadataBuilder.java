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

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.entity.MemberEntity;
import io.grpc.Metadata;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class SignVerifyMetadataBuilder extends AbstractMetadataBuilder {
    public SignVerifyMetadataBuilder(GatewayMetaProto.TransferMeta transferMeta) {
        super(transferMeta);
    }

    @Override
    public Metadata build() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstant.SIGN_HEADER_KEY, generateSign());
        return metadata;
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
        SecretKeyType secretKeyType = memberEntity.getSecretKeyType();
        try {
            return JObject.create()
                    .append(GrpcConstant.SIGN_KEY_SIGN, SignUtil.sign(signParamStr, privateKey, secretKeyType))
                    .append(GrpcConstant.SIGN_KEY_DATA, signParamStr).toString();
        } catch (Exception e) {
            LOG.error("Failed to generate signature：", e);
        }

        return "";
    }


}

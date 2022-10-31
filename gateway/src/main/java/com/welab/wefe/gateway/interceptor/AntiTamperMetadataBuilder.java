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
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.service.MessageService;
import com.welab.wefe.gateway.util.GrpcUtil;
import io.grpc.Metadata;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

public class AntiTamperMetadataBuilder extends AbstractMetadataBuilder {
    public AntiTamperMetadataBuilder(GatewayMetaProto.TransferMeta transferMeta) {
        super(transferMeta);
    }

    @Override
    public Metadata build() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY, generateSign(this.getTransferMeta()));
        return metadata;
    }

    /**
     * Generate signature
     */
    private String generateSign(GatewayMetaProto.TransferMeta transferMeta) {

        MemberEntity memberEntity = MemberCache.getInstance().getSelfMember();
        // Signature parameters
        Map<String, String> signParam = new TreeMap<>();
        signParam.put(GrpcConstant.REQ_DATA_HASH_SIGN_HEADER_KEY.name(), generateMessageMd5(transferMeta));
        signParam.put(GrpcConstant.SIGN_KEY_MEMBER_ID, MemberCache.getInstance().getSelfMember().getId());
        String signParamStr = JObject.create(signParam).toString();

        try {
            return JObject.create()
                    .append(GrpcConstant.SIGN_KEY_SIGN, SignUtil.sign(signParamStr, memberEntity.getPrivateKey(), memberEntity.getSecretKeyType()))
                    .append(GrpcConstant.SIGN_KEY_DATA, signParamStr).toString();
        } catch (Exception e) {
            LOG.error("Failed to generate signature for message MD5：", e);
        }
        return "";
    }

    private String generateMessageMd5(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            long startTime = System.currentTimeMillis();

            // Get message byte size
            byte[] messageByteArray = GrpcUtil.getMessageProtobufferByte(transferMeta);
            long spentTime = System.currentTimeMillis() - startTime;
            // Convert to KB measurement
            int sizeKB = BigDecimal.valueOf(messageByteArray.length)
                    .divide(BigDecimal.valueOf(1024), 0, RoundingMode.HALF_UP)
                    .intValue();
            // MD5
            startTime = System.currentTimeMillis();
            String messageMd5 = DigestUtils.md5Hex(messageByteArray);
            LOG.info("AntiTamperClientInterceptor　called, message intercepted, Size：{} KB, calculate message byte size time consuming：{} millisecond, Byte to MD5: {} time consuming：{} millisecond", sizeKB, spentTime, messageMd5, (System.currentTimeMillis() - startTime));
            return messageMd5;

        } catch (Exception e) {
            LOG.error("生成消息MD5值失败：", e);
            MessageService messageService = GatewayServer.CONTEXT.getBean(MessageService.class);
            messageService.saveError("生成消息MD5值失败", "原因：" + e.getMessage());
        }
        return null;
    }


}

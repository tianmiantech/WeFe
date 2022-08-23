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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.AsymmetricCryptoUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.dto.BoardConfigModel;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.sdk.BoardHelper;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Push to the board module message processor in HTTP mode
 *
 * @author aaron.li
 **/
@Processor(type = GatewayProcessorType.boardHttpProcessor, desc = "push to the board module message processor in HTTP mode")
public class BoardHttpProcessor extends AbstractProcessor {
    private final static String ENCRYPT_MARK_PREFIX = "ENCRYPT:";
    private final static String NON_ENCRYPT_MARK_PREFIX = "NON_ENCRYPT:";

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private ConfigProperties config;

    @Override
    public BasicMetaProto.ReturnStatus beforeSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            transferMeta = encryptTransferMeta(transferMeta);
        } catch (Exception e) {
            LOG.error("BoardHttpProcessor encrypt transferMeta exception: ", e);
            return ReturnStatusBuilder.sysExc("加密数据异常：" + e.getMessage(), transferMeta.getSessionId());
        }

        return toRemote(transferMeta);
    }

    @Override
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            String data = transferMeta.getContent().getObjectData();
            if (StringUtil.isNotEmpty(data)) {
                data = data.startsWith(ENCRYPT_MARK_PREFIX) ? decryptContent(data.substring(ENCRYPT_MARK_PREFIX.length())) : data.substring(NON_ENCRYPT_MARK_PREFIX.length());
            }
            JObject contentJson = JObject.create(data);
            String url = contentJson.getString("url");
            String method = contentJson.getString("method");
            String body = contentJson.getString("body");
            Map<String, String> headers = new HashMap<>(16);
            headers.put("srcMemberId", transferMeta.getSrc().getMemberId());
            BoardConfigModel boardConfig = globalConfigService.getBoardConfig();
            if (boardConfig == null || StringUtil.isEmpty(boardConfig.intranetBaseUri)) {
                LOG.error("The intranet communication address of board service is not set");
                return ReturnStatusBuilder.sysExc("board内网通信地址未设置", transferMeta.getSessionId());
            }

            String boardBaseUrl = boardConfig.intranetBaseUri;
            boardBaseUrl = (boardBaseUrl.endsWith("/") ? boardBaseUrl : (boardBaseUrl + "/"));
            String fullUrl = boardBaseUrl + url;
            LOG.info("Gateway access board address：" + fullUrl);
            HttpResponse response = BoardHelper.push(fullUrl, method, headers, BoardHelper.generateReqParam(body));
            if (response.success()) {
                return ReturnStatusBuilder.ok(transferMeta.getSessionId(), response.getBodyAsString());
            } else {
                String errorMsg = "请求Board地址【" + url + "】失败，Http code: " + response.getCode() + ", errorMsg: " + response.getError().getMessage();
                return ReturnStatusBuilder.sysExc(errorMsg, transferMeta.getSessionId());
            }
        } catch (Exception e) {
            LOG.error("BoardHttpProcessor fail, exception:", e);
            return ReturnStatusBuilder.sysExc(e.getMessage(), transferMeta.getSessionId());
        }
    }

    /**
     * Encrypt content
     */
    private GatewayMetaProto.TransferMeta encryptTransferMeta(GatewayMetaProto.TransferMeta transferMeta) throws Exception {
        String body = transferMeta.getContent().getObjectData();
        if (StringUtil.isEmpty(body)) {
            return transferMeta;
        }
        MemberEntity dstMember = MemberCache.getInstance().get(transferMeta.getDst().getMemberId());
        GatewayMetaProto.Content.Builder contentBuilder = transferMeta.getContent().toBuilder();
        if (dstMember.getGatewayTlsEnable()) {
            return transferMeta.toBuilder().setContent(contentBuilder.setObjectData(NON_ENCRYPT_MARK_PREFIX + body).build()).build();
        }

        String encryptBody = AsymmetricCryptoUtil.encryptByPublicKey(body, dstMember.getPublicKey(), dstMember.getSecretKeyType());
        GatewayMetaProto.Content content = contentBuilder.setObjectData(ENCRYPT_MARK_PREFIX + encryptBody).build();

        return transferMeta.toBuilder().setContent(content).build();
    }

    /**
     * Decrypt content
     */
    private String decryptContent(String cipherContent) throws Exception {
        if (StringUtil.isEmpty(cipherContent)) {
            return cipherContent;
        }
        MemberEntity selfMember = MemberCache.getInstance().getSelfMember();
        return AsymmetricCryptoUtil.decryptByPrivateKey(cipherContent, selfMember.getPrivateKey(), selfMember.getSecretKeyType());
    }
}

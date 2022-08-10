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

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.*;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.common.ReturnStatusEnum;
import com.welab.wefe.gateway.dto.BoardConfigModel;
import com.welab.wefe.gateway.sdk.BoardHelper;
import com.welab.wefe.gateway.service.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Push to the board module message processor in HTTP mode
 *
 * @author aaron.li
 **/
@Processor(type = GatewayProcessorType.boardHttpProcessor, desc = "push to the board module message processor in HTTP mode")
public class BoardHttpProcessor extends AbstractProcessor {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    public BasicMetaProto.ReturnStatus beforeSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {


        return super.beforeSendToRemote(transferMeta);
    }

    @Override
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            JObject contentJson = JObject.create(transferMeta.getContent().getObjectData());
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

    public static void main(String[] args) throws Exception {
        RSAUtil.RsaKeyPair rsaKeyPair = RSAUtil.generateKeyPair();
        String data = "aaaa{}fasdfasdfasfasfasdfasdfasdfasdfasdf";
        String encryptData = RSAUtil.encryptByPublicKey(data, rsaKeyPair.publicKey);
        System.out.println("encryptData=" + encryptData);
        String decryptData = RSAUtil.decryptByPrivateKey(encryptData, rsaKeyPair.privateKey);
        System.out.println("decryptData=" + decryptData);

        SM2Util.Sm2KeyPair sm2KeyPair = SM2Util.generateKeyPair();

        SM2 sm2 = SmUtil.sm2(Base64Util.decode(sm2KeyPair.privateKey).getBytes(StandardCharsets.UTF_8), Base64Util.decode(sm2KeyPair.publicKey).getBytes(StandardCharsets.UTF_8));
        // 公钥加密，私钥解密
        String encryptStr = sm2.encryptBcd("aa", KeyType.PublicKey);
        System.out.println("sm2=" + encryptStr);
    }
}

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

package com.welab.wefe.gateway.service;

import java.util.ArrayList;
import java.util.List;

import com.welab.wefe.common.wefe.dto.global_config.GatewayConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.storage.ClickHouseStorageConfigModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.dto.global_config.MemberInfoModel;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.sdk.UnionHelper;
import com.welab.wefe.gateway.service.base.AbstractMemberService;

/**
 * @author aaron.li
 **/
@Service
public class MemberService extends AbstractMemberService {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    public List<MemberEntity> find(String memberId) throws Exception {
        JSONArray jsonArray = JObject.parseArray(UnionHelper.getMembers(memberId));
        List<MemberEntity> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(jsonArray)) {
            return resultList;
        }
        MemberEntity member = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            member = new MemberEntity();
            member.setGatewayExternalUri(obj.getString("gateway_uri"));
            member.setName(obj.getString("name"));
            member.setId(obj.getString("id"));
            member.setPublicKey(obj.getString("public_key"));

            JSONObject extJsonObj = obj.getJSONObject("ext_json");
            if (null != extJsonObj && !extJsonObj.isEmpty()) {
                String secretKeyType = extJsonObj.getString("secret_key_type");
                member.setSecretKeyType(SecretKeyType.get(secretKeyType));
                member.setGatewayTlsEnable(extJsonObj.getBooleanValue("member_gateway_tls_enable"));
            }

            resultList.add(member);
        }
        return resultList;
    }

    /**
     * Query own member information
     */
    @Override
    public MemberEntity findSelf() {
        MemberInfoModel memberInfo = globalConfigService.getModel(MemberInfoModel.class);
        if (null == memberInfo) {
            return null;
        }
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberInfo.getMemberId());
        memberEntity.setName(memberInfo.getMemberName());
        memberEntity.setPrivateKey(memberInfo.getRsaPrivateKey());
        memberEntity.setPublicKey(memberInfo.getRsaPublicKey());
        memberEntity.setGatewayExternalUri(memberInfo.getMemberGatewayUri());
        GatewayConfigModel gatewayConfigModel = globalConfigService.getModel(GatewayConfigModel.class);
        if (null != gatewayConfigModel) {
            memberEntity.setGatewayInternalUri(gatewayConfigModel.intranetBaseUri);
        }
        memberEntity.setSecretKeyType(null == memberInfo.getSecretKeyType() ? SecretKeyType.rsa : memberInfo.getSecretKeyType());
        memberEntity.setGatewayTlsEnable(memberInfo.getMemberGatewayTlsEnable());
        return memberEntity;
    }

    public boolean getMemberGatewayTlsEnable() {
        GlobalConfigService globalConfigService = GatewayServer.CONTEXT.getBean(GlobalConfigService.class);
        MemberInfoModel memberInfoModel = globalConfigService.getModel(MemberInfoModel.class);
        return memberInfoModel.getMemberGatewayTlsEnable() == null ? Boolean.FALSE
                : memberInfoModel.getMemberGatewayTlsEnable();
    }
}

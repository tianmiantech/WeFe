/**
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.dto.MemberInfoModel;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.sdk.UnionHelper;
import com.welab.wefe.gateway.service.base.AbstractMemberService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            String gatewayUri = obj.getString("gateway_uri");
            member = new MemberEntity();
            if (StringUtil.isNotEmpty(gatewayUri) && gatewayUri.split(":").length == 2) {
                member.setIp(gatewayUri.split(":")[0]);
                member.setPort(NumberUtils.toInt(gatewayUri.split(":")[1]));
            }
            member.setName(obj.getString("name"));
            member.setId(obj.getString("id"));
            member.setPublicKey(obj.getString("public_key"));

            JSONObject extJsonObj = obj.getJSONObject("ext_json");
            if (null != extJsonObj && !extJsonObj.isEmpty()) {
                String secretKeyType = extJsonObj.getString("secret_key_type");
                member.setSecretKeyType(SecretKeyType.get(secretKeyType));
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
        MemberInfoModel memberInfo = globalConfigService.getMemberInfo();
        if (null == memberInfo) {
            return null;
        }
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(memberInfo.getMemberId());
        memberEntity.setName(memberInfo.getMemberName());
        memberEntity.setPrivateKey(memberInfo.getRsaPrivateKey());
        memberEntity.setPublicKey(memberInfo.getRsaPublicKey());
        String memberGatewayUri = memberInfo.getMemberGatewayUri();
        if (StringUtil.isNotEmpty(memberGatewayUri)) {
            String[] memberGatewayUriArray = memberGatewayUri.split(":");
            if (memberGatewayUriArray.length == 2) {
                memberEntity.setIp(memberGatewayUriArray[0]);
                memberEntity.setPort(NumberUtils.toInt(memberGatewayUriArray[1]));
            }
        }
        memberEntity.setSecretKeyType(null == memberInfo.getSecretKeyType() ? SecretKeyType.rsa : memberInfo.getSecretKeyType());
        return memberEntity;
    }
}

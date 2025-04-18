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

package com.welab.wefe.serving.service.dto.globalconfig;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.serving.service.dto.globalconfig.base.AbstractConfigModel;
import com.welab.wefe.serving.service.dto.globalconfig.base.ConfigGroupConstant;
import com.welab.wefe.serving.service.dto.globalconfig.base.ConfigModel;

/**
 * @author hunter.zhao
 */
@ConfigModel(group = ConfigGroupConstant.IDENTITY_INFO)
public class IdentityInfoModel extends AbstractConfigModel {

    @Check(name = "系统 id", desc = "全局唯一，独立模式默认为uuid，联邦模式为memberId。")
    private String memberId;
    @Check(name = "名称")
    private String memberName;
    @Check(name = "邮箱")
    private String email;
    @Check(name = "系统域路径")
    private String servingBaseUrl;
    @Check(name = "头像")
    private String avatar;

    @Check(name = "私钥")
    @Secret(maskStrategy = MaskStrategy.BLOCK)
    private String rsaPrivateKey;
    @Check(name = "公钥")
    private String rsaPublicKey;

    @Check(name = "模式 standalone-独立模式 union-联邦模式")
    private String mode;

    @Check(name = "密钥类型")
    private SecretKeyType secretKeyType = SecretKeyType.rsa;

    //region getter/setter


    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServingBaseUrl() {
        return servingBaseUrl;
    }

    public void setServingBaseUrl(String servingBaseUrl) {
        this.servingBaseUrl = servingBaseUrl;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public String getRsaPublicKey() {
        return rsaPublicKey;
    }

    public void setRsaPublicKey(String rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }

    //endregion
}

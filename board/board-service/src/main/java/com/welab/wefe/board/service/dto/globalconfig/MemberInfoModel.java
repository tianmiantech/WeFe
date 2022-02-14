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

package com.welab.wefe.board.service.dto.globalconfig;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author zane.luo
 */
public class MemberInfoModel {

    @Check(name = "联邦成员 Id", desc = "全局唯一，默认为uuid。")
    private String memberId;
    @Check(name = "联邦成员名称")
    private String memberName;
    @Check(name = "联邦成员邮箱")
    private String memberEmail;
    @Check(name = "联邦成员电话")
    private String memberMobile;
    @Check(name = "联邦成员网关访问地址")
    private String memberGatewayUri;
    @Check(name = "是否允许对外公开数据集基础信息")
    private Boolean memberAllowPublicDataSet;

    @Check(name = "私钥")
    private String rsaPrivateKey;
    @Check(name = "公钥")
    private String rsaPublicKey;

    @Check(name = "成员头像")
    private String memberLogo;

    @Check(name = "成员隐身状态")
    private Boolean memberHidden;

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

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberMobile() {
        return memberMobile;
    }

    public void setMemberMobile(String memberMobile) {
        this.memberMobile = memberMobile;
    }

    public String getMemberGatewayUri() {
        return memberGatewayUri;
    }

    public void setMemberGatewayUri(String memberGatewayUri) {
        this.memberGatewayUri = memberGatewayUri;
    }

    public Boolean getMemberAllowPublicDataSet() {
        return memberAllowPublicDataSet;
    }

    public void setMemberAllowPublicDataSet(Boolean memberAllowPublicDataSet) {
        this.memberAllowPublicDataSet = memberAllowPublicDataSet;
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

    public String getMemberLogo() {
        return memberLogo;
    }

    public void setMemberLogo(String memberLogo) {
        this.memberLogo = memberLogo;
    }

    public Boolean getMemberHidden() {
        return memberHidden;
    }

    public void setMemberHidden(Boolean memberHidden) {
        this.memberHidden = memberHidden;
    }

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }
    //endregion
}

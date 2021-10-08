/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.dto.globalconfig;

/**
 * @author Zane
 */

/**
 * @author zane.luo
 */
public class MemberInfoModel {

    /**
     * 联邦成员 Id;
     * 全局唯一，默认为uuid。
     */
    private String memberId;
    /**
     * 联邦成员名称
     */
    private String memberName;
    /**
     * 联邦成员邮箱
     */
    private String memberEmail;
    /**
     * 联邦成员电话
     */
    private String memberMobile;
    /**
     * 联邦成员网关访问地址
     */
    private String memberGatewayUri;
    /**
     * 是否允许对外公开数据集基础信息
     */
    private Boolean memberAllowPublicDataSet;

    /**
     * 私钥
     */
    private String rsaPrivateKey;
    /**
     * 公钥
     */
    private String rsaPublicKey;

    /**
     * 成员头像
     */
    private String memberLogo;

    /**
     * 成员隐身状态
     */
    private Boolean memberHidden;

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

    //endregion
}

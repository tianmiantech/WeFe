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

package com.welab.wefe.gateway.dto;

/**
 * @author Zane
 */

import com.welab.wefe.common.constant.SecretKeyType;

/**
 * @author aaron.li
 */
public class MemberInfoModel {

    /**
     * Federal member ID;
     * Globally unique. The default value is UUID.
     */
    private String memberId;
    /**
     * Name of federal member
     */
    private String memberName;
    /**
     * Federal member mailbox
     */
    private String memberEmail;
    /**
     * Federal member telephone
     */
    private String memberMobile;
    /**
     * Federated member gateway access address
     */
    private String memberGatewayUri;
    /**
     * Is it allowed to disclose the basic information of the dataset
     */
    private Boolean memberAllowPublicDataSet;

    /**
     * Private key
     */
    private String rsaPrivateKey;
    /**
     * Public key
     */
    private String rsaPublicKey;

    /**
     * Member Avatar
     */
    private String memberLogo;

    /**
     * Member stealth status
     */
    private Boolean memberHidden;

    /**
     * Secret key type, default rsa
     */
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

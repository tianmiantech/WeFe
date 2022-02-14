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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.MEMBER)
public class Member extends AbstractBlockChainBusinessModel {
    private String memberId;
    private String name;
    private String mobile;
    private String email;
    private String allowOpenDataSet;
    private String hidden;
    private String freezed;
    private String lostContact;
    private String publicKey;
    private String gatewayUri;
    private String logo;
    private String lastActivityTime;
    private SecretKeyType secretKeyType = SecretKeyType.rsa;


    private MemberExtJSON extJson = new MemberExtJSON();




    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAllowOpenDataSet() {
        return allowOpenDataSet;
    }

    public void setAllowOpenDataSet(String allowOpenDataSet) {
        this.allowOpenDataSet = allowOpenDataSet;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getFreezed() {
        return freezed;
    }

    public void setFreezed(String freezed) {
        this.freezed = freezed;
    }

    public String getLostContact() {
        return lostContact;
    }

    public void setLostContact(String lostContact) {
        this.lostContact = lostContact;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getGatewayUri() {
        return gatewayUri;
    }

    public void setGatewayUri(String gatewayUri) {
        this.gatewayUri = gatewayUri;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(String lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public MemberExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(MemberExtJSON extJson) {
        this.extJson = extJson;
    }

    public SecretKeyType getSecretKeyType() {
        return secretKeyType;
    }

    public void setSecretKeyType(SecretKeyType secretKeyType) {
        this.secretKeyType = secretKeyType;
    }
}

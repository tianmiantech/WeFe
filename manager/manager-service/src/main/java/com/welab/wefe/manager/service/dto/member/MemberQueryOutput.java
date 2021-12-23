/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.web.dto.AbstractTimedApiOutput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/20
 */
public class MemberQueryOutput  extends AbstractTimedApiOutput {
    private String id;
    private String name;
    private String mobile;
    private String email;
    private int allowOpenDataSet;
    private int hidden;
    private int freezed;
    private int lostContact;
    private String publicKey;
    private String gatewayUri;
    private String logo;
    private long logTime;
    private long lastActivityTime;
    private int status;
    private MemberExtJSON extJson;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getAllowOpenDataSet() {
        return allowOpenDataSet;
    }

    public void setAllowOpenDataSet(int allowOpenDataSet) {
        this.allowOpenDataSet = allowOpenDataSet;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public int getFreezed() {
        return freezed;
    }

    public void setFreezed(int freezed) {
        this.freezed = freezed;
    }

    public int getLostContact() {
        return lostContact;
    }

    public void setLostContact(int lostContact) {
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

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }


    public MemberExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(MemberExtJSON extJson) {
        this.extJson = extJson;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

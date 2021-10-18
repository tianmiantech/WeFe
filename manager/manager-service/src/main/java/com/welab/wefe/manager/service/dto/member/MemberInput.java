package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.web.dto.UniqueIDApiInput;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
public class MemberInput extends UniqueIDApiInput {

    private String name;
    private String mobile;
    private String email;
    private int allowOpenDataSet;
    private String publicKey;
    private String gatewayUri;

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
}
package com.welab.wefe.manager.service.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractBlockChainEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
@Entity
@Table(name = "u_member")
public class Member extends AbstractBlockChainEntity {

    private String name;
    private String mobile;
    private String email;
    @Column(name = "allow_open_data_set")
    private int allowOpenDataSet;
    private int hidden;
    private int freezed;
    @Column(name = "lost_contact")
    private int lostContact;
    @Column(name = "public_key")
    private String publicKey;
    @Column(name = "gateway_uri")
    private String gatewayUri;
    private String logo;
    @Column(name = "last_activity_time")
    private long lastActivityTime;

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

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }
}

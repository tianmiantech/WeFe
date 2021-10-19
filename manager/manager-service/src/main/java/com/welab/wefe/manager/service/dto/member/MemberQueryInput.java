package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.manager.service.dto.base.PageInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class MemberQueryInput extends PageInput {
    private String id;
    private String name;

    private Boolean lostContact;
    private Boolean hidden;
    private Boolean freezed;
    private String status;


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

    public Boolean getLostContact() {
        return lostContact;
    }

    public void setLostContact(Boolean lostContact) {
        this.lostContact = lostContact;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getFreezed() {
        return freezed;
    }

    public void setFreezed(Boolean freezed) {
        this.freezed = freezed;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

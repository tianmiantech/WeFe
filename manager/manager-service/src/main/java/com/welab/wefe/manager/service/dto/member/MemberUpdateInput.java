package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class MemberUpdateInput extends BaseInput {
    @Check(require = true)
    private String id;
    private Boolean hidden;
    private Boolean freezed;
    private Boolean lostContact;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getLostContact() {
        return lostContact;
    }

    public void setLostContact(Boolean lostContact) {
        this.lostContact = lostContact;
    }
}

package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.web.dto.PageableApiInput;

/**
 * @Author Jervis
 * @Date 2020-05-27
 **/
public class MemberQueryInput extends PageableApiInput {

    private String id;
    private String name;

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
}

package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.web.dto.PageableApiInput;

/**
 * @Author Jervis
 * @Date 2020-05-27
 **/
public class DataSetQueryInput extends PageableApiInput {

    private String id;
    private String memberId;
    private String memberName;
    private String name;
    private Boolean containsY;
    private String tag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Boolean getContainsY() {
        return containsY;
    }

    public void setContainsY(Boolean containsY) {
        this.containsY = containsY;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}

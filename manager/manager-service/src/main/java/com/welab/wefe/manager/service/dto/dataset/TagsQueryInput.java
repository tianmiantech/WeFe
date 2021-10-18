package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.web.dto.PageableApiInput;

/**
 * @Author Jervis
 * @Date 2020-06-01
 **/
public class TagsQueryInput extends PageableApiInput {

    private String memberId;
    private String tagName;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}

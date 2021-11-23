package com.welab.wefe.manager.service.dto.tag;

import com.welab.wefe.manager.service.dto.base.PageInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class DataSetTagsQueryInput extends PageInput {
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

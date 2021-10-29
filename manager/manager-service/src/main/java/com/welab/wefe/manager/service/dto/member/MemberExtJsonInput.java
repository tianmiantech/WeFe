package com.welab.wefe.manager.service.dto.member;

import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class MemberExtJsonInput extends BaseInput {
    @Check(require = true)
    private String id;
    private MemberExtJSON extJson;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MemberExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(MemberExtJSON extJson) {
        this.extJson = extJson;
    }
}

package com.welab.wefe.manager.service.dto.tag;

import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetDefaultTagExtJSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class DatSetDefaultTagAddInput extends BaseInput {
    @Check(require = true)
    private String tagId;
    @Check(require = true)
    private String tagName;
    private DataSetDefaultTagExtJSON extJson;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public DataSetDefaultTagExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataSetDefaultTagExtJSON extJson) {
        this.extJson = extJson;
    }
}

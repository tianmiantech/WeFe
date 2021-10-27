package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.manager.service.dto.base.BaseInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class DataSetUpdateExtJsonInput extends BaseInput {
    @Check(require = true)
    private String id;
    private DataSetExtJSON extJson;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataSetExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(DataSetExtJSON extJson) {
        this.extJson = extJson;
    }
}

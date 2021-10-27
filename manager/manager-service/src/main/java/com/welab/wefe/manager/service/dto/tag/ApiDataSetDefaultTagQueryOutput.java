package com.welab.wefe.manager.service.dto.tag;

import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetDefaultTagExtJSON;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class ApiDataSetDefaultTagQueryOutput {
    private String id;
    private String tagName;
    private int status;
    private DataSetDefaultTagExtJSON extJson;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

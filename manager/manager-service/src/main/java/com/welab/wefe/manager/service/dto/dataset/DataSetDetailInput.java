package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @Author Jervis
 * @Date 2020-06-10
 **/
public class DataSetDetailInput extends AbstractApiInput {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

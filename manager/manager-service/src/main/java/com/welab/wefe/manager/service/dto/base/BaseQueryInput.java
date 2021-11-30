package com.welab.wefe.manager.service.dto.base;

public class BaseQueryInput extends PageInput{
    private Boolean status = false;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}

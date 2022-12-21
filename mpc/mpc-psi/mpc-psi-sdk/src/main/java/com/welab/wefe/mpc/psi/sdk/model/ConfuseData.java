package com.welab.wefe.mpc.psi.sdk.model;

import java.util.ArrayList;
import java.util.List;

public class ConfuseData {

    private String singleFieldName;
    private List<String> mixFieldNames = new ArrayList<>();;
    private List<String> data = new ArrayList<>();
    private boolean isJson = false;

    public String getSingleFieldName() {
        return singleFieldName;
    }

    public void setSingleFieldName(String singleFieldName) {
        this.singleFieldName = singleFieldName;
    }

    public List<String> getMixFieldNames() {
        return mixFieldNames;
    }

    public void setMixFieldNames(List<String> mixFieldNames) {
        this.mixFieldNames = mixFieldNames;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public boolean isJson() {
        return isJson;
    }

    public void setJson(boolean isJson) {
        this.isJson = isJson;
    }

    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    public int size() {
        return isEmpty() ? 0 : data.size();
    }

}

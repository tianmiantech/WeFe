package com.welab.wefe.mpc.psi.sdk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfuseData {

    private String singleFieldName;
    private List<String> mixFieldNames = new ArrayList<>();;
    private boolean isJson = false;
    private Function<String, List<String>> generateDataFunc;

    public List<String> generateConfuseData(String s) {
        if (generateDataFunc != null) {
            return generateDataFunc.apply(s);
        } else {
            return new ArrayList<>();
        }
    }

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

    public boolean isJson() {
        return isJson;
    }

    public void setJson(boolean isJson) {
        this.isJson = isJson;
    }

    public Function<String, List<String>> getGenerateDataFunc() {
        return generateDataFunc;
    }

    public void setGenerateDataFunc(Function<String, List<String>> generateDataFunc) {
        this.generateDataFunc = generateDataFunc;
    }

}

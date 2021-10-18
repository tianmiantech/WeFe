package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.UniqueIDApiInput;

/**
 * @Author Jervis
 * @Date 2020-05-27
 **/
public class DataSetInput extends UniqueIDApiInput {

    @Check(require = true)
    private String memberId;
    private String publicMemberList;
    @Check(require = true)
    private String name;
    private int containsY;
    private String sampleCount;
    private String dimensionCount;
    private String dimensionList;
    private Integer useCount;
    private String description;
    @Check(require = true, regex = "[\\u4e00-\\u9fa5a-zA-Z,]+")
    private String tags;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getPublicMemberList() {
        return publicMemberList;
    }

    public void setPublicMemberList(String publicMemberList) {
        this.publicMemberList = publicMemberList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContainsY() {
        return containsY;
    }

    public void setContainsY(int containsY) {
        this.containsY = containsY;
    }

    public String getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(String sampleCount) {
        this.sampleCount = sampleCount;
    }

    public String getDimensionCount() {
        return dimensionCount;
    }

    public void setDimensionCount(String dimensionCount) {
        this.dimensionCount = dimensionCount;
    }

    public String getDimensionList() {
        return dimensionList;
    }

    public void setDimensionList(String dimensionList) {
        this.dimensionList = dimensionList;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}

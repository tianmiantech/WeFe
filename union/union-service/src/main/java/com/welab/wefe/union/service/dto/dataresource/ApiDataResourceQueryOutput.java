package com.welab.wefe.union.service.dto.dataresource;

import com.welab.wefe.common.enums.DataResourceType;
import com.welab.wefe.common.web.dto.AbstractTimedApiOutput;

public class ApiDataResourceQueryOutput extends AbstractTimedApiOutput {
    private String dataResourceId;
    private String memberId;
    private String memberName;
    private String name;
    private String description;
    private String tags;
    private String totalDataCount;
    private String publicLevel;
    private String publicMemberList;
    private String usageCountInJob;
    private String usageCountInFlow;
    private String usageCountInProject;
    private String usageCountInMember;
    private String enable;
    private DataResourceType dataResourceType;


    public String getDataResourceId() {
        return dataResourceId;
    }

    public void setDataResourceId(String dataResourceId) {
        this.dataResourceId = dataResourceId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(String totalDataCount) {
        this.totalDataCount = totalDataCount;
    }

    public String getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(String publicLevel) {
        this.publicLevel = publicLevel;
    }

    public String getPublicMemberList() {
        return publicMemberList;
    }

    public void setPublicMemberList(String publicMemberList) {
        this.publicMemberList = publicMemberList;
    }

    public String getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(String usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public String getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(String usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public String getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(String usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
    }

    public String getUsageCountInMember() {
        return usageCountInMember;
    }

    public void setUsageCountInMember(String usageCountInMember) {
        this.usageCountInMember = usageCountInMember;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}

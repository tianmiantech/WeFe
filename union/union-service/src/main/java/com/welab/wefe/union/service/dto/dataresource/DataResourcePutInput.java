package com.welab.wefe.union.service.dto.dataresource;

public class DataResourcePutInput extends AbstractDataResourceInput {
    protected String name;
    protected String description;
    protected String tags;
    protected long totalDataCount;
    protected String publicLevel;
    protected String publicMemberList;
    protected int usageCountInJob;
    protected int usageCountInFlow;
    protected int usageCountInProject;
    protected int usageCountInMember;
    protected String dataResourceType;

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

    public long getTotalDataCount() {
        return totalDataCount;
    }

    public void setTotalDataCount(long totalDataCount) {
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

    public int getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(int usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public int getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(int usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public int getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(int usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
    }

    public int getUsageCountInMember() {
        return usageCountInMember;
    }

    public void setUsageCountInMember(int usageCountInMember) {
        this.usageCountInMember = usageCountInMember;
    }

    public String getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(String dataResourceType) {
        this.dataResourceType = dataResourceType;
    }
}

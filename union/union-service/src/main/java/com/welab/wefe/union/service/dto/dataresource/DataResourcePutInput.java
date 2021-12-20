package com.welab.wefe.union.service.dto.dataresource;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.dto.base.BaseInput;

public class DataResourcePutInput extends BaseInput {
    @Check(require = true)
    protected String dataResourceId;
    @Check(require = true)
    protected String memberId;
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
    @Check(require = true)
    protected DataResourceType dataResourceType;

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

    public DataResourceType getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(DataResourceType dataResourceType) {
        this.dataResourceType = dataResourceType;
    }
}

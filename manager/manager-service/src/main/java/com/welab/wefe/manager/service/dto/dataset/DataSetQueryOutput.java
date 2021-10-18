package com.welab.wefe.manager.service.dto.dataset;

import com.welab.wefe.common.web.dto.AbstractTimedApiOutput;
import com.welab.wefe.manager.service.entity.DataSet;

/**
 * @Author Jervis
 * @Date 2020-05-27
 **/
public class DataSetQueryOutput extends AbstractTimedApiOutput {
    private String id;
    private String memberName;
    private String name;
    private String memberId;
    private int containsY;
    private Long rowCount;
    private Integer columnCount;
    private String columnNameList;
    private Integer featureCount;
    private String featureNameList;
    private String publicLevel;
    private String publicMemberList;
    private Integer usageCountInJob;
    private Integer usageCountInFlow;
    private Integer usageCountInProject;
    private String description;
    private String tags;

    public DataSetQueryOutput(){}
    public DataSetQueryOutput(DataSet dataSet, String memberName){
        this.id = dataSet.getId();
        this.memberId = dataSet.getMemberId();
        this.memberName = memberName;
        this.name = dataSet.getName();
        this.containsY = dataSet.getContainsY();
        this.rowCount = dataSet.getRowCount();
        this.columnCount = dataSet.getColumnCount();
        this.columnNameList = dataSet.getColumnNameList();
        this.featureCount = dataSet.getFeatureCount();
        this.featureNameList = dataSet.getFeatureNameList();
        this.publicLevel = dataSet.getPublicLevel();
        this.publicMemberList = dataSet.getPublicMemberList();
        this.usageCountInJob = dataSet.getUsageCountInJob();
        this.usageCountInFlow = dataSet.getUsageCountInFlow();
        this.usageCountInProject = dataSet.getUsageCountInProject();
        this.description = dataSet.getDescription();
        this.tags = dataSet.getTags();
        this.createdTime = dataSet.getCreatedTime();
        this.updatedTime = dataSet.getUpdatedTime();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getContainsY() {
        return containsY;
    }

    public void setContainsY(int containsY) {
        this.containsY = containsY;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public Integer getFeatureCount() {
        return featureCount;
    }

    public void setFeatureCount(Integer featureCount) {
        this.featureCount = featureCount;
    }

    public String getFeatureNameList() {
        return featureNameList;
    }

    public void setFeatureNameList(String featureNameList) {
        this.featureNameList = featureNameList;
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

    public Integer getUsageCountInJob() {
        return usageCountInJob;
    }

    public void setUsageCountInJob(Integer usageCountInJob) {
        this.usageCountInJob = usageCountInJob;
    }

    public Integer getUsageCountInFlow() {
        return usageCountInFlow;
    }

    public void setUsageCountInFlow(Integer usageCountInFlow) {
        this.usageCountInFlow = usageCountInFlow;
    }

    public Integer getUsageCountInProject() {
        return usageCountInProject;
    }

    public void setUsageCountInProject(Integer usageCountInProject) {
        this.usageCountInProject = usageCountInProject;
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

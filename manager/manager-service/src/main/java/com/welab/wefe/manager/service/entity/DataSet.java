package com.welab.wefe.manager.service.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractBlockChainEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 联邦成员同步的数据集
 *
 * @Date 2020-05-22
 **/
@Entity
@Table(name = "u_data_set")
public class DataSet extends AbstractBlockChainEntity {

    private String name;
    @Column(name = "member_id")
    private String memberId;
    @Column(name = "contains_y")
    private int containsY;
    @Column(name = "row_count")
    private Long rowCount;
    @Column(name = "column_count")
    private Integer columnCount;
    @Column(name = "column_name_list")
    private String columnNameList;
    @Column(name = "feature_count")
    private Integer featureCount;
    @Column(name = "feature_name_list")
    private String featureNameList;
    @Column(name = "public_level")
    private String publicLevel;
    @Column(name = "public_member_list")
    private String publicMemberList;
    @Column(name = "usage_count_in_job")
    private int usageCountInJob;
    @Column(name = "usage_count_in_flow")
    private int usageCountInFlow;
    @Column(name = "usage_count_in_project")
    private int usageCountInProject;
    private String description;
    private String tags;

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

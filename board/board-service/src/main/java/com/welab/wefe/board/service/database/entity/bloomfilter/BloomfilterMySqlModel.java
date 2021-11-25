/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.database.entity.bloomfilter;

import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.enums.BloomfilterProgressType;
import com.welab.wefe.common.enums.DataSetPublicLevel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author jaccky.jiang
 */
@Entity(name = "bloomfilter")
public class BloomfilterMySqlModel extends AbstractBaseMySqlModel {

    /**
     * 过滤器名称
     */
    private String name;
    /**
     * 标签
     */
    private String tags;
    /**
     * 描述
     */
    private String description;
    /**
     * 过滤器选择的字段列表
     */
    private String columnNameList;
    /**
     * 过滤器的可见性
     */
    @Enumerated(EnumType.STRING)
    private DataSetPublicLevel publicLevel;

    /**
     * 可见成员列表，只有在列表中的联邦成员才可以看到该过滤器的基本信息
     */
    private String publicMemberList;
    /**
     * 使用次数
     */
    private Integer usageCount = 0;
    /**
     * 数据源id
     */
    private String dataSourceId;
    /**
     * 数据源类型
     */
    @Enumerated(EnumType.STRING)
    private BloomfilterAddMethod bloomfilterAddMethod;
    /**
     * The exponent publicKey
     */
    private String e;
    /**
     * The publicKey modulus
     */
    private String n;
    /**
     * The exponent privateKey
     */
    private String d;
    /**
     * 生成过滤器目录
     */
    private String bloomfilterPath;
    /**
     * 源文件目录
     */
    private String sourcePath;
    /**
     * sql脚本
     */
    private String sqlScript;
    /**
     * 已经处理的数据行
     */
    private Integer processCount = 0;
    /**
     * 数据行数
     */
    private Long totalDataRowCount;
    /**
     * 当前生成过滤器的状态
     */
    private BloomfilterProgressType process;


    //region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(String columnNameList) {
        this.columnNameList = columnNameList;
    }

    public DataSetPublicLevel getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(DataSetPublicLevel publicLevel) {
        this.publicLevel = publicLevel;
    }

    public String getPublicMemberList() {
        return publicMemberList;
    }

    public void setPublicMemberList(String publicMemberList) {
        this.publicMemberList = publicMemberList;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public BloomfilterAddMethod getBloomfilterAddMethod() {
        return bloomfilterAddMethod;
    }

    public void setBloomfilterAddMethod(BloomfilterAddMethod bloomfilterAddMethod) {
        this.bloomfilterAddMethod = bloomfilterAddMethod;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getBloomfilterPath() {
        return bloomfilterPath;
    }

    public void setBloomfilterPath(String bloomfilterPath) {
        this.bloomfilterPath = bloomfilterPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSqlScript() {
        return sqlScript;
    }

    public void setSqlScript(String sqlScript) {
        this.sqlScript = sqlScript;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public Long getTotalDataRowCount() {
        return totalDataRowCount;
    }

    public void setTotalDataRowCount(Long totalDataRowCount) {
        this.totalDataRowCount = totalDataRowCount;
    }

    public BloomfilterProgressType getProcess() {
        return process;
    }

    public void setProcess(BloomfilterProgressType process) {
        this.process = process;
    }


    //endregion
}

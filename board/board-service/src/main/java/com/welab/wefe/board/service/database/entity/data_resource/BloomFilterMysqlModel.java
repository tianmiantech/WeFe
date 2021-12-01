/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.database.entity.data_resource;

import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.common.enums.DataResourceType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author zane
 * @date 2021/12/1
 */
@Entity(name = "bloom_filter")
public class BloomFilterMysqlModel extends DataResourceMysqlModel {
    /**
     * 密钥e
     */
    private String rsaE;
    /**
     * 密钥n
     */
    private String rsaN;
    /**
     * 密钥e
     */
    private String rsaD;
    /**
     * 数据源id
     */
    private String dataSourceId;
    /**
     * 数据源地址
     */
    private String sourcePath;
    /**
     * 主键hash生成方法
     */
    private String hashFunction;
    /**
     * 布隆过滤器添加方式
     */
    @Enumerated(EnumType.STRING)
    private BloomfilterAddMethod addMethod;
    /**
     * sql语句
     */
    private String sqlScript;

    public BloomFilterMysqlModel() {
        super.setResourceType(DataResourceType.BloomFilter);
    }

    // region getter/setter

    public String getRsaE() {
        return rsaE;
    }

    public void setRsaE(String rsaE) {
        this.rsaE = rsaE;
    }

    public String getRsaN() {
        return rsaN;
    }

    public void setRsaN(String rsaN) {
        this.rsaN = rsaN;
    }

    public String getRsaD() {
        return rsaD;
    }

    public void setRsaD(String rsaD) {
        this.rsaD = rsaD;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getHashFunction() {
        return hashFunction;
    }

    public void setHashFunction(String hashFunction) {
        this.hashFunction = hashFunction;
    }

    public BloomfilterAddMethod getAddMethod() {
        return addMethod;
    }

    public void setAddMethod(BloomfilterAddMethod addMethod) {
        this.addMethod = addMethod;
    }

    public String getSqlScript() {
        return sqlScript;
    }

    public void setSqlScript(String sqlScript) {
        this.sqlScript = sqlScript;
    }


    // endregion
}

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

package com.welab.wefe.serving.service.database.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "table_service")
@Table(name = "table_service")
public class TableServiceMySqlModel extends BaseServiceMySqlModel {

    private static final long serialVersionUID = -151994449884740867L;

    /**
     * 查询参数配置
     */
    @Column(name = "query_params")
    private String queryParams;

    /**
     * 查询配置参数描述
     */
    @Column(name = "query_params_config")
    private String queryParamsConfig;

    /**
     * SQL配置
     */
    @Column(name = "data_source")
    private String dataSource;// json

    /**
     * SQL配置
     */
    @Column(name = "service_config")
    private String serviceConfig;// json

    @Column(name = "ids_table_name")
    private String idsTableName;

    @Column(name = "operator")
    private String operator;

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getIdsTableName() {
        return idsTableName;
    }

    public void setIdsTableName(String idsTableName) {
        this.idsTableName = idsTableName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(String serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public String getQueryParamsConfig() {
        return queryParamsConfig;
    }

    public void setQueryParamsConfig(String queryParamsConfig) {
        this.queryParamsConfig = queryParamsConfig;
    }

}

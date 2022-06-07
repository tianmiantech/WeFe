/*
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
package com.welab.wefe.serving.service.dto;

import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.database.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceMySqlModel;

import javax.persistence.Column;

/**
 * @author hunter.zhao
 */
public class ServiceDetailOutput {

    private String id;
    /**
     * 服务名
     */
    private String name;
    /**
     * 服务地址
     */
    private String url;
    /**
     * 服务类型 1匿踪查询，2交集查询，3安全聚合(被查询方),4安全聚合(查询方)
     */
    @Column(name = "service_type")
    private int serviceType;

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

    /**
     * 是否在线 1在线 0离线
     */
    @Column(name = "status")
    private int status = 0;

    @Column(name = "ids_table_name")
    private String idsTableName;

    @Column(name = "operator")
    private String operator;

    public static ServiceDetailOutput convertByModel(ModelMySqlModel model) {
        ServiceDetailOutput output = ModelMapper.map(model, ServiceDetailOutput.class);
        output.setStatus(model.isEnable() ? 1 : 0);

        return output;
    }

    public static ServiceDetailOutput convertByService(ServiceMySqlModel service) {
        return ModelMapper.map(service, ServiceDetailOutput.class);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
}

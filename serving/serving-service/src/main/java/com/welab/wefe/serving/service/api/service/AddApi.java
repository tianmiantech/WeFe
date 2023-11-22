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

package com.welab.wefe.serving.service.api.service;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "service/add", name = "add service")
public class AddApi extends AbstractApi<AddApi.Input, AddApi.Output> {

    @Autowired
    private ServiceService service;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        Output output = service.save(input);
        return success(output);
    }

    public static class Output extends AbstractApiOutput {
        private String id;
        private String params;
        private String method = "POST";
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParams() {
            return params;
        }

        public void setParams(String params) {
            this.params = params;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

    public static class Input extends AbstractApiInput {

        @Check(require = true, name = "服务名")
        private String name;
        @Check(require = true, name = "服务地址")
        private String url;
        @Check(require = true, name = "服务类型")
        private int serviceType;
        @Check(name = "操作")
        private String operator;
        @Check(name = "查询参数配置")
        private List<String> queryParams;
        @Check(name = "SQL配置")
        private String dataSource;// json
        @Check(name = "服务配置")
        private String serviceConfig;// json

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (!ServiceTypeEnum.checkServiceType(serviceType)) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("服务类型错误：" + serviceType);
            }
            if (ServiceTypeEnum.needDataSource(serviceType)) {
                if (StringUtils.isBlank(dataSource)) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("SQL 配置不能为空");
                }
                if (serviceType == ServiceTypeEnum.PSI.getCode()) {
                    JObject dataS = JObject.create(dataSource);
                    JSONArray keyCalcRules = dataS.getJSONArray("key_calc_rules");
                    if (keyCalcRules == null || keyCalcRules.isEmpty()) {
                        StatusCode.PARAMETER_VALUE_INVALID.throwException("求交主键不能为空");
                    }
                }
            }
            if (ServiceTypeEnum.needServiceConfig(serviceType)) {
                if (StringUtils.isBlank(serviceConfig)) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("服务配置不能为空");
                }
            }
            if(url.startsWith("/")) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("服务地址不能/开头");   
            }
            if(!url.matches("^[0-9a-zA-Z_][0-9a-zA-Z_/-]{1,}$")) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("服务地址只能包含数字字母与'/','_','-'");   
            }
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

        public List<String> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(List<String> queryParams) {
            this.queryParams = queryParams;
        }

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
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

    }
}

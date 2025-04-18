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

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "service/show_sql", name = "service show sql")
public class ServiceShowSQLApi extends AbstractApi<ServiceShowSQLApi.Input, ServiceShowSQLApi.Output> {

	@Autowired
	ServiceService serviceService;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
		return success(serviceService.showSql(input));
	}

	public static class Output extends AbstractApiOutput {
		private JObject result;

		public JObject getResult() {
			return result;
		}

		public void setResult(JObject result) {
			this.result = result;
		}

	}

	public static class Input extends AbstractApiInput {
		@Check(name = "SQL配置")
		private String dataSource;// json
		@Check(name = "查询参数")
		private String params;

		public String getDataSource() {
			return dataSource;
		}

		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}

		public String getParams() {
			return params;
		}

		public void setParams(String params) {
			this.params = params;
		}

	}

}

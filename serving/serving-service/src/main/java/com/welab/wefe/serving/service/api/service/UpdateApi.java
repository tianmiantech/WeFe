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

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "service/update", name = "update service info")
public class UpdateApi extends AbstractNoneOutputApi<UpdateApi.Input> {

	@Autowired
	private ServiceService service;

	@Override
	protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
		service.update(input);
		return success();
	}

	public static class Input extends AbstractApiInput {

		@Check(require = true, name = "ID")
		private String id;
		@Check(name = "服务名")
		private String name;
		@Check(name = "服务地址")
		private String url;
		@Check(name = "服务类型")
		private int serviceType;
		@Check(name = "查询参数配置")
		private String queryParams;// json
		@Check(name = "SQL配置")
		private String dataSource;// json
		@Check(name = "查询字段")
		private String conditionFields;// json
		@Check(name = "是否在线")
		private int status;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
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

		public String getConditionFields() {
			return conditionFields;
		}

		public void setConditionFields(String conditionFields) {
			this.conditionFields = conditionFields;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

	}

}

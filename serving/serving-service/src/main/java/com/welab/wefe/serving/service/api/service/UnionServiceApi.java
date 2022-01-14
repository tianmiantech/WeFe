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
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.UnionServiceService;

@Api(path = "service/union/query", name = "query union service list")
public class UnionServiceApi extends AbstractApi<UnionServiceApi.Input, PagingOutput<UnionServiceApi.Output>> {

	@Autowired
	UnionServiceService unionServiceService;

	@Override
	protected ApiResult<PagingOutput<Output>> handle(Input input) throws StatusCodeWithException, IOException {
		return success(unionServiceService.query(input));
	}

	public static class Output extends AbstractApiOutput {
		private String id;// 服务ID
		private String name; // 服务名
		private String supplierId;// 供应商Id
		private String supplierName;// 供应商
		private String baseUrl;
		private String apiName;
		private List<String> params;
		private int serviceType; // 服务类型
		private Date createdTime;// 创建时间
		private Date updatedTime;// 更新时间

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

		public String getSupplierId() {
			return supplierId;
		}

		public void setSupplierId(String supplierId) {
			this.supplierId = supplierId;
		}

		public String getSupplierName() {
			return supplierName;
		}

		public void setSupplierName(String supplierName) {
			this.supplierName = supplierName;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getApiName() {
			return apiName;
		}

		public void setApiName(String apiName) {
			this.apiName = apiName;
		}

		public List<String> getParams() {
			return params;
		}

		public void setParams(List<String> params) {
			this.params = params;
		}

		public int getServiceType() {
			return serviceType;
		}

		public void setServiceType(int serviceType) {
			this.serviceType = serviceType;
		}

		public Date getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		public Date getUpdatedTime() {
			return updatedTime;
		}

		public void setUpdatedTime(Date updatedTime) {
			this.updatedTime = updatedTime;
		}
	}

	public static class Input extends PagingInput {

		// 服务类型 1=匿踪查询，2=交集查询，3=安全聚合 4=安全聚合（查询方）
		private int serviceType = -1;
		private String memberName;
		private String serviceName;

		public String getMemberName() {
			return memberName;
		}

		public void setMemberName(String memberName) {
			this.memberName = memberName;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

		public int getServiceType() {
			return serviceType;
		}

		public void setServiceType(int serviceType) {
			this.serviceType = serviceType;
		}

	}
}

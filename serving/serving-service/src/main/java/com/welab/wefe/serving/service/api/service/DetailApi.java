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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ServiceRepository;
import com.welab.wefe.serving.service.utils.ModelMapper;

@Api(path = "service/detail", name = "服务详情")
public class DetailApi extends AbstractApi<DetailApi.Input, DetailApi.Output> {

	@Autowired
	private ServiceRepository serviceRepo;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {

		Optional<ServiceMySqlModel> serviceMySqlModel = serviceRepo.findById(input.getId());
		if (serviceMySqlModel == null) {
			return fail("service entity was found");
		}
		ServiceMySqlModel entity = serviceMySqlModel.get();

		DetailApi.Output output = ModelMapper.map(entity, DetailApi.Output.class);
		if (StringUtils.isNotBlank(entity.getDataSource())) {
			output.setDataSource(JSONObject.parseObject(entity.getDataSource()));
		}
		if (StringUtils.isNotBlank(entity.getQueryParams())) {
			output.setQueryParams(Arrays.asList(entity.getQueryParams().split(",")));
		}
		if (StringUtils.isNotBlank(entity.getServiceConfig())) {
			output.setServiceConfig(JSONObject.parseArray(entity.getServiceConfig()));
		}
		return success(output);
	}

	public static class Input extends AbstractApiInput {

		@Check(name = "主键id")
		private String id;

		// region getter/setter

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
		// endregion
	}

	public static class Output extends AbstractApiOutput {
		private String id;
		private String name;
		private String url;
		private int serviceType;
		private List<String> queryParams;// json
		private JSONObject dataSource;// json
		private JSONArray serviceConfig;
		private String createdBy;
		private String updatedBy;
		private Date createdTime;
		private Date updatedTime;
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

		public List<String> getQueryParams() {
			return queryParams;
		}

		public void setQueryParams(List<String> queryParams) {
			this.queryParams = queryParams;
		}

		public JSONObject getDataSource() {
			return dataSource;
		}

		public void setDataSource(JSONObject dataSource) {
			this.dataSource = dataSource;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public String getUpdatedBy() {
			return updatedBy;
		}

		public void setUpdatedBy(String updatedBy) {
			this.updatedBy = updatedBy;
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

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public JSONArray getServiceConfig() {
			return serviceConfig;
		}

		public void setServiceConfig(JSONArray serviceConfig) {
			this.serviceConfig = serviceConfig;
		}

	}

}

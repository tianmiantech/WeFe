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
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "service/online", name = "online service")
public class OnlineApi extends AbstractApi<OnlineApi.Input, OnlineApi.Output> {

	@Autowired
	ServiceService serviceService;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
		serviceService.onlineService(input.getId());
		return success();
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

	}

}

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
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "api", name = "api service", forward = true, login = false, rsaVerify = true, domain = Caller.Customer)
public class RouteApi extends AbstractApi<RouteApi.Input, JObject> {

	@Autowired
	private ServiceService service;

	@Override
	protected ApiResult<JObject> handle(Input input) throws StatusCodeWithException, IOException {
		LOG.info("request =" + JObject.toJSONString(input));
		JObject result = service.executeService(input);
		LOG.info("response =" + JObject.toJSONString(result));
		return success(result);
	}

	public static class Input extends AbstractApiInput {
		private String data;

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

	}

}
